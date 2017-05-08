package com.jerrol.app.maplocator.googleapis;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import android.os.ResultReceiver;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "FetchAddressIS";

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;
    int iReverseMode;

    public FetchAddressIntentService() {
        super(TAG);
    }

    /**
     * Tries to get the location address using a Geocoder. If successful, sends an address to a
     * result receiver. If unsuccessful, sends an error message instead.
     * Note: We define a {@link ResultReceiver} in * MainActivity to process content
     * sent from this service.
     *
     * This service calls this method from the default worker thread with the intent that started
     * the service. When this method returns, the service automatically stops.
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        mReceiver = intent.getParcelableExtra(LocationAPIConstants.RECEIVER);

        // Check if receiver was properly registered.
        if(mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        // Get the location passed to this service through an extra.
        Location location = null;
        String addressLocation = null;

        iReverseMode = intent.getIntExtra("REVERSE", 0);
        if(iReverseMode == 0) {
            location = intent.getParcelableExtra(LocationAPIConstants.LOCATION_DATA_EXTRA);
        } else {
            addressLocation = intent.getStringExtra(LocationAPIConstants.LOCATION_DATA_EXTRA);
            Log.i("FAIS", addressLocation);
        }

        // Make sure that the location data was really sent over through an extra. If it wasn't,
        // send an error error message and return.
        if(location == null && addressLocation == null) {
            errorMessage = LocationAPIConstants.NO_LOCATION_DATA_PROVIDED;
            Log.wtf(TAG, errorMessage);
            deliverResultToReceiver(LocationAPIConstants.FAILURE_RESULT, errorMessage);
            return;
        }

        // Erros could still arise from using the Geocoder (for example, if there is no
        // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
        // simply not have an address for a location. In all these cases, we communicate with the
        // receiver using a resultCode indication failure. If an address is found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are localized for the given
        // Locale, which represents a specific geographical or linguistic region. Locales are used
        // to alter the presentation of information such as numbers or dates to suit the conventions
        // in the region they describe.
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using Geocoder.
        List<Address> addresses = null;

        try {
            if(location != null) {
                // Using getFromLocation() returns an array of Addresses for the area immediately
                // surrounding the given latitude and longitude. The results are a best guess and are
                // not guaranteed to be accurate.
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        // In this sample, we get just a single address.
                        1);
            } else {
                addresses = geocoder.getFromLocationName(addressLocation, 1);
            }
        } catch (IOException ioException) {
            // Catch network or other I/O probles.
            errorMessage = LocationAPIConstants.NO_ADDRESS_FOUND;
            Log.e(TAG, errorMessage);
        } catch (IllegalArgumentException illegalException) {
            // Catch invalid latitude or longitude values.
            errorMessage = LocationAPIConstants.INVALID_LAT_LONG_USED;
            Log.e(TAG, errorMessage + ". " +
            "Latitude = " + location.getLatitude() +
            ", Longitude = " + location.getLongitude(), illegalException);
        }

        //Handle case where no address was found.
        if(addresses == null || addresses.size() == 0) {
            if(errorMessage.isEmpty()) {
                errorMessage = LocationAPIConstants.NO_ADDRESS_FOUND;
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(LocationAPIConstants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragment = new ArrayList<>();

            // Fetch the address lines using {@code getAddressLing},
            // join them, and send them to the thread. The {@link android.location.address}
            // ckass provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)
            if(location != null) {
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragment.add(address.getAddressLine(i));
                }
                deliverResultToReceiver(LocationAPIConstants.SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragment));
            } else {
                deliverResultToReceiverLatLng(LocationAPIConstants.SUCCESS_RESULT, new LatLng(address.getLatitude(), address.getLongitude()));
            }
            Log.i(TAG, LocationAPIConstants.ADDRESS_FOUND);
            Log.i(TAG, addresses.toString());
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(LocationAPIConstants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    private void deliverResultToReceiverLatLng(int resultCode, LatLng latLng) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(LocationAPIConstants.RESULT_DATA_KEY, latLng);
        bundle.putInt("REVERSE", iReverseMode);
        mReceiver.send(resultCode, bundle);
    }
}
