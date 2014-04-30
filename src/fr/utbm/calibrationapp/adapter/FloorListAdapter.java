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
import fr.utbm.calibrationapp.model.Floor;

public class FloorListAdapter extends BaseAdapter {
	Context m_context;
	List<Floor> floorList = getDataForListView();
	
	public FloorListAdapter(Context c) {
		m_context = c;
	}

	@Override
	public int getCount() {
		return floorList.size();
	}

	@Override
	public Object getItem(int index) {
		return floorList.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.floor_list_item, viewGroup, false);
		}

		TextView floorName = (TextView) view.findViewById(R.id.floorName);
		TextView floorNbPoints = (TextView) view.findViewById(R.id.floorNbPoints);

		Floor floor = floorList.get(index);

		floorName.setText(floor.getName());
		floorNbPoints.setText(floor.getNbPoints() + " saved points");

		return view;
	}

	public List<Floor> getDataForListView() {
		List<Floor> floorList = new ArrayList<Floor>();

		for (int i = 0; i < 10; i++) {
			Floor newFloor = new Floor();
			newFloor.setName("Floor " + i);
			floorList.add(newFloor);
		}

		return floorList;
	}
}