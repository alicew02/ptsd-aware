package com.teamcasey.watchdog;

import android.content.Context;
import android.widget.Toast;

import java.util.Calendar;

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

    /**
     * Returns true if the current time is divisible by 5 evenly and false otherwise
     *
     * @return boolean -> true if time is divisible by 5, false otherwise
     */
    public static boolean isCurrentTimeDivisibleByFive() {
        return (Calendar.getInstance().get(Calendar.MINUTE) % 5) == 0;
    }
}
