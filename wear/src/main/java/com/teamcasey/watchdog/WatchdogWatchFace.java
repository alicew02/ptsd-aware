package com.teamcasey.watchdog;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

public class WatchdogWatchFace extends CanvasWatchFaceService {
    private static final String TAG = "watchdog_watch_face";

    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    protected class Engine extends CanvasWatchFaceService.Engine implements SensorEventListener {
        //fields for drawing
        //copy-pasted from example, don't touch unless you understand what you're doing
        private Paint textPaintForDrawing;
        private final Paint peekCardBackgroundPaintForDrawing = new Paint();
        private float xOffsetForDrawing;
        private float yOffsetForDrawing;
        private float textSpacingHeightForDrawing;
        private int screenTextColorForDrawing = Color.WHITE;
        private final Rect cardBounds = new Rect();
        private boolean lowBitAmbientForDrawing;
        private Drawable heartIconForDrawing;
        private Drawable offHeartIconForDrawing;

        //fields for sensors
        private Sensor heartRateSensor;
        private SensorManager sensorManager;

        //fields for heartrate
        private static final int numberOfRecordsForBaseline = 5000;
        private int baselineHeartRate;
        private int currentHeartRate;
        private Long currentRowCount;
        private int recordsUntilPrompt;
        private boolean currentlyMonitoringHeartRate = false;


        /**
         * Lifecycle method that gets called when the watch face is created
         *
         * @param holder -> previous data
         */
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            // Accepts tap events via WatchFaceStyle (setAcceptsTapEvents(true)).
            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchdogWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(true)
                    .setAcceptsTapEvents(true)
                    .build());

            setupFieldsForDrawingAndTouchingAndHeartRate();
        }

        /**
         * Called from onCreate(), initial field setup
         */
        private void setupFieldsForDrawingAndTouchingAndHeartRate() {
            Resources resources = WatchdogWatchFace.this.getResources();

            textSpacingHeightForDrawing = resources.getDimension(R.dimen.interactive_text_size);

            textPaintForDrawing = new Paint();
            textPaintForDrawing.setColor(screenTextColorForDrawing);
            textPaintForDrawing.setTypeface(BOLD_TYPEFACE);
            textPaintForDrawing.setAntiAlias(true);

            currentHeartRate = 0;
            recordsUntilPrompt = 0;

            //get the current # of heart rate entries, will calculate baseline if necessary
            this.currentRowCount = new Long(0);
            new CountHeartRateRows().execute();
        }

        /**
         * Lifecycle method for dynamically resizing based on insets
         * From example code, didn't write this
         *
         * @param insets
         */
        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            /** Loads offsets / text size based on device type (square vs. round). */
            Resources resources = WatchdogWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            xOffsetForDrawing = resources.getDimension(
                    isRound ? R.dimen.interactive_x_offset_round : R.dimen.interactive_x_offset);
            yOffsetForDrawing = resources.getDimension(
                    isRound ? R.dimen.interactive_y_offset_round : R.dimen.interactive_y_offset);

            float textSize = resources.getDimension(
                    isRound ? R.dimen.interactive_text_size_round : R.dimen.interactive_text_size);

            textPaintForDrawing.setTextSize(textSize);

            heartIconForDrawing = resources.getDrawable(R.drawable.heart, null);
            offHeartIconForDrawing = resources.getDrawable(R.drawable.off_heart, null);
            int height = heartIconForDrawing.getMinimumHeight()+25;
            int width = heartIconForDrawing.getMinimumWidth()+41;
            int left = (int) xOffsetForDrawing-21;
            int right = (int) xOffsetForDrawing + width;
            int top = (int) yOffsetForDrawing-25;
            int bottom = (int) yOffsetForDrawing + height;
            heartIconForDrawing.setBounds(new Rect(left, top, right, bottom));
            offHeartIconForDrawing.setBounds(new Rect(left, top, right, bottom));
        }

        /**
         * Lifecycle method for dealing with notifications
         * Fromm example code, didn't write this
         *
         * @param bounds
         */
        @Override
        public void onPeekCardPositionUpdate(Rect bounds) {
            super.onPeekCardPositionUpdate(bounds);

            if (!bounds.equals(cardBounds)) {
                cardBounds.set(bounds);
                invalidate();
            }
        }

        /**
         * Lifecycle method for screen props changing
         * From example code, didn't write this
         *
         * @param properties
         */
        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            textPaintForDrawing.setTypeface(burnInProtection ? NORMAL_TYPEFACE : BOLD_TYPEFACE);

            lowBitAmbientForDrawing = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        /**
         * Lifecycle method for dealing with low ambient mode
         * From example code, didn't write this
         *
         * @param inAmbientMode
         */
        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (lowBitAmbientForDrawing) {
                boolean antiAlias = !inAmbientMode;
                textPaintForDrawing.setAntiAlias(antiAlias);
            }
            invalidate();
        }

        /*
         * Captures tap event, type and screen location
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                //Up event
                case WatchFaceService.TAP_TYPE_TAP:
                    break;

                //Down event
                case WatchFaceService.TAP_TYPE_TOUCH:
                    if (heartIconForDrawing.getBounds().contains(x, y)) {
                        if (this.currentlyMonitoringHeartRate) {
                            this.disableHeartRateMonitoring();
                        } else {
                            this.setupHeartRateMonitoring();
                        }

                        invalidate();
                    }
                    break;
            }
        }

        /**
         * System call that auto runs every time a new minute ticks internally
         * Forces a screen redraw so time can update
         * Every five minute updates rowcount which may also update baseline
         */
        @Override
        public void onTimeTick() {
            super.onTimeTick();

            if (AndroidSystemHelper.isCurrentTimeDivisibleByFive()) {
                new CountHeartRateRows().execute();
            }

            invalidate();
        }

        /**
         * Runs whenever invalidate() is called, redraws the watch face appropriately
         *
         * @param canvas -> canvas object on which to draw
         * @param bounds -> bounds of the canvas depending on square/round watchface
         */
        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            //set background color
            canvas.drawColor(Color.BLACK);

            if (this.currentlyMonitoringHeartRate) {
                this.heartIconForDrawing.draw(canvas);

                if (this.hasEnoughRowsForBaseline()) {
                    canvas.drawText(
                            "HR: " + currentHeartRate,
                            xOffsetForDrawing,
                            yOffsetForDrawing + (textSpacingHeightForDrawing * 2),
                            textPaintForDrawing);
                } else {
                    Long recordsToCalibrated = Engine.numberOfRecordsForBaseline - this.currentRowCount;

                    canvas.drawText(
                            recordsToCalibrated + " left",
                            xOffsetForDrawing,
                            yOffsetForDrawing + (textSpacingHeightForDrawing * 2),
                            textPaintForDrawing);
                }
            } else {
                this.offHeartIconForDrawing.draw(canvas);

                canvas.drawText(
                        "  Touch",
                        xOffsetForDrawing,
                        yOffsetForDrawing + (textSpacingHeightForDrawing * 2),
                        textPaintForDrawing);
            }

            /** Covers area under peek card */
            if (isInAmbientMode()) {
                canvas.drawRect(cardBounds, peekCardBackgroundPaintForDrawing);
            }
        }

        /**
         * Store the heartrate change and prompt the user to relax if above their baseline
         *
         * @param event from API
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.values[0] > 0) {
                this.currentHeartRate = (int) event.values[0];
                new InsertHeartRateRow().execute((int) event.values[0]);

                invalidate();

                if (userIsAboveHeartRateThreshold()) {
                    if (this.recordsUntilPrompt == 0) {
                        this.askUserIfTheyWishToRelax();
                        this.recordsUntilPrompt = 10;
                    } else {
                        this.recordsUntilPrompt--;
                    }
                } else {
                    this.recordsUntilPrompt = 10;
                }
            }
        }

        /**
         * Returns whether or not the user is above their heartrate threshold
         * Calculates the heartrate threshold to be 144% of the baseline
         */
        private boolean userIsAboveHeartRateThreshold() {
            if (this.hasEnoughRowsForBaseline()) {
                return (this.currentHeartRate >= ((float)this.baselineHeartRate * 1.44));
            } else {
                return false;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Filler, needs to be implemented
        }

        /**
         * Runs whenever the watch face becomes visible/invisible
         *
         * @param visible -> true if visible false otherwise
         */
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                setupHeartRateMonitoring();
            } else {
                disableHeartRateMonitoring();
            }
        }

        /**
         * Creates the sensors and starts listening for heart rate changes
         */
        private void setupHeartRateMonitoring() {
            this.sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
            this.heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            this.sensorManager.registerListener(this, this.heartRateSensor, 1);
            this.currentlyMonitoringHeartRate = true;
        }

        /**
         * Shuts the hr sensor off
         */
        private void disableHeartRateMonitoring() {
            this.sensorManager.unregisterListener(this);
            this.currentlyMonitoringHeartRate = false;
        }

        /**
         * Creates an alert dialog asking if the user wishes to relax
         */
        private void askUserIfTheyWishToRelax() {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            //param denotes how many ms to vibrate for
            vibrator.vibrate(1000);
        }

        /**
         * Setter for this.currentRowCount
         * Attempts to update the baseline if enough rows exist (>=5000)
         *
         * Should only get called every 5 minutes via Engine.timeTick() system call
         */
        private void updateRowCount(Long rowCount) {
            this.currentRowCount = rowCount;
            recalculateBaselineIfNecessary();
        }

        /**
         * Setter for this.baselineHeartRate
         */
        private void updateBaselineHeartRate(int baseline) {
            this.baselineHeartRate = baseline;
        }


        /**
         * @return true if >= 5000 rows in the database or false otherwise
         */
        private boolean hasEnoughRowsForBaseline() {
            return this.currentRowCount >= Engine.numberOfRecordsForBaseline;
        }

        /**
         * Recalculates the baseline heartrate if enough rows exist to do so
         *
         * Should only get called every 5 minutes via Engine.timeTick() system call
         */
        private void recalculateBaselineIfNecessary() {
            if (this.hasEnoughRowsForBaseline()) {
                new CalculateBaselineHeartRate().execute();
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
         * A class for counting how many current heart rate rows are in the database and
         * passes that number to updateRowCount()
         *
         * @return Long -> # of rows in the database
         */
        private class CountHeartRateRows extends AsyncTask<Void, Void, Long> {
            @Override
            protected Long doInBackground(Void... nothing) {
                SQLiteDatabase db = WearDatabaseHelper.getInstance(getApplicationContext()).getReadableDatabase();

                return DatabaseUtils.queryNumEntries(db, WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME);
            }

            @Override
            protected void onPostExecute(Long rowCount) {
                WatchdogWatchFace.Engine.this.updateRowCount(rowCount);
            }
        }

        /**
         * Calculate the average heart rate using sql and passes it to updateBaselineHeartRate
         *
         * @return int -> the baseline heartrate
         */
        private class CalculateBaselineHeartRate extends AsyncTask<Void, Void, Integer> {
            @Override
            protected Integer doInBackground(Void... nothing) {
                SQLiteDatabase db = WearDatabaseHelper.getInstance(getApplicationContext()).getReadableDatabase();

                Cursor result = db.rawQuery("SELECT AVG(" + WearDatabaseHelper.HeartRateTableConstants.COL1 + ") from " + WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME, null);

                if (result.moveToFirst()) {
                    return result.getInt(0);
                } else {
                    return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer baseline) {
                WatchdogWatchFace.Engine.this.updateBaselineHeartRate(baseline);
            }
        }
    }
}