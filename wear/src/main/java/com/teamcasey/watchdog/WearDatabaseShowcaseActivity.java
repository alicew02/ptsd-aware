package com.teamcasey.watchdog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by seth on 3/6/16.
 */
public class WearDatabaseShowcaseActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    final public WearDatabaseHelper database = new WearDatabaseHelper(this);
    GoogleApiClient googleClient;
    private final static String TAG = "Wear MainActivity";
    boolean connected = false;
    String datapath = "/message_path";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wear_database_layout);
        setupButtonListeners();
        setupMessaging();
    }

    //Getter such that the async tasks can get the database
    public WearDatabaseHelper getDatabase() {
        return this.database;
    }

    private void setupButtonListeners() {
        final Button addButton = (Button) findViewById(R.id.addrowbtn);
        final Button deleteButton = (Button) findViewById(R.id.deleterowbtn);
        final Button listButton = (Button) findViewById(R.id.listrowbtn);


        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new InsertHeartRateRow().execute(50, 25, 30);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new DeleteHeartRateRow().execute();
            }
        });

        listButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new GetHeartRateRows().execute();
                new SendThread(datapath, "hello mobile device - from wear").start();
            }
        });
    }

    /**
     * An interfacing method used for passing data from an async task back to this activity
     * via the UI thread
     *
     * @param returnedRows -> Cursor from an async tasks that lets you explore the returned rows
     */
    private void logCurrentTableStructure(Cursor returnedRows) {
        returnedRows.moveToFirst();

        while (returnedRows.isAfterLast() == false) {
            String id = returnedRows.getString(0);
            String bpm = returnedRows.getString(1);
            String date = returnedRows.getString(2);

            System.out.println(id + " " + bpm + " " + date);

            returnedRows.moveToNext();
        }
    }

    /**
     * Class for inserting heart rate rows via the background thread during the calibrating period
     * How to use: Long[] result = new InsertHeartRateRowInCalibratingPeriod().execute(10, 20, 30);
     * Where 10, 20 and 30 above are different heartbeat readings
     *
     * @return Long[] -> a list of inserted heart rate row IDs
     */
    private class InsertHeartRateRowInCalibratingPeriod extends AsyncTask<Integer, Void, Long[]> {
        @Override
        protected Long[] doInBackground(Integer... heartRates) {
            SQLiteDatabase db = WearDatabaseShowcaseActivity.this.getDatabase().getWritableDatabase();
            Long[] insertedRowIDs = new Long[heartRates.length];
            int counter = 0;

            for (Integer heartRate : heartRates) {
                ContentValues valuesToInsert = new ContentValues();
                valuesToInsert.put(WearDatabaseHelper.HeartRateTableConstants.COL1, heartRate);
                valuesToInsert.put(WearDatabaseHelper.HeartRateTableConstants.COL3, 1);

                Long insertedRowID = db.insert(WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME, null, valuesToInsert);

                insertedRowIDs[counter] = insertedRowID;
                counter++;
            }

            return insertedRowIDs;
        }
    }

    /**
     * Class for inserting heart rate rows via the background thread not during the calibrating period
     * How to use: Long[] result = new InsertHeartRateRow().execute(10, 20, 30);
     * Where 10, 20 and 30 above are different heartbeat readings
     *
     * @return Long[] -> a list of inserted heart rate row IDs
     */
    private class InsertHeartRateRow extends AsyncTask<Integer, Void, Long[]> {
        @Override
        protected Long[] doInBackground(Integer... heartRates) {
            SQLiteDatabase db = WearDatabaseShowcaseActivity.this.getDatabase().getWritableDatabase();
            Long[] insertedRowIDs = new Long[heartRates.length];
            int counter = 0;

            for (Integer heartRate : heartRates) {
                ContentValues valuesToInsert = new ContentValues();
                valuesToInsert.put(WearDatabaseHelper.HeartRateTableConstants.COL1, heartRate);
                valuesToInsert.put(WearDatabaseHelper.HeartRateTableConstants.COL3, 0);

                Long insertedRowID = db.insert(WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME, null, valuesToInsert);

                insertedRowIDs[counter] = insertedRowID;
                counter++;
            }

            return insertedRowIDs;
        }
    }

    /**
     * Class for deleting all heart rate rows via the background thread
     *
     * Doesn't return anything
     */
    private class DeleteHeartRateRow extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... nothing) {
            SQLiteDatabase db = WearDatabaseShowcaseActivity.this.getDatabase().getWritableDatabase();

            db.delete(WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME,
                    WearDatabaseHelper.HeartRateTableConstants.COL1 + " >= 0;", null);

            return null;
        }
    }


    /**
     * Class for getting the heart rate rows out of the table, interesting because
     * it shows how to pass data from an async task to the enclosing activity using
     * onPostExecute (which runs on the UI thread instead of in the background)
     */
    private class GetHeartRateRows extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... nothing) {
            SQLiteDatabase db = WearDatabaseShowcaseActivity.this.getDatabase().getWritableDatabase();

            String[] columns = {WearDatabaseHelper
                    .HeartRateTableConstants.KEY_ID,
                    WearDatabaseHelper.HeartRateTableConstants.COL1,
                    WearDatabaseHelper.HeartRateTableConstants.COL2};

            return db.query(WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME, columns, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor returnedRows) {
            WearDatabaseShowcaseActivity.this.logCurrentTableStructure(returnedRows);
        }
    }

    private void setupMessaging() {
        // Register the local broadcast receiver to receive messages from the listener.
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        // Build a new GoogleApiClient that includes the Wearable API
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.v(TAG, "Main activity received message: " + message);

            new SendThread(datapath, "hello phone device").start();
        }
    }

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }


    //needed to send a message back to the device.  hopefully.
    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        connected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connected = false;
    }

    //This actually sends the message to the wearable device.
    class SendThread extends Thread {
        String path;
        String message;

        //constructor
        SendThread(String p, String msg) {
            path = p;
            message = msg;
        }

        //sends the message via the thread.  this will send to all wearables connected, but
        //since there is (should only?) be one, so no problem.
        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v(TAG, "SendThread: message send to "+ node.getDisplayName());

                } else {
                    // Log an error
                    Log.v(TAG, "SendThread: message failed to" + node.getDisplayName());
                }
            }
        }
    }
}
