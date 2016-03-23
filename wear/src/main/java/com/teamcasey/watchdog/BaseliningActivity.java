package com.teamcasey.watchdog;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

public class BaseliningActivity extends Activity implements SensorEventListener {

    private static final String TAG = BaseliningActivity.class.getName();

    private TextView rate;
    private TextView accuracy;
    private TextView sensorInformation;
    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baselining);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                rate = (TextView) stub.findViewById(R.id.rate);
                accuracy = (TextView) stub.findViewById(R.id.accuracy);
                sensorInformation = (TextView) stub.findViewById(R.id.sensor);
            }
        });

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > 0) {
            Log.d(TAG, "sensor event: " + event.accuracy + " = " + event.values[0]);
            rate.setText(String.valueOf(event.values[0]));
            accuracy.setText("Accuracy: " + event.accuracy);
            sensorInformation.setText(event.sensor.toString());
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
}