package fr.utbm.calibrationapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class WelcomeActivity extends Activity {
	Button butPrefs;
	Button butCalibrate;
	ImageView logo;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome); 
        
        logo = (ImageView) findViewById(R.id.logo);
        butPrefs = (Button) findViewById(R.id.settings_button);
        butCalibrate = (Button) findViewById(R.id.calibration_button);
                
        butPrefs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        Intent i = new Intent("fr.utbm.calibrationapp.PREFS");
		        startActivity(i);
			}
		});
        
        butCalibrate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        Intent i = new Intent("fr.utbm.calibrationapp.BUILDING");
		        startActivity(i);
			}
		});

    }
}
