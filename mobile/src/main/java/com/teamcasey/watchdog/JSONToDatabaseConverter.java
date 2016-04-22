package com.teamcasey.watchdog;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by seth on 4/1/16.
 */
public class JSONToDatabaseConverter {
    private JSONObject jsonObject;
    private DatabaseHelper database;
    private ArrayList<Integer> currentIDs = new ArrayList<Integer>();


    public JSONToDatabaseConverter(JSONObject jsonObject, DatabaseHelper database) {
        this.jsonObject = jsonObject;
        this.database = database;

        populateCurrentIDsArray();
        populateDatabaseWithNewRows();
    }

    private void populateCurrentIDsArray() {
        SQLiteDatabase db = this.database.getWritableDatabase();

        String[] columns = {DatabaseHelper.HeartRateTableConstants.KEY_ID};

        Cursor cursor = db.query(DatabaseHelper.HeartRateTableConstants.TABLE_NAME, columns, null, null, null, null, null);

        walkthroughReturnedRowsAndPopulate(cursor);
    }

    private void walkthroughReturnedRowsAndPopulate(Cursor cursor) {
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            Integer id = Integer.parseInt(cursor.getString(0));

            this.currentIDs.add(id);
            cursor.moveToNext();
        }
    }

    private void populateDatabaseWithNewRows() {
        JSONArray rows = null;

        try {
            rows = this.jsonObject.getJSONArray("rows");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = null;

            try {
                row = rows.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            String idString = null;

            try {
                idString = row.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            Integer id = Integer.parseInt(idString);

            if (!this.currentIDs.contains(id)) {
                addRowToDatabase(row);
            }
        }
    }

    private void addRowToDatabase(JSONObject row) {
        String idString = null;
        String bpmString = null;
        String date = null;
        String isInCalibratingString = null;

        try {
            idString = row.getString("id");
            bpmString = row.getString("bpm");
            date = row.getString("date");
            isInCalibratingString = row.getString("isInCalibrating");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        ContentValues valuesToInsert = new ContentValues();
        valuesToInsert.put(DatabaseHelper.HeartRateTableConstants.KEY_ID, Integer.parseInt(idString));
        valuesToInsert.put(DatabaseHelper.HeartRateTableConstants.COL1, Integer.parseInt(bpmString));
        valuesToInsert.put(DatabaseHelper.HeartRateTableConstants.COL2, date);
        valuesToInsert.put(DatabaseHelper.HeartRateTableConstants.COL3, Integer.parseInt(isInCalibratingString));

        this.database.getWritableDatabase().insert(DatabaseHelper.HeartRateTableConstants.TABLE_NAME, null, valuesToInsert);
    }
}
