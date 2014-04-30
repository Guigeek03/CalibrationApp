package fr.utbm.calibrationapp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.utbm.calibrationapp.R;
import fr.utbm.calibrationapp.model.Building;

public class BuildingListAdapter extends BaseAdapter {
	Context m_context;
	List<Building> buildingList = getDataForListView();
	
	public BuildingListAdapter(Context c) {
		m_context = c;
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

	public List<Building> getDataForListView() {
		List<Building> buildingList = new ArrayList<Building>();

		for (int i = 0; i < 6; i++) {
			Building newBuilding = new Building();
			newBuilding.setName("Building " + i);
			newBuilding.setNbFloors(i + 1);
			buildingList.add(newBuilding);
		}

		return buildingList;
	}
}