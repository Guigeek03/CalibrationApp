package fr.utbm.calibrationapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import fr.utbm.calibrationapp.adapter.FloorListAdapter;
import fr.utbm.calibrationapp.model.Building;
import fr.utbm.calibrationapp.model.Floor;
import fr.utbm.calibrationapp.utils.NetworkUtils;

public class FloorActivity extends Activity {
	final private int ACTIVITY_NEW_FLOOR = 1;

	private ActionMode mActionMode;
	private SharedPreferences sp;
	private TextView text_building;
	private ListView listFloors;
	private Bundle bundle;
	private FloorListAdapter listAdapter;
	private ArrayList<Floor> floors = new ArrayList<Floor>();
	private int lastItemSelected = -1;
	private int buildingId;
	private String buildingName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_floor);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		bundle = getIntent().getExtras();

		sp = PreferenceManager.getDefaultSharedPreferences(this);

		text_building = (TextView) findViewById(R.id.chosen_building);
		listFloors = (ListView) findViewById(R.id.list_floors);

		Typeface typeFace = Typeface.createFromAsset(getAssets(), "calibril.ttf");
		text_building.setTypeface(typeFace);

		buildingName = bundle.getString("building_name");
		buildingId = bundle.getInt("building_id");
		setTitle("Floor (" + buildingName + ")");

		listAdapter = new FloorListAdapter(FloorActivity.this, buildingId, floors);
		listFloors.setAdapter(listAdapter);

		new RefreshFloorTask().execute("http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings/" + buildingId);

		listFloors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				Intent i = new Intent("fr.utbm.calibrationapp.CALIBRATION");
				Floor floor = (Floor) listAdapter.getItem((int) id);
				Bundle b = new Bundle();
				b.putString("image", buildingId + "_" + floor.getId() + ".jpg");
				b.putInt("mapId", floor.getId());
				i.putExtras(b);
				startActivity(i);
			}
		});

		listFloors.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				lastItemSelected = position;
				if (mActionMode != null) {
					return false;
				}

				mActionMode = startActionMode(mActionModeCallback);
				view.setSelected(true);
				return true;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
			} else {
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
			/** ADD NEW ACTION **/
		case R.id.actionAdd:
			Toast.makeText(FloorActivity.this, "Ajouter", Toast.LENGTH_SHORT).show();
			Intent i = new Intent("fr.utbm.calibrationapp.NEW_FLOOR");
			i.putExtras(bundle);
			startActivityForResult(i, ACTIVITY_NEW_FLOOR);
			return true;
			/** REFRESH LIST ACTION **/
		case R.id.actionRefresh:
			Toast.makeText(FloorActivity.this, "Refresh", Toast.LENGTH_SHORT).show();
			new RefreshFloorTask().execute("http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings/" + bundle.getInt("building_id"));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_floors, menu);
		return true;
	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.menu_floors_cab, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		/** MANAGES CLICKS ON CAB **/
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (lastItemSelected != -1) {
				switch (item.getItemId()) {
				case R.id.actionDiscard:
					Toast.makeText(FloorActivity.this, "Deletion selected", Toast.LENGTH_LONG).show();
					new DeleteFloorTask().execute((Floor) listAdapter.getItem(lastItemSelected));
					lastItemSelected = -1;
					mode.finish();
					return true;
				default:
					return false;
				}
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_NEW_FLOOR:
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(FloorActivity.this, "Addition canceled", Toast.LENGTH_SHORT).show();
			} else if (resultCode == RESULT_OK) {
				Toast.makeText(getApplicationContext(), "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
				try {
					JSONObject response = new JSONObject(data.getStringExtra("jsonNewFloor"));
					if (response.getBoolean("success")) {
						JSONObject jsonNewFloor = new JSONObject(response.getString("data"));

						try {
						    File dataDir = Environment.getExternalStorageDirectory();
						    if (dataDir.canWrite()) {
						        Uri sourceImage = (Uri) data.getParcelableExtra("imageFile");
						        String sourceImagePath = sourceImage.getPath();
						        Log.d("IMAGE_UPLOAD", "CAN WRITE : " + sourceImagePath);
						        String destinationImagePath= "/calibrationApp/maps/" + buildingId + "_" + jsonNewFloor.getString("id") + ".jpg";
						        File source= new File(sourceImagePath);
						        File destination= new File(dataDir, destinationImagePath);
						        if (source.exists()) {
						        	FileInputStream fis = new FileInputStream(source);
						        	FileOutputStream fos = new FileOutputStream(destination);
						            FileChannel src = fis.getChannel();
						            FileChannel dst = fos.getChannel();
						            dst.transferFrom(src, 0, src.size());
						            src.close();
						            dst.close();
						            fis.close();
						            fos.close();
						        }
						    }
						} catch (Exception e) {
							Log.d("IMAGE_UPLOAD", "Error with image copy");
							e.printStackTrace();
						}

						Floor newFloor = new Floor(jsonNewFloor.getInt("id"), jsonNewFloor.getString("name"), jsonNewFloor.getInt("nbPoints"));
						floors.add(newFloor);
						listAdapter.notifyDataSetChanged();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class RefreshFloorTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			try {
				return NetworkUtils.sendRequest(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d("HTTP_REQUEST", result);
			try {
				JSONArray jsonArray = new JSONArray(result);
				floors.clear();
				Log.d("HTTP_REQUEST", "Refresh floors...");
				for (int i = 0; i < jsonArray.length(); ++i) {
					JSONObject row = jsonArray.getJSONObject(i);
					floors.add(new Floor(row.getInt("id"), row.getString("name"), row.getInt("nbPoints")));
				}
				listAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private class DeleteFloorTask extends AsyncTask<Floor, Void, String> {
		Floor floor;

		@Override
		protected String doInBackground(Floor... params) {
			try {
				floor = params[0];
				return NetworkUtils.sendRequest("http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings/" + buildingId + "/delete?id=" + params[0].getId());
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
					Log.d("HTTP_REQUEST", "Delete building...");
					
					File file = new File(Environment.getExternalStorageDirectory(), "/calibrationApp/maps/" + buildingId + "_" + floor.getId() + ".jpg");
					file.delete();
					floors.remove(floor);
					listAdapter.notifyDataSetChanged();
				} else {
					Toast.makeText(FloorActivity.this, jsonObject.getString("exception"), Toast.LENGTH_LONG).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
