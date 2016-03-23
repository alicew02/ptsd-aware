package com.teamcasey.watchdog;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by seth on 3/6/16.
 */
public class WearDatabaseShowcaseActivity extends Activity {
    private WearDatabaseHelper databaseHelper = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openDB();
        setContentView(R.layout.wear_database_layout);
        setupButtonListeners();
    }

    private void openDB() {
        this.databaseHelper = new WearDatabaseHelper(this);
    }

    private void setupButtonListeners() {
        final Button addButton = (Button) findViewById(R.id.addrowbtn);
        final Button deleteButton = (Button) findViewById(R.id.deleterowbtn);
        final Button listButton = (Button) findViewById(R.id.listrowbtn);


        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                insertRandomHeartRateRow();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteAllHeartRateRows();
            }
        });

        listButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logCurrentTableStructure();
            }
        });
    }


    private void insertRandomHeartRateRow() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues valuesToInsert = new ContentValues();
        valuesToInsert.put(WearDatabaseHelper.HeartRateTableConstants.COL1, 50);
        valuesToInsert.put(WearDatabaseHelper.HeartRateTableConstants.COL3, 0);

        db.insert(WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME, null, valuesToInsert);
    }

    private void deleteAllHeartRateRows() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.delete(WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME,
                WearDatabaseHelper.HeartRateTableConstants.COL1 + " >= 50;", null);
    }

    private void logCurrentTableStructure() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String[] columns = {WearDatabaseHelper.HeartRateTableConstants.KEY_ID,
                WearDatabaseHelper.HeartRateTableConstants.COL1,
                WearDatabaseHelper.HeartRateTableConstants.COL2};

        Cursor returnedRows = db.query(WearDatabaseHelper.HeartRateTableConstants.TABLE_NAME, columns, null, null, null, null, null);

        returnedRows.moveToFirst();

        while (returnedRows.isAfterLast() == false) {
            String id = returnedRows.getString(0);
            String bpm = returnedRows.getString(1);
            String date = returnedRows.getString(2);

            System.out.println(id + " " + bpm + " " + date);

            returnedRows.moveToNext();
        }
    }
}