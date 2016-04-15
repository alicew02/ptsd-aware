package com.teamcasey.watchdog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A class that allows us to receive messages from a local broadcaster across the application
 * regardless of what activity this is used in
 */
public class MessageReceiver extends BroadcastReceiver {
    //Store all different message type paths here, make sure they match phone app's path names
    public static String heartRateDatabasePath = "/message_path";

    private String TAG;
    private String datapath;
    private Context context;

    public MessageReceiver(String TAG, String datapath, Context context) {
        this.TAG = TAG;
        this.datapath = datapath;
        this.context = context;
    }

    /**
     * Automatically run under the hood when a message comes in, setup to populate a toast
     * with the message and then reply atm
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        AndroidSystemHelper.makeToast(context, message);

        new MessageSendingThread(TAG, datapath, "hello phone device", GoogleConnection.getInstance(context)).start();
    }
}

