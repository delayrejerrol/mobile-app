package com.jerrol.app.smsobserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Jerrol on 5/7/2017.
 */

public class SMSReceiver extends BroadcastReceiver {

    public static final String TAG = "SMSReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        /*try {
            Log.d(TAG, "SMSReceive Trigger");
            if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Log.d(TAG, "Incoming Message");
            } else if (intent.getAction().equals("android.provider.Telephony.SMS_SENT")) {
                Log.d(TAG, "Outgoing SMS");
            }
        } catch (Exception e) {
            Log.e(TAG, "onReceiver method cannot be processed");
        }*/
        Log.i(TAG, "" + intent.getAction());
        Intent i = new Intent(context, SMSService.class);
        //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //i.addCategory(Intent.CATEGORY_LAUNCHER);
        //context.startActivity(i);
        context.startService(i);
    }
}
