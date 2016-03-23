package com.teamcasey.watchdog;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.net.Uri;

public class ButtonTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_test);
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        final Button openWith = (Button) findViewById(R.id.openWith);
        final Button toYoutube = (Button) findViewById(R.id.youtube);
        final Button databaseTest = (Button) findViewById(R.id.database);

        openWith.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openWith(v);
            }
        });

        toYoutube.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toYoutube(v);
            }
        });

        databaseTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toDatabaseActivity(v);
            }
        });
    }

    private void openWith(View view) {
        // Map point based on address
        Uri location = Uri.parse("https://www.google.com/maps/place/235+Pheasant+Run+Dr,+Blacksburg,+VA+24060/@37.2485043,-80.4211887,17z/data=!3m1!4b1!4m2!3m1!1s0x884d957d0960648b:0x14227e7d1c0d293d");
        Intent intent = new Intent(Intent.ACTION_VIEW, location);
        Intent chooser = Intent.createChooser(intent, "Open with...");

        // Verify it resolves
        //PackageManager packageManager = getPackageManager();
        //List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        //boolean isIntentSafe = activities.size() > 0;

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    private void toYoutube(View view) {
        Uri webpage = Uri.parse("http://www.youtube.com");
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        startActivity(webIntent);
    }

    private void toDatabaseActivity(View view) {
        Intent intent = new Intent(this, DatabaseShowcaseActivity.class);
        startActivity(intent);
    }

}
