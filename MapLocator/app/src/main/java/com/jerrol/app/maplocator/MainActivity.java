package com.jerrol.app.maplocator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.jerrol.app.maplocator.googleapis.SignInAPI;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SignInAPI.OnRevokeListener {

    private static final String TAG = "MainActivity";

    @BindString(R.string.signed_in_as) String signInAs;
    @BindView(R.id.btn_sign_in) SignInButton mBtnSignIn;
    @BindView(R.id.btn_disconnect) Button mBtnDisconnect;
    @BindView(R.id.tv_display_name) TextView mTextViewDisplayName;
    SignInAPI signInAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        signInAPI = new SignInAPI(this);
        mBtnSignIn.setSize(SignInButton.SIZE_STANDARD);
    }

    @OnClick(R.id.btn_sign_in)
    public void signIn_GoogleAccount(View view) {
        Log.i("MainActivity", "signInGoogle called");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(signInAPI.mGoogleApiClient);
        startActivityForResult(signInIntent, signInAPI.RC_SIGN_IN);
    }

    @OnClick(R.id.fab_start)
    public void startGoogleMap(View view) {
        Intent intent = new Intent(MainActivity.this, GoogleMapActivity.class);
        if(signInAPI.isSignedIn()) {
            Bundle bundle = new Bundle();
            bundle.putString(SignInAPI.DISPLAY_NAME, signInAPI.getDisplayName());
            bundle.putString(SignInAPI.EMAIL, signInAPI.getEmail());
            bundle.putParcelable(SignInAPI.PHOTO, signInAPI.getPhoto());
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    @OnClick(R.id.btn_disconnect)
    public void signOut_GoogleAccount(View view) {
        signInAPI.revokeAccess();
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == signInAPI.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            signInAPI.handleSignInResult(result);
            updateUI(signInAPI.isSignedIn());
        }
    }
    // [END onActivityResult]


    @Override
    protected void onStart() {
        super.onStart();

        signInAPI.onStart();
        updateUI(signInAPI.isSignedIn());
    }

    private void updateUI(boolean isSignedIn) {
        if(isSignedIn) {
            mBtnSignIn.setVisibility(View.GONE);
            mBtnDisconnect.setVisibility(View.VISIBLE);
            mTextViewDisplayName.setText(String.format(signInAs, signInAPI.getDisplayName()));
        } else {
            mBtnSignIn.setVisibility(View.VISIBLE);
            mBtnDisconnect.setVisibility(View.GONE);
            mTextViewDisplayName.setText("");
        }
    }

    @Override
    public void onDisconnect() {
        updateUI(signInAPI.isSignedIn());
    }
}
