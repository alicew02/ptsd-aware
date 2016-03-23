package com.teamcasey.watchdog;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by seth on 3/6/16.
 */
public class DatabaseShowcaseActivity extends Activity {
    final public DatabaseHelper database = new DatabaseHelper(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.database_layout);
        setupButtonListeners();
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
                    DatabaseHelper.HeartRateTableConstants.COL2};

            return db.query(DatabaseHelper.HeartRateTableConstants.TABLE_NAME, columns, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor returnedRows) {
            DatabaseShowcaseActivity.this.logCurrentTableStructure(returnedRows);
        }
    }
}
