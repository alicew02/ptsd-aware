package com.teamcasey.watchdog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A class for creating a database on the watch if one doesn't exist.
 *
 * Use getInstance() to get the singleton object across all watch activities
 * NEVER USE new WearDatabaseHelper. ALWAYS use getInstance()
 */
public class WearDatabaseHelper extends SQLiteOpenHelper {
    private static WearDatabaseHelper dbHelperInstance;

    public static abstract class HeartRateTableConstants {
        public static final String TABLE_NAME = "HeartRate";
        public static final String KEY_ID = "id";
        public static final String COL1 = "bpm";
        public static final String COL2 = "date";
        public static final String COL3 = "isWithinCalibratingPeriod";
    };

    public static final String DB_NAME = "Watchdog_DB";
    public static final int VERSION = 1;

    public WearDatabaseHelper(Context context) {
        super(context, "Watchdog_DB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlString = "create table " + HeartRateTableConstants.TABLE_NAME +
                " (" + HeartRateTableConstants.KEY_ID + " integer primary key autoincrement, " +
                HeartRateTableConstants.COL1 + " int not null, " +
                HeartRateTableConstants.COL2 + " datetime default current_timestamp, " +
                HeartRateTableConstants.COL3 + " int not null);";

        db.execSQL(sqlString);
    };

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sqlString = "DROP TABLE IF EXISTS " + HeartRateTableConstants.TABLE_NAME;

        db.execSQL(sqlString);
        onCreate(db);
    };

    //Singleton pattern, always use this method to get an instance of the DB anywhere in the app
    public static synchronized WearDatabaseHelper getInstance(Context context) {
        if (dbHelperInstance == null) {
            dbHelperInstance = new WearDatabaseHelper(context.getApplicationContext());
        }
        return dbHelperInstance;
    }
}