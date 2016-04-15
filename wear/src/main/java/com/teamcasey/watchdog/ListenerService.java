package com.teamcasey.watchdog;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/*
  *  This is a listener on the mobile device to get messages via
  *  the datalayer and then pass it to a broadcaster  so it can be
  *  displayed.   the messages should be coming from the device/phone.
  *
  *  NOTE: has to be declared as a listener in AndroidManifest.xml
 */
public class ListenerService extends WearableListenerService {
    String TAG = "wear listener";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        //each message will have a path that allows us to determine where it comes from
        if (messageEvent.getPath().equals(MessageReceiver.heartRateDatabasePath)) {
            final String message = new String(messageEvent.getData());

            // Broadcast message so other activities can see it
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}