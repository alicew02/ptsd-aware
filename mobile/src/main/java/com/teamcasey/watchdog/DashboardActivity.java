package com.teamcasey.watchdog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class DashboardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }
    /**
     * called by res/layout/activity_dashboard.xml
     * settings_button: onClick
     */
    public void showSettings(View v) {

        Intent showSettings = new Intent(this, SettingsActivity.class);
        startActivity(showSettings);
    }

    /**
     * Launches a distraction
     * TODO: Listener service for "launch_technique" that calls provided intent string, Organization of many intents
     * called from start_technique_button: onClick
     * @param v
     */
    public void launchTechnique(View v) {
        Uri webpage = Uri.parse("http://iwastesomuchtime.com/toppic.php?sort=week");
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        startActivity(webIntent);
    }

    /**
     * called by res/layout/activity_dashboard.xml
     * settings_button: onClick
     */
    public void showTechniquesMenu(View v){

        Intent showTechMenu = new Intent(this, TechniquesDashboardActivity.class);
        startActivity(showTechMenu);
    }


//// Use for intents If you invoke an intent and there is no app available on the device that can handle the intent, your app will crash.
//  returned List is not empty, you can safely use the intent
//       PackageManager packageManager = getPackageManager();
//        List activities = packageManager.queryIntentActivities(intent,
//                PackageManager.MATCH_DEFAULT_ONLY);
//        boolean isIntentSafe = activities.size() > 0; //if true, we have one app
    // if false, don't start activity
    //http://developer.android.com/training/basics/intents/sending.html
    //
}
