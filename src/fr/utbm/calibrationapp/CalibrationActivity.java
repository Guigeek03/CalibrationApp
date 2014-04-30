package fr.utbm.calibrationapp;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class CalibrationActivity extends Activity {
	ImageView mapView;
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	int viewHeight;
	int viewWidth;
	Rect bounds;
	float imageValues[] = new float[9];

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF startPoint = new PointF();
	PointF midPoint = new PointF();
	float oldDist = 1f;
	String savedItemClicked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);

		mapView = (ImageView) findViewById(R.id.map);
		bounds = mapView.getDrawable().getBounds();
		viewHeight = getResources().getDisplayMetrics().heightPixels;
		viewWidth  = getResources().getDisplayMetrics().widthPixels;
		
		mapView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView view = (ImageView) v;
				//dumpEvent(event);

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

		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.actionAdd:
			Toast.makeText(CalibrationActivity.this, "Select a point on the map", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.actionDiscard:
			Toast.makeText(CalibrationActivity.this, "Delete the selected point", Toast.LENGTH_SHORT).show();
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

}
