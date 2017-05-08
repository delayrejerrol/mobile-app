package com.jerrol.app.maplocator.googleapis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jerrol-PC on 7/4/2016.
 */
public class LocationAPI implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult> {

    private Context context;

    private static final String TAG = "LocationAPI";

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * The desire interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 4000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Represents a Geographical location.
     */
    private Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the Start updates and Stop updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as string.
     */
    private String mLastUpdateTime;

    /**
     * The formatted location address.
     */
    private String mAddressOutput;

    /**
     * Tracks whether the user has requested an address. Becomes true when the user requests an
     * address and false when the address (or an error message) is delivered.
     * The user requests an address by pressing the Fetch Address button. This may happen
     * before GoogleApiClient connects. This activity uses this boolean to keep track of the
     * user's intent. If the value is true, the activity tries to fetch the address as soon as
     * GoogleApiClient connects.
     */
    private boolean mAddressRequested;

    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

    /**
     * LatLng from address generated.
     */
    private LatLng latLngFromAddress;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_SETTINGS = 0x1;


    LocationAPIListener mListener;

    public interface LocationAPIListener {
        void locationUpdate(Location location);
        void getOutputAddress(String address);
        void getLatlngAddress(LatLng latLng);
    }

    public LocationAPI(Context context) {
        this.context = context;
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        mResultReceiver = new AddressResultReceiver(new Handler());
        mAddressRequested = false;
        mAddressOutput = "";

        buildGoogleApiClient();
        buildLocationSettingsRequest();

        if(context instanceof LocationAPIListener) {
            mListener = (LocationAPIListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LocationAPIListener");
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    private synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in the AndroidManifest.xml
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    /**
     * Uses a {@link LocationSettingsRequest.Builder} to build
     * a {@link LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult( (AppCompatActivity) context, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    /**
     * A controller that will start the location updates.
     */
    public void startLocationUpdate() {
        checkLocationSettings();
        /*if(isLocationEnabled()) {
            if (!mRequestingLocationUpdates) {
                mRequestingLocationUpdates = true;
                startLocationUpdates();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Location API");
            builder.setMessage("Enable location services to continue.");
            builder.setPositiveButton("OK", (dialog, which) -> {
                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            });
            builder.setNegativeButton("CANCEL", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }*/
    }

    /**
     * A controller that will stop the location updates.
     */
    public void stopLocationUpdate() {
        if(mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        .setResultCallback(status -> {
            if(status.getStatus().isSuccess()) {
                mRequestingLocationUpdates = true;
                Log.i(TAG, "Successfully requested location updates");
            } else {
                Log.e(TAG, "Failed in requesting location updates, "
                + "status code: " + status.getStatusCode()
                + ", message: " + status.getStatusMessage());
            }
        });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is specially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    public void OnStart() {
        mGoogleApiClient.connect();
    }

    public void OnResume() {
        if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    public void OnPause() {
        if(mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    /**
     * Call this before {@code super.onStop()}
     */
    public void OnStop() {
        mGoogleApiClient.disconnect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss", Locale.US).format(new Date());
            Log.i(TAG, "Last update time: " + mLastUpdateTime);

            //
            if(mCurrentLocation != null) {
                if (!Geocoder.isPresent()) {
                    Log.i(TAG, LocationAPIConstants.NO_GEOCODER_AVAILABLE);
                    return;
                }

                if(mAddressRequested) {
                    startIntentService();
                }
            }
        }

        if(mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss", Locale.US).format(new Date());
        Log.i(TAG, "Last update time: " + mLastUpdateTime);
        mListener.locationUpdate(mCurrentLocation);
        //updateUI();
        //Toast.makeText(this, getResources().getString(R.string.location_updated_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getCode() = " + connectionResult.getErrorCode());
    }

    public boolean hasCurrentLocation() {

        return mCurrentLocation != null;
    }

    public Double getLatitude() {
        return mCurrentLocation.getLatitude();
    }

    public Double getLongitude() {
        return mCurrentLocation.getLongitude();
    }

    /**
     * Check if location service is enabled. if not it open a settings to enable location service.
     *
     * @return true if location services is enable; otherwise false.
     */
    public boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProvider;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProvider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProvider);
        }
    }

    public void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(context, FetchAddressIntentService.class);

        // Pass the result receiveer as an extra to the service.
        intent.putExtra(LocationAPIConstants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(LocationAPIConstants.LOCATION_DATA_EXTRA, mCurrentLocation);
        intent.putExtra("REVERSE", 0);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        context.startService(intent);
    }

    public void startIntentService(String address) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(context, FetchAddressIntentService.class);

        // Pass the result receiveer as an extra to the service.
        intent.putExtra(LocationAPIConstants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(LocationAPIConstants.LOCATION_DATA_EXTRA, address);
        intent.putExtra("REVERSE", 1);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        context.startService(intent);
    }

    public void startIntentServiceWithSelectedLocation(double latitude, double longitude) {
        Location newLocation = new Location(LocationManager.NETWORK_PROVIDER);
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);

        mCurrentLocation = newLocation;
        startIntentService();
    }

    public String getAddressOutput() {
        return mAddressOutput;
    }

    public LatLng getLatLngFromAddress() {
        return latLngFromAddress;
    }

    private class AddressResultReceiver extends ResultReceiver {

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultData.getInt("REVERSE", 0) == 0) {
                //Display the address string or an error message sent from the intent service.
                mAddressOutput = resultData.getString(LocationAPIConstants.RESULT_DATA_KEY);
                Log.i(TAG, "Address: " + mAddressOutput);

                if (resultCode == LocationAPIConstants.SUCCESS_RESULT) {
                    Log.i(TAG, LocationAPIConstants.ADDRESS_FOUND);
                }
                mListener.getOutputAddress(getAddressOutput());
            } else {
                latLngFromAddress = resultData.getParcelable(LocationAPIConstants.RESULT_DATA_KEY);
                mListener.getLatlngAddress(getLatLngFromAddress());
            }
            mAddressRequested = false;
        }
    }
}