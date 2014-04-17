package fr.utbm.calibrationapp;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class Calibration extends Activity {
	ImageView mapView;
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

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
		
		mapView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView view = (ImageView) v;
				dumpEvent(event);

				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					Log.d("TEST",
							"X = " + event.getX() + " | Y = " + event.getY());
					startPoint.set(event.getX(), event.getY());
					savedMatrix.set(matrix);
					Log.d("TEST", "mode=DRAG");
					mode = DRAG;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist = spacing(event);
					Log.d("TEST", "oldDist=" + oldDist);
					if (oldDist > 10f) {
						savedMatrix.set(matrix);
						midPoint(midPoint, event);
						mode = ZOOM;
						Log.d("TEST", "mode=ZOOM");
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					mode = NONE;
					Log.d("TEST", "mode=NONE");
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {
						matrix.set(savedMatrix);
						matrix.postTranslate(event.getX() - startPoint.x,
								event.getY() - startPoint.y);

						Log.d("TEST", "Width : " + mapView.getMeasuredWidth());
						Log.d("TEST", "matrix=" + matrix);
					} else if (mode == ZOOM) {
						float newDist = spacing(event);
						Log.d("TEST", "newDist=" + newDist);
						if (newDist > 10f) {
							matrix.set(savedMatrix);
							float scale = newDist / oldDist;
							matrix.postScale(scale, scale, midPoint.x,
									midPoint.y);
						}
					}
					Log.d("TEST", "X = " + event.getX() + " | Y  = " + event.getY());
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
					break;
				}

				view.setImageMatrix(matrix);

				return true;
			}

			private void dumpEvent(MotionEvent event) {
				String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
						"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
				StringBuilder sb = new StringBuilder();
				int action = event.getAction();
				int actionCode = action & MotionEvent.ACTION_MASK;
				sb.append("event ACTION_").append(names[actionCode]);
				if (actionCode == MotionEvent.ACTION_POINTER_DOWN
						|| actionCode == MotionEvent.ACTION_POINTER_UP) {
					sb.append("(pid ").append(
							action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
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
				return FloatMath.sqrt(x * x + y * y);
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
			Toast.makeText(Calibration.this, "Select a point on the map", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.actionDiscard:
			Toast.makeText(Calibration.this, "Delete the selected point", Toast.LENGTH_SHORT).show();
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
