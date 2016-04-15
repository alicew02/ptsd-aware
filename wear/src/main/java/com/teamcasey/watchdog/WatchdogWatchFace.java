package com.teamcasey.watchdog;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

    private class Engine extends CanvasWatchFaceService.Engine implements SensorEventListener {
        private Paint mTextPaint;
        private final Paint mPeekCardBackgroundPaint = new Paint();

        private float mXOffset;
        private float mYOffset;
        private float mTextSpacingHeight;
        private int mScreenTextColor = Color.WHITE;

        private int mTouchCoordinateX;
        private int mTouchCoordinateY;
        private String mCurrentHeartRate;

        private Sensor mHeartRateSensor;
        private SensorManager mSensorManager;

        private final Rect mCardBounds = new Rect();

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            /** Accepts tap events via WatchFaceStyle (setAcceptsTapEvents(true)). */
            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchdogWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(true)
                    .setAcceptsTapEvents(true)
                    .build());

            Resources resources = WatchdogWatchFace.this.getResources();
            mTextSpacingHeight = resources.getDimension(R.dimen.interactive_text_size);

            mTextPaint = new Paint();
            mTextPaint.setColor(mScreenTextColor);
            mTextPaint.setTypeface(BOLD_TYPEFACE);
            mTextPaint.setAntiAlias(true);

            mTouchCoordinateX = 0;
            mTouchCoordinateX = 0;
            mCurrentHeartRate = "0";

            setupHeartRateMonitoring();
        }

        private void setupHeartRateMonitoring() {
            this.mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
            this.mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

            //listen for heart rate changes at a rate of 3
            this.mSensorManager.registerListener(this, this.mHeartRateSensor, 1);
        }


        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            /** Loads offsets / text size based on device type (square vs. round). */
            Resources resources = WatchdogWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(
                    isRound ? R.dimen.interactive_x_offset_round : R.dimen.interactive_x_offset);
            mYOffset = resources.getDimension(
                    isRound ? R.dimen.interactive_y_offset_round : R.dimen.interactive_y_offset);

            float textSize = resources.getDimension(
                    isRound ? R.dimen.interactive_text_size_round : R.dimen.interactive_text_size);

            mTextPaint.setTextSize(textSize);
        }

        @Override
        public void onPeekCardPositionUpdate(Rect bounds) {
            super.onPeekCardPositionUpdate(bounds);

            if (!bounds.equals(mCardBounds)) {
                mCardBounds.set(bounds);
                invalidate();
            }
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mTextPaint.setTypeface(burnInProtection ? NORMAL_TYPEFACE : BOLD_TYPEFACE);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mTextPaint.setAntiAlias(antiAlias);
            }
            invalidate();
        }

        /*
         * Captures tap event (and tap type) and increments correct tap type total.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            mTouchCoordinateX = x;
            mTouchCoordinateY = y;

            switch(tapType) {
                case TAP_TYPE_TOUCH:
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    break;
                case TAP_TYPE_TAP:
                    break;
            }

            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            /** Draws background */
            canvas.drawColor(Color.BLACK);

            canvas.drawText(
                    "HR: " + mCurrentHeartRate,
                    mXOffset,
                    mYOffset + (mTextSpacingHeight),
                    mTextPaint);

            canvas.drawText(
                    "X, Y: " + mTouchCoordinateX + ", " + mTouchCoordinateY,
                    mXOffset,
                    mYOffset + (mTextSpacingHeight * 2),
                    mTextPaint
            );

            /** Covers area under peek card */
            if (isInAmbientMode()) {
                canvas.drawRect(mCardBounds, mPeekCardBackgroundPaint);
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
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
                this.mCurrentHeartRate = String.valueOf(event.values[0]);
                invalidate();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Filler
        }
    }
}