package com.teamcasey.watchdog;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.app.Activity;
import android.content.SharedPreferences;


public class TechniquesDashboardActivity extends Activity {

    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_techniques_dashboard);
        radioGroup = (RadioGroup)findViewById(R.id.techniquesRadioGroup);
        radioGroup.setOnCheckedChangeListener(radioGroupOnCheckedChangeListener);

        final Button backButton = (Button) findViewById(R.id.techniquesBackButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toDashboard(v);
            }
        });

        LoadPreferences();
    }
    
    OnCheckedChangeListener radioGroupOnCheckedChangeListener =
      new OnCheckedChangeListener(){

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

     RadioButton checkedRadioButton = (RadioButton)radioGroup.findViewById(checkedId);
     int checkedIndex = radioGroup.indexOfChild(checkedRadioButton);
     String techName = checkedRadioButton.getText().toString();
     
     SavePreferences(checkedIndex, techName);
    }
      };

 private void SavePreferences(int value, String name) {

     SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

     SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();

     preferenceEditor.putString("techName", name);
     preferenceEditor.putInt("id", value);
     preferenceEditor.commit();
 }
 
 private void LoadPreferences(){
     SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

  int savedRadioIndex = sharedPreferences.getInt("id", 0);
     RadioButton savedCheckedRadioButton = (RadioButton)radioGroup.getChildAt(savedRadioIndex);
  savedCheckedRadioButton.setChecked(true);
 }

    private void toDashboard(View view) {
        Intent backIntent = new Intent(this, DashboardActivity.class);
        startActivity(backIntent);
    }
}