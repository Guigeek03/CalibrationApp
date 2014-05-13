package fr.utbm.calibrationapp;

import fr.utbm.calibrationapp.widget.ImageMap;
import fr.utbm.calibrationapp.R;

import android.app.Activity;
import android.os.Bundle;

public class MapActivity extends Activity {
	ImageMap mImageMap;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        // find the image map in the view
        mImageMap = (ImageMap)findViewById(R.id.map);
	    //mImageMap.setImageResource(R.drawable.usamap);
        mImageMap.setImageResource(R.drawable.h_rdc);
        
        // add a click handler to react when areas are tapped
        mImageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler()
        {
			@Override
			public void onImageMapClicked(int id, ImageMap imageMap)
			{
				// when the area is tapped, show the name in a 
				// text bubble
				mImageMap.showBubble(id);
			}

			@Override
			public void onBubbleClicked(int id)
			{
				// react to info bubble for area being tapped
			}
		});
    }
}
