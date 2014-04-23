package fr.utbm.calibrationapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class WelcomeActivity extends Activity {
	Button butPrefs;
	Button butCalibrate;
	ImageView logo;
	TextView textcopyrights;
	TextView welcometext;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome); 
       
        logo = (ImageView) findViewById(R.id.logo);
        butPrefs = (Button) findViewById(R.id.settings_button);
        butCalibrate = (Button) findViewById(R.id.calibration_button);
        textcopyrights = (TextView) findViewById(R.id.copyrights);
        welcometext = (TextView) findViewById(R.id.welcometext);
        
        //Set new font
        Typeface typeFace=Typeface.createFromAsset(getAssets(),"calibril.ttf");
        butCalibrate.setTypeface(typeFace);
        textcopyrights.setTypeface(typeFace);
        butPrefs.setTypeface(typeFace);
        welcometext.setTypeface(typeFace);
        
        //Font now set
        
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
