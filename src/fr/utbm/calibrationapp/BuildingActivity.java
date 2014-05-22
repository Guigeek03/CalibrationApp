package fr.utbm.calibrationapp;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import fr.utbm.calibrationapp.adapter.BuildingListAdapter;
import fr.utbm.calibrationapp.model.Building;
import fr.utbm.calibrationapp.utils.NetworkUtils;

public class BuildingActivity extends Activity {

	private ActionMode mActionMode;
	private ListView listBuildings;
	private SharedPreferences sp;
	private TextView text;
	private BuildingListAdapter listAdapter;
	private ArrayList<Building> buildings = new ArrayList<Building>();
	private int lastItemSelected = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_building);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		sp = PreferenceManager.getDefaultSharedPreferences(this);

		listBuildings = (ListView) findViewById(R.id.list_buildings);

		// Set new font
		Typeface typeFace = Typeface.createFromAsset(getAssets(), "calibril.ttf");
		text = (TextView) findViewById(R.id.building_text);
		text.setTypeface(typeFace);
		// Font now set

		new RefreshBuildingTask().execute("http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings");

		listAdapter = new BuildingListAdapter(BuildingActivity.this, buildings);
		listBuildings.setAdapter(listAdapter);

		listBuildings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final Building item = (Building) parent.getItemAtPosition(position);
				Intent i = new Intent("fr.utbm.calibrationapp.FLOOR");
				Bundle b = new Bundle();
				b.putString("building", item.getName());
				i.putExtras(b);
				startActivity(i);
			}
		});

		listBuildings.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				lastItemSelected = position;
				if (mActionMode != null) {
					return false;
				}

				// Start the CAB using the ActionMode.Callback defined
				// above
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
		case R.id.actionAdd:
			Toast.makeText(BuildingActivity.this, "Ajouter", Toast.LENGTH_SHORT).show();
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("New building...");
			alert.setMessage("Enter the name :");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Editable value = input.getText();
					Log.d("REQUEST", sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings/add?name=" + value.toString());
					new AddBuildingTask().execute(new Building(0, value.toString(), 0));
				}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});

			alert.show();

			return true;
		case R.id.actionRefresh:
			Toast.makeText(BuildingActivity.this, "Refresh", Toast.LENGTH_SHORT).show();
			new RefreshBuildingTask().execute("http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_buildings, menu);
		return true;
	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.menu_buildings_cab, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (lastItemSelected != -1) {
				switch (item.getItemId()) {
				case R.id.actionDiscard:
					Toast.makeText(BuildingActivity.this, "Deletion selected", Toast.LENGTH_LONG).show();
					new DeleteBuildingTask().execute((Building) listAdapter.getItem(lastItemSelected));
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

	private class DeleteBuildingTask extends AsyncTask<Building, Void, String> {
		@Override
		protected String doInBackground(Building... params) {
			try {
				return NetworkUtils.sendRequest("http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings/delete?id=" + params[0].getId());
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d("HTTP_REQUEST", result);
			try {
				JSONObject jsonObject = new JSONObject(result);
				if (jsonObject.getString("answer").equals("success")) {
					Log.d("HTTP_REQUEST", "Delete building...");
					buildings.remove(new Building(jsonObject.getInt("id"), "", 0));
					listAdapter.notifyDataSetChanged();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private class AddBuildingTask extends AsyncTask<Building, Void, String> {
		private Building newBuilding;

		@Override
		protected String doInBackground(Building... params) {
			try {
				newBuilding = params[0];
				return NetworkUtils.sendRequest("http://" + sp.getString("serverAddress", "192.168.1.1") + ":" + sp.getString("serverPort", "80") + "/server/buildings/add?name=" + newBuilding.getName());
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d("HTTP_REQUEST", result);
			try {
				JSONObject jsonObject = new JSONObject(result);
				if (jsonObject.getString("answer").equals("success")) {
					Log.d("HTTP_REQUEST", "Add building...");
					newBuilding.setId(jsonObject.getInt("id"));
					newBuilding.setName(jsonObject.getString("name"));
					buildings.add(newBuilding);
					listAdapter.notifyDataSetChanged();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private class RefreshBuildingTask extends AsyncTask<String, Void, String> {
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
				buildings.clear();
				Log.d("HTTP_REQUEST", "Refresh buildings...");
				for (int i = 0; i < jsonArray.length(); ++i) {
					JSONObject row = jsonArray.getJSONObject(i);
					buildings.add(new Building(row.getInt("id"), row.getString("name"), row.getInt("count")));
				}
				listAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
