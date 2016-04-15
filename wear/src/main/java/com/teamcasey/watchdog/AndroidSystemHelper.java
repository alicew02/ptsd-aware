package com.teamcasey.watchdog;

import android.content.Context;
import android.widget.Toast;

/**
 * Put all helpers that can be static in here so that they don't clutter our main activities
 */
public class AndroidSystemHelper {

    /*
     * Makes a toast notification, takes a context which you can get via getApplicationContext()
     * and a string to display in the toast
     */
    public static void makeToast(Context context, String message) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}
