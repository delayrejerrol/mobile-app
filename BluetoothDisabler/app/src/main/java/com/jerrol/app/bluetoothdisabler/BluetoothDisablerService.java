package com.jerrol.app.bluetoothdisabler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

public class BluetoothDisablerService extends Service {
    private static final String TAG = "BDService";
    public BluetoothDisablerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called");
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        boolean isWifi = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if (isConnected && isWifi) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
                Log.i(TAG, "bluetooth disable");
            }
        } else {
            // Check the wifi status again if it is connected or not
            // This issue is comes below API 24.
            AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(this, BluetoothDisablerService.class);
            PendingIntent pi =PendingIntent.getService(getApplicationContext(), 0, alarmIntent, 0);
            am.set(AlarmManager.RTC, System.currentTimeMillis() + (1000 * 60), pi);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy called");
        startService(new Intent(this, BluetoothDisablerService.class));
        //super.onDestroy();
    }
}
