package com.teamcasey.watchdog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class TechniquesDashboardActivity extends Activity {

    public static String packageString = "com.google.android.youtube";
    public static String filter = "ASMR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_techniques_dashboard);

        RadioGroup techniqueGroup = (RadioGroup) findViewById(R.id.techniquesRadioGroup);

        final RadioButton comedyRadioButton = (RadioButton) findViewById(R.id.comedyRadioButton);
        final RadioButton asmrRadioButton = (RadioButton) findViewById(R.id.asmrRadioButton);
        final RadioButton spotifyRadioButton = (RadioButton) findViewById(R.id.spotifyRadioButton);
        final RadioButton tetrisRadioButton = (RadioButton) findViewById(R.id.tetrisRadioButton);

        techniqueGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);

                if (checkedRadioButton.getText().equals("Comedy Playlist")) {
                    comedyRadioButton.setChecked(true);
                    packageString = "com.google.android.youtube";
                    filter = "Comedy";
                } else if (checkedRadioButton.getText().equals("ASMR Playlist")) {
                    asmrRadioButton.setChecked(true);
                    packageString = "com.google.android.youtube";
                    filter = "ASMR";
                } else if (checkedRadioButton.getText().equals("Spotify")) {
                    spotifyRadioButton.setChecked(true);
                    packageString = "com.spotify.music";
                } else if (checkedRadioButton.getText().equals("Tetris")) {
                    tetrisRadioButton.setChecked(true);
                    packageString = "com.ea.game.tetris2011_row";
                }
                    //String text = checkedRadioButton.getText().toString();

            }
        });



        setupButtonListeners();
    }

    private void setupButtonListeners() {
        final Button backButton = (Button) findViewById(R.id.techniquesBackButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toDashboard(v);
            }
        });
    }

    private void toDashboard (View view) {
        Intent backIntent = new Intent(this, DashboardActivity.class);
        startActivity(backIntent);
    }

    private void getID() {
        //
    }
}
//TODO: Onclick launches for buttons