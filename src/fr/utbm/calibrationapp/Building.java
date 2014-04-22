package fr.utbm.calibrationapp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import fr.utbm.calibrationapp.utils.NetworkUtils;

public class Building extends Activity {
	ActionMode mActionMode;
	ListView listBuildings;
	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_building);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		listBuildings = (ListView) findViewById(R.id.list_buildings);

		String[] values = new String[] { "Building A", "Building B", "Building C", "Building D", "Building E", "Building F", "Building G", "Building H", "Building I" };

		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
		}

		final StableArrayAdapter adapter = new StableArrayAdapter(this, R.layout.list_item, list);
		listBuildings.setAdapter(adapter);
		listBuildings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final String item = (String) parent.getItemAtPosition(position);
				Intent i = new Intent("fr.utbm.calibrationapp.FLOOR");
				Bundle b = new Bundle();
				b.putString("building", item);
				i.putExtras(b);
				startActivity(i);
			}
		});

		listBuildings.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				final String item = (String) parent.getItemAtPosition(position);

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
			Toast.makeText(Building.this, "Ajouter", Toast.LENGTH_SHORT).show();
			try {
				String newBuildingName = "newBuilding";
				new NetworkUtils().execute(new URL("http", sp.getString("serverAddress", "192.168.1.1"), Integer.parseInt(sp.getString("serverPort", "80")), "/buildings/add?name="+newBuildingName));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return true;
		case R.id.actionRefresh:
			Toast.makeText(Building.this, "Refresh", Toast.LENGTH_SHORT).show();
			try {
				new NetworkUtils().execute(new URL("http", sp.getString("serverAddress", "192.168.1.1"), Integer.parseInt(sp.getString("serverPort", "80")), "/buildings"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
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

	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

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

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.actionDiscard:
				Toast.makeText(Building.this, "Deletion selected", Toast.LENGTH_LONG).show();
				try {
					String id = "1";
					new NetworkUtils().execute(new URL("http", sp.getString("serverAddress", "192.168.1.1"), Integer.parseInt(sp.getString("serverPort", "80")), "/buildings/delete?id="+id));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				mode.finish(); // Action picked, so close the CAB
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	};
}
