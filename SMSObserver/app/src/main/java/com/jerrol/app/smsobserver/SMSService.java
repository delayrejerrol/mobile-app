package com.jerrol.app.smsobserver;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Jerrol on 5/7/2017.
 */

public class SMSService extends Service {
    private static final String TAG = "SMSService";

    public SMSService() {

    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called");
        ContentObserver co = new SMSObserver(new Handler(), getApplicationContext());
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, co);

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
        sendBroadcast(new Intent(getApplicationContext(), SMSReceiver.class));
        //super.onDestroy();
    }
}
