package com.teamcasey.watchdog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Handler;
import android.os.Message;
import android.content.IntentFilter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by seth on 3/6/16.
 */
public class DatabaseShowcaseActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    final public DatabaseHelper database = new DatabaseHelper(this);
    GoogleApiClient googleClient;
    String datapath = "/message_path";
    Handler handler;
    String TAG = "Mobile DatabaseShowcaseActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.database_layout);
        setupButtonListeners();
        setupMessaging();
    }

    //Getter such that the async tasks can get the database
    public DatabaseHelper getDatabase() {
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
                new SendThread(datapath, "Hello wearable device").start();
                new GetHeartRateRows().execute();
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
        DatabaseToJSONConverter converter = new DatabaseToJSONConverter(returnedRows);

        JSONObject results = null;

        try {
            results = converter.getJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(results.toString());
        new JSONToDatabaseConverter(results, this.getDatabase());
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
            SQLiteDatabase db = DatabaseShowcaseActivity.this.getDatabase().getWritableDatabase();
            Long[] insertedRowIDs = new Long[heartRates.length];
            int counter = 0;

            for (Integer heartRate : heartRates) {
                ContentValues valuesToInsert = new ContentValues();
                valuesToInsert.put(DatabaseHelper.HeartRateTableConstants.COL1, heartRate);
                valuesToInsert.put(DatabaseHelper.HeartRateTableConstants.COL3, 1);

                Long insertedRowID = db.insert(DatabaseHelper.HeartRateTableConstants.TABLE_NAME, null, valuesToInsert);

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
            SQLiteDatabase db = DatabaseShowcaseActivity.this.getDatabase().getWritableDatabase();
            Long[] insertedRowIDs = new Long[heartRates.length];
            int counter = 0;

            for (Integer heartRate : heartRates) {
                ContentValues valuesToInsert = new ContentValues();
                valuesToInsert.put(DatabaseHelper.HeartRateTableConstants.COL1, heartRate);
                valuesToInsert.put(DatabaseHelper.HeartRateTableConstants.COL3, 0);

                Long insertedRowID = db.insert(DatabaseHelper.HeartRateTableConstants.TABLE_NAME, null, valuesToInsert);

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
            SQLiteDatabase db = DatabaseShowcaseActivity.this.getDatabase().getWritableDatabase();

            db.delete(DatabaseHelper.HeartRateTableConstants.TABLE_NAME,
                    DatabaseHelper.HeartRateTableConstants.COL1 + " >= 0;", null);

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
            SQLiteDatabase db = DatabaseShowcaseActivity.this.getDatabase().getWritableDatabase();

            String[] columns = {DatabaseHelper.HeartRateTableConstants.KEY_ID,
                    DatabaseHelper.HeartRateTableConstants.COL1,
                    DatabaseHelper.HeartRateTableConstants.COL2,
                    DatabaseHelper.HeartRateTableConstants.COL3};

            return db.query(DatabaseHelper.HeartRateTableConstants.TABLE_NAME, columns, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor returnedRows) {
            DatabaseShowcaseActivity.this.logCurrentTableStructure(returnedRows);
        }
    }

    //sets up the google api client such that we can send and recieve messages from the watch
    private void setupMessaging() {
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                System.out.println(stuff.getString("logthis"));
                return true;
            }
        });

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    //setup a broadcast receiver to receive the messages from the wear device via the listenerService.
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.v(TAG, "Main activity received message: " + message);

            System.out.println(message);
        }
    }

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    // Send a message when the data layer connection is successful.
    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("successfully connected - mobile");
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection suspended - mobile");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connection failed - mobile");
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
