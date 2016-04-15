package com.teamcasey.watchdog;

import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * A thread to send messages such that we don't have these message sending actions clogging
 * up our main UI thread.
 */
public class MessageSendingThread extends Thread {
    private String TAG;
    private String path;
    private String message;
    private GoogleConnection connection;

    /**
     * Constructor
     *
     * @param TAG - the tag name of the activity/class you are sending this message from
     * @param path - the path of the type of message as defined in MessageReceiver
     * @param msg - the content of the message
     * @param connection - an instance of GoogleConnection
     */
    public MessageSendingThread(String TAG, String path, String msg, GoogleConnection connection) {
        this.TAG = TAG;
        this.path = path;
        this.message = msg;
        this.connection = connection;
    }

    /**
     * Sends the message, will log whether or not the message was successfully sent
     */
    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(connection.getClient()).await();
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(connection.getClient(), node.getId(), path, message.getBytes()).await();
            if (result.getStatus().isSuccess()) {
                Log.v(TAG, "SendThread: message send to " + node.getDisplayName());
            } else {
                Log.v(TAG, "SendThread: message failed to" + node.getDisplayName());
            }
        }
    }
}
