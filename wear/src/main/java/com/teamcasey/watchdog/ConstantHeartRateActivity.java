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
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * The main screen that displays when launching the application. Will display heart rate,
 * communicate with google apis, and notify user if they want to relax
 */
public class ConstantHeartRateActivity extends Activity {
    private static final String TAG = ConstantHeartRateActivity.class.getName();
    private ImageButton logoButton;
    String datapath = "/message_path";

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
                //get the UI elements here

                logoButton = (ImageButton) findViewById(R.id.logoButton);
                //we need to wait for the view to be populated before we can do this
                //aka we need to do this in this handler instead of in onStart()
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
        launchDialogChooser();
    }


    /*
     * Listen on the buttons to manipulate the database
     */
    private void setupButtonListeners() {
        logoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               launchDialogChooser();
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
     * Creates an alert dialog asking if the user wishes to relax
     */
    private void launchDialogChooser() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        //param denotes how many ms to vibrate for
        vibrator.vibrate(5000);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Log.d(TAG, "User clicked yes");
                        dialog.dismiss();
                        launchOnPhoneOrWatchChooser();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Log.d(TAG, "User clicked no");
                        dialog.dismiss();
                        killApplication();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ConstantHeartRateActivity.this);
        builder.setMessage("Engage in a distraction?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void launchOnPhoneOrWatchChooser() {
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
                        dialog.dismiss();
                        new MessageSendingThread(TAG, datapath, "Start phone app", GoogleConnection.getInstance(getApplicationContext())).start();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ConstantHeartRateActivity.this);
        builder.setMessage("Launch from phone or watch?").setPositiveButton("Watch", dialogClickListener)
                .setNegativeButton("Phone", dialogClickListener).show();
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

    private void killApplication() {
        this.finish();
        System.exit(0);
    }
}
