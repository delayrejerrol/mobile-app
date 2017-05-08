package com.jerrol.app.maplocator.googleapis;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

/**
 * Created by Jerrol on 3/15/2017.
 */

public class SignInAPI implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SigninAPI";
    public static final String DISPLAY_NAME = "displayName";
    public static final String EMAIL = "email";
    public static final String PHOTO = "photo";

    private Context mContext;
    public GoogleApiClient mGoogleApiClient;

    public final int RC_SIGN_IN = 9001;
    private ProgressDialog mProgressDialog;

    private String mDisplayName;
    private String mGivenName;
    private String mFamilyName;
    private String mEmail;
    private String mId;
    private Uri mPhoto;

    private boolean isSignedIn;

    private OnRevokeListener mListener;

    public SignInAPI(Context context) {
        mContext = context;

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .enableAutoManage((AppCompatActivity) mContext /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

        if(mContext instanceof OnRevokeListener) {
            mListener = (OnRevokeListener) mContext;
        } else {
            throw new RuntimeException(mContext.toString() + " must implement OnRevokeListener");
        }
        isSignedIn = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    public void onStart() {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(opr.isDone()) {
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(googleSignInResult -> {
                hideProgressDialog();
                handleSignInResult(googleSignInResult);
            });
        }
    }

    // [START handleSignInResult]
    public void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            mDisplayName = acct.getDisplayName();
            mGivenName = acct.getGivenName();
            mFamilyName = acct.getFamilyName();
            mEmail = acct.getEmail();
            mId = acct.getId();
            mPhoto = acct.getPhotoUrl();

            String sPhoto = null;
            if(mPhoto != null) {
                sPhoto = mPhoto.toString();
            }

            Log.i(TAG, "Image URL: " + sPhoto);
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            //updateUI(true);
            isSignedIn = true;
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
            isSignedIn = false;
        }
    }
    // [END handleSignInResult]

    // [START signOut]
    public void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                status -> {
                    // [START_EXCLUDE]
                    //updateUI(false);
                    isSignedIn = false;
                    // [END_EXCLUDE]
                });
    }
    // [END signOut]

    // [START revokeAccess]
    public void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                status -> {
                    // [START_EXCLUDE]
                    //updateUI(false);
                    isSignedIn = false;
                    mListener.onDisconnect();
                    // [END_EXCLUDE]
                });
    }
    // [END revokeAccess]

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public boolean isSignedIn() {
        return isSignedIn;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getEmail() {
        return mEmail;
    }

    public Uri getPhoto() {
        return mPhoto;
    }

    public interface OnRevokeListener {
        void onDisconnect();
    }
}