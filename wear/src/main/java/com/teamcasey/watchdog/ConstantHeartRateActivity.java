package com.teamcasey.watchdog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * The main screen that displays when launching the application. Will display heart rate,
 * communicate with google apis, and notify user if they want to relax
 */
public class ConstantHeartRateActivity extends Activity implements SensorEventListener {
    private static final String TAG = ConstantHeartRateActivity.class.getName();
    private static final int maxRate = 80;

    private TextView rate;
    private Button deleteRowsButton;
    private Button listRowsButton;
    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;
    private boolean inDistraction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baselining);

        //This will grab the correct layout for either the round or the square watchfaces
        //TODO add layout for square watchface with same elements
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                rate = (TextView) stub.findViewById(R.id.rate);
                deleteRowsButton = (Button) stub.findViewById(R.id.deleterowbtn);
                listRowsButton = (Button) stub.findViewById(R.id.listrowbtn);

                //we need to wait for the view to be populated before we can do this
                //aka we need to do this in this handler instead of in onStart()
                setupSensors();
                setupButtonListeners();
                setupMessageListener();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //start the connection to the google server
        GoogleConnection.getInstance(getApplicationContext()).connect();
    }

    /*
     * Creates the sensor manager and heart sensor and begins tracking heart rate
     */
    private void setupSensors() {
        this.mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        this.mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        //listen for heart rate changes at a rate of 3
        this.mSensorManager.registerListener(this, this.mHeartRateSensor, 3);
    }

    /*
     * Listen on the buttons to manipulate the database
     * TODO delete this eventually
     */
    private void setupButtonListeners() {
        this.deleteRowsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new DeleteHeartRateRow().execute();
            }
        });

        this.listRowsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new GetHeartRateRows().execute();
                new MessageSendingThread(TAG, MessageReceiver.heartRateDatabasePath, "hello mobile device - from wear", GoogleConnection.getInstance(getApplicationContext())).start();
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
            SQLiteDatabase db = WearDatabaseHelper.getInstance(getApplicationContext()).getWritableDatabase();
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
            SQLiteDatabase db = WearDatabaseHelper.getInstance(getApplicationContext()).getWritableDatabase();
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
            SQLiteDatabase db = WearDatabaseHelper.getInstance(getApplicationContext()).getWritableDatabase();

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
            SQLiteDatabase db = WearDatabaseHelper.getInstance(getApplicationContext()).getWritableDatabase();

            String[] columns = {WearDatabaseHelper
                    .HeartRateTableConstants.KEY_ID,
                    WearDatabaseHelper.HeartRateTableConstants.COL1,
                    WearDatabaseHelper.HeartRateTableConstants.COL2};

            return db.query(WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME, columns, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor returnedRows) {
            ConstantHeartRateActivity.this.logCurrentTableStructure(returnedRows);
        }
    }

    /**
     * Listeners on the local broadcast manager for messages coming in from the phone
     * Handlers the actual messages in the MessageReceiver class
     */
    private void setupMessageListener() {
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver(TAG, MessageReceiver.heartRateDatabasePath, getApplicationContext());
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    /**
     * Whenever the heart rate monitor changes and the user isn't being prompted to relax
     * store the data in the database and change the rate textview
     *
     * @param event from API
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > 0) {
            if (!this.inDistraction) {
                this.rate.setText(String.valueOf(event.values[0]));
                new InsertHeartRateRow().execute((int) event.values[0]);
            }

            if (this.maxRate < event.values[0]) {

                // Pinging the android app...
                // Likely does not work
                Intent messageIntent = new Intent();
                messageIntent.setAction(Intent.ACTION_SEND);
                messageIntent.putExtra("/message_path", "");
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);

                askUserIfTheyWishToRelax();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Filler
    }

    /**
     * Creates an alert dialog asking if the user wishes to relax
     */
    private void askUserIfTheyWishToRelax() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Log.d(TAG, "User clicked yes");
                        dialog.dismiss();
                        launchNewApplication();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Log.d(TAG, "User clicked no");
                        inDistraction = false;
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ConstantHeartRateActivity.this);
        builder.setMessage("Engage in a distraction?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    /**
     * Launch the chooser which allows users to pick a distracting app
     * TODO flesh this out
     */
    private void launchNewApplication() {
        Intent intent = new Intent(Intent.ACTION_MAIN);

        String messageToSend = "";
        Intent chooser = Intent.createChooser(intent, messageToSend);

        startActivity(chooser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSensorManager.unregisterListener(this);
    }
}
