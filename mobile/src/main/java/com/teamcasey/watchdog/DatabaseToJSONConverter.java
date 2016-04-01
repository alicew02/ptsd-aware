package com.teamcasey.watchdog;

import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by seth on 4/1/16.
 */
public class DatabaseToJSONConverter {
    Cursor cursor;

    public DatabaseToJSONConverter(Cursor cursor) {
        this.cursor = cursor;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject pl = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        this.cursor.moveToFirst();

        while (this.cursor.isAfterLast() == false) {
            String id = this.cursor.getString(0);
            String bpm = this.cursor.getString(1);
            String date = this.cursor.getString(2);
            String isInCalibratingPeriod = this.cursor.getString(3);

            JSONObject val = new JSONObject();

            try {
                val.put("id", id);
                val.put("bpm", bpm);
                val.put("date", date);
                val.put("isInCalibrating", isInCalibratingPeriod);

                jsonArray.put(val);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            this.cursor.moveToNext();
        }

        pl.put("rows", jsonArray);

        return pl;
    }
}
