package fr.utbm.calibrationapp.adapter;

import java.net.MalformedURLException;
import java.net.NetPermission;
import java.net.URL;
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
import fr.utbm.calibrationapp.model.Floor;
import fr.utbm.calibrationapp.utils.NetworkUtils;

public class FloorListAdapter extends BaseAdapter {
	Context m_context;
	List<Floor> floorList;
	SharedPreferences sp;
	String buildingId;
	Boolean SIMULATION = true;

	public FloorListAdapter(Context c, String id) {
		m_context = c;
		buildingId = id;
		floorList = getDataForListView();
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
		if (!SIMULATION) {
				sp = PreferenceManager.getDefaultSharedPreferences(m_context);
				//new NetworkUtils().execute(new URL("http", sp.getString("serverAddress", "192.168.1.1"), Integer.parseInt(sp.getString("serverPort", "80")), "/test/building"));
		} else {
			for (int i = 0; i < 10; i++) {
				Floor newFloor = new Floor();
				newFloor.setName("Floor " + i);
				floorList.add(newFloor);
			}
		}

		return floorList;
	}
}