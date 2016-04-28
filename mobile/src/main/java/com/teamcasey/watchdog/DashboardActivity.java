package com.teamcasey.watchdog;

//

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;

public class DashboardActivity extends Activity {


    private String packName = "";
    private String filter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setupButtonListeners();
    }

    private void setupButtonListeners() {

        final Button launchActivity = (Button) findViewById(R.id.btnStartTechnique);
        final Button viewActivity = (Button) findViewById(R.id.btnViewTechniques);
        final ImageButton settingsButton = (ImageButton) findViewById(R.id.btnSettingsMenu);

        final SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        launchActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // int checkedID = mSharedPreference.getInt("MY_SHARED_PREF", MODE_PRIVATE);
                String selectedRadio = mSharedPreference.getString("techName", "ASMR Playlist");

                if (selectedRadio.equals("Comedy Playlist")) {
                    packName = "com.google.android.youtube";
                    filter = "Comedy";
                } else if (selectedRadio.equals("ASMR Playlist")) {
                    packName = "com.google.android.youtube";
                    filter = "ASMR";
                } else if (selectedRadio.equals("Spotify")) {
                    packName = "com.spotify.music";
                } else if (selectedRadio.equals("Tetris")) {
                    packName = "com.ea.game.tetris2011_row";
                }

                if (isInstalled(packName)) {
                    Intent LaunchIntent = getPackageManager()
                            .getLaunchIntentForPackage(packName);
                    startActivity(LaunchIntent);
                } else {
                    if (packName.equals("com.google.android.youtube")) {
                        Uri webpage = null;
                        if (filter.equals("Comedy")) {
                            // TODO
                            webpage = Uri.parse("http://www.youtube.com");
                        } else if (filter.equals("ASMR")) {
                            webpage = Uri.parse("https://www.youtube.com/watch?v=1s58rW0_LN4&list=PLAEQD0ULngi5nVGjPmjw-vCE5AuDTLkkQ");
                        }
                        Intent LaunchIntent = new Intent(Intent.ACTION_VIEW, webpage);
                        startActivity(LaunchIntent);
                    } else {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packName)));
                        }
                    }
                }
            }
        });

        Intent intent = getIntent();
        int value = intent.getIntExtra("flag_key", 0);

        if (value == 1) {
            launchActivity.performClick();
        }

        viewActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toActivityMenu(v);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toSettingsMenu(v);
            }
        });
    }
    /**
     * called by res/layout/activity_dashboard.xml
     * settings_button: onClick
     */

    // Inclusion was causing an error.
    /**
     * public void showSettings(View v) {
     * <p/>
     * Intent showSettings = new Intent(this, SettingsActivity.class);
     * startActivity(showSettings);
     * }
     **/

    private void toActivityMenu(View view) {
        Intent activityIntent = new Intent(this, TechniquesDashboardActivity.class);
        startActivity(activityIntent);
    }

    private void toSettingsMenu(View view) {
        Intent settingsIntent = new Intent(this, DatabaseShowcaseActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * Launches a distraction
     * TODO: Listener service for "launch_technique" that calls provided intent string, Organization of many intents
     * called from start_technique_button: onClick
     *
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
    public void showTechniquesMenu(View v) {

        Intent showTechMenu = new Intent(this, TechniquesDashboardActivity.class);
        startActivity(showTechMenu);
    }


    // Checks to see if a certain package is installed, according to package name
    private boolean isInstalled(String packName) {

        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(packName)) {
                return true;
            }

            // Log.d(TAG, "Installed package :" + packageInfo.packageName);
            // Log.d(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
        }
        return false;
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
