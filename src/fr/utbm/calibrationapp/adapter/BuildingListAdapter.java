package fr.utbm.calibrationapp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.utbm.calibrationapp.R;
import fr.utbm.calibrationapp.model.Building;

public class BuildingListAdapter extends BaseAdapter {
	private Context m_context;
	private SharedPreferences sp;
	private List<Building> buildingList;
	private Boolean SIMULATION = true;

	public BuildingListAdapter(Context c, List<Building> list) {
		m_context = c;
		buildingList = list;
	}

	@Override
	public int getCount() {
		return buildingList.size();
	}

	@Override
	public Object getItem(int index) {
		return buildingList.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.building_list_item, viewGroup, false);
		}

		TextView buildingName = (TextView) view.findViewById(R.id.buildingName);
		TextView buildingNbFloors = (TextView) view.findViewById(R.id.buildingNbFloors);

		Building building = buildingList.get(index);

		buildingName.setText(building.getName());
		buildingNbFloors.setText(building.getNbFloors() + " floors available");

		return view;
	}

	/**public List<Building> getDataForListView() {
		List<Building> buildingList = new ArrayList<Building>();
		if (!SIMULATION) {
				sp = PreferenceManager.getDefaultSharedPreferences(m_context);
				//new NetworkUtils().execute(new URL("http", sp.getString("serverAddress", "192.168.1.1"), Integer.parseInt(sp.getString("serverPort", "80")), "/buildings"));
		} else {

			for (int i = 0; i < 6; i++) {
				Building newBuilding = new Building();
				newBuilding.setName("Building " + i);
				newBuilding.setNbFloors(i + 1);
				buildingList.add(newBuilding);
			}
		}

		return buildingList;
	}**/
}