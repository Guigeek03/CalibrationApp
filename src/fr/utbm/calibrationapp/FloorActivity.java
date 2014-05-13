package fr.utbm.calibrationapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import fr.utbm.calibrationapp.adapter.FloorListAdapter;

public class FloorActivity extends Activity {
	TextView text_building;
	ListView listFloors;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_floor);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle b = getIntent().getExtras();

		text_building = (TextView) findViewById(R.id.chosen_building);
		listFloors = (ListView) findViewById(R.id.list_floors);

		// Set new font
		Typeface typeFace = Typeface.createFromAsset(getAssets(), "calibril.ttf");
		text_building.setTypeface(typeFace);
		// Font now set
		setTitle("Floor (" + b.getString("building") + ")");

		/**String[] values = new String[] { "RDC", "Floor 2", "Floor 3", "Floor 4", "Floor 5" };

		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i]);
		}

		final StableArrayAdapter adapter = new StableArrayAdapter(this, R.layout.list_item, list);**/
		final FloorListAdapter adapter = new FloorListAdapter(FloorActivity.this, b.getString("building"));
		listFloors.setAdapter(adapter);

		listFloors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				Intent i = new Intent("fr.utbm.calibrationapp.MAP");
				startActivity(i);
			}
		});
	}

	/**private class StableArrayAdapter extends ArrayAdapter<String> {

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

	}**/

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
		}
		return super.onOptionsItemSelected(item);
	}

}
