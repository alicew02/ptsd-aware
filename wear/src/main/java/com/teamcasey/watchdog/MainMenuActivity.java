package com.teamcasey.watchdog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

/**
 * Created by seth on 4/8/16.
 */
public class MainMenuActivity extends Activity implements SensorEventListener {
    private static final String TAG = MainMenuActivity.class.getName();

    private static final int maxRate = 80;
    private TextView rate;
    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;
    private boolean inDistraction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baselining);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                rate = (TextView) stub.findViewById(R.id.rate);
            }
        });

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > 0) {
            Log.d(TAG, "sensor event: " + event.accuracy + " = " + event.values[0]);

            if (!inDistraction) {
                rate.setText(String.valueOf(event.values[0]));
            }

            if (this.maxRate < event.values[0]) {
                Log.d(TAG, "sensor event: Heart rate higher than allowed rate");
                askUserIfTheyWishToRelax();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Filler
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSensorManager.registerListener(this, this.mHeartRateSensor, 3);
    }

    private void askUserIfTheyWishToRelax() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Log.d(TAG, "User clicked yes");
                        launchNewApplication();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Log.d(TAG, "User clicked no");
                        inDistraction = false;
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
        builder.setMessage("Engage in a distraction?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void launchNewApplication() {
        Intent intent = new Intent(Intent.ACTION_MAIN);

        String messageToSend = "random data";
        Intent chooser = Intent.createChooser(intent, messageToSend);

        startActivity(chooser);
    }
}
