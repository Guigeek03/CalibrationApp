package fr.utbm.calibrationapp;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import fr.utbm.calibrationapp.utils.NetworkUtils;

public class CalibrationActivity extends Activity {
	
	private SharedPreferences sp;
	private Bundle bundle;
	private Integer mapId;
	
	private RelativeLayout relativeLayout;
	private ImageView mapView;
	private ImageView marker;
	private TextView modeTextView;
	private Button measureButton;

	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private int viewHeight;
	private int viewWidth;
	private Rect bounds;
	private float imageValues[] = new float[9];
	private float selectedPoint[] = new float[2];
	boolean passiveMode = true;

	// Remember some things for zooming
	private PointF startPoint = new PointF();
	private PointF midPoint = new PointF();
	private float oldDist = 1f;
	private String savedItemClicked;
	
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		bundle = getIntent().getExtras();
		
		mapId = bundle.getInt("mapId");
		
		// Link elements from layout
		relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
		mapView = (ImageView) findViewById(R.id.map);
		measureButton = (Button) findViewById(R.id.measureButton);
		modeTextView = (TextView) findViewById(R.id.modeTextView);
		
		// Init measure button
		measureButton.setEnabled(false);
		measureButton.setClickable(false);
		
		// Init map image
		Bitmap image = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/calibrationApp/maps/" + bundle.getString("image"));
		mapView.setImageBitmap(image);
		bounds = mapView.getDrawable().getBounds();
		
		viewHeight = getResources().getDisplayMetrics().heightPixels;
		viewWidth  = getResources().getDisplayMetrics().widthPixels;
		
		// Init marker
		marker = new ImageView(getApplicationContext());
		marker.setImageResource(R.drawable.marker);
		marker.setAlpha(0f);
		relativeLayout.addView(marker);
		
		// Set Passive mode
		mapView.setOnTouchListener(passiveModeListener);
			
		// Set on click listener on measure button
		measureButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AddPointTask().execute();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actionAdd:
			mapView.setOnTouchListener(activeModeListener);
			modeTextView.setText(R.string.activeMode);
			Log.d("CALIBRATION", "ACTIVE MODE ACTIVATED !!");
			Toast.makeText(CalibrationActivity.this, "Select a point on the map", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.actionMap:
			marker.setAlpha(0f);
			measureButton.setEnabled(false);
			measureButton.setClickable(false);
			mapView.setOnTouchListener(passiveModeListener);
			modeTextView.setText(R.string.passiveMode);
			Log.d("CALIBRATION", "PASSIVE MODE ACTIVATED !!");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_calibration, menu);
		return true;
	}
	
	private OnTouchListener passiveModeListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ImageView view = (ImageView) v;
			dumpEvent(event);

			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				startPoint.set(event.getX(), event.getY());
				savedMatrix.set(matrix);
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(midPoint, event);
					mode = ZOOM;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
				} else if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						matrix.set(savedMatrix);
						float scale = newDist / oldDist;

						matrix.postScale(scale, scale, midPoint.x, midPoint.y);
						matrix.getValues(imageValues);
						
						/** ZOOM CONTROL **/
						if (imageValues[0] < viewWidth*1./bounds.width()) {
							matrix.setScale(viewWidth*1.f/bounds.width(), viewWidth*1.f/bounds.width(), midPoint.x, midPoint.y);
						}
						if (imageValues[0] > 2) {
							matrix.setScale(2, 2);
						}
					}
				}
				matrix.getValues(imageValues);
				/** PANNING CONTROL **/
				if (imageValues[2] > 0) {
					matrix.postTranslate(-imageValues[2], 0);
				}
				if (imageValues[5] > 0) {
					matrix.postTranslate(0, -imageValues[5]);
				}
				if (imageValues[2] < viewWidth - bounds.width()*imageValues[0]) {
					matrix.postTranslate(viewWidth-bounds.width()*imageValues[0]-imageValues[2], 0);
				}
				
				if (imageValues[5] < -bounds.height()*imageValues[0]) {
					matrix.postTranslate(0, -bounds.height()*imageValues[0]-imageValues[5]);
				}
				
				break;
			}

			view.setImageMatrix(matrix);

			return true;
		}

		private void dumpEvent(MotionEvent event) {
			String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
			StringBuilder sb = new StringBuilder();
			int action = event.getAction();
			int actionCode = action & MotionEvent.ACTION_MASK;
			sb.append("event ACTION_").append(names[actionCode]);
			if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
				sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
				sb.append(")");
			}
			sb.append("[");
			for (int i = 0; i < event.getPointerCount(); i++) {
				sb.append("#").append(i);
				sb.append("(pid ").append(event.getPointerId(i));
				sb.append(")=").append((int) event.getX(i));
				sb.append(",").append((int) event.getY(i));
				if (i + 1 < event.getPointerCount())
					sb.append(";");
			}
			sb.append("]");
			Log.d("TEST", sb.toString());
		}

		/** Determine the space between the first two fingers */
		private float spacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return (float) Math.sqrt(x * x + y * y);
		}

		/** Calculate the mid point of the first two fingers */
		private void midPoint(PointF point, MotionEvent event) {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}

	};
	
	private OnTouchListener activeModeListener = new OnTouchListener() {
				
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				matrix.getValues(imageValues);

				selectedPoint[0] = (event.getX() - imageValues[2]) / imageValues[0];
				selectedPoint[1] = (event.getY() - imageValues[5]) / imageValues[4];

				Log.d("POINT_TOUCH", "IMAGE : " + selectedPoint[0] + " and " + selectedPoint[1]);

				marker.setX(event.getX());
				marker.setY(event.getY());
				marker.setAlpha(1f);
				measureButton.setClickable(true);
				measureButton.setEnabled(true);
				break;
			}
			return true;
		}
	};
	
	private class AddPointTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			try {
				return NetworkUtils.sendRequest("http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/points/add?x=" + selectedPoint[0] + "&y=" + selectedPoint[1] + "&mapId=" + mapId);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d("HTTP_REQUEST", result);
			try {
				JSONObject jsonObject = new JSONObject(result);
				if (jsonObject.getBoolean("success")) {
					Toast.makeText(CalibrationActivity.this, "Point saved !", Toast.LENGTH_LONG).show();	
				} else {
					Toast.makeText(CalibrationActivity.this, "Try again...", Toast.LENGTH_LONG).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
