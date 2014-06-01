package fr.utbm.calibrationapp.adapter;

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
	private Context m_context;
	private List<Floor> floorList;
	private Integer buildingId;

	public FloorListAdapter(Context c, Integer id, List<Floor> list) {
		m_context = c;
		setBuildingId(id);
		floorList = list;
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

	public Integer getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(Integer buildingId) {
		this.buildingId = buildingId;
	}
}