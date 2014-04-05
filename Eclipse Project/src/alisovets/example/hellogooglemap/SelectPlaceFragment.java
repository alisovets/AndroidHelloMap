package alisovets.example.hellogooglemap;

import java.util.List;

import alisovets.example.hellogooglemap.data.DataAdapter;
import alisovets.example.hellogooglemap.dialog.DialogCreator;
import alisovets.example.hellogooglemap.dto.PlaceBean;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectPlaceFragment extends ListFragment {

	private Location mCurrentLocation;

	@Override
	public void onResume() {
		super.onResume();
		DataAdapter dataAdapter = ((MapApplication) getActivity().getApplication()).getDataAdapter();
		List<PlaceBean> placeList = dataAdapter.selectAll();
		if (placeList.size() == 0) {
			getActivity().finish();
			return;
		}
		setHasOptionsMenu(true);
		setListAdapter(new LocationAdapter(getActivity(), placeList));
		obtainExtraParameters();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			DialogCreator.viewDialog(getActivity(), R.layout.about);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// get selected items
		PlaceBean placeBean = (PlaceBean) getListAdapter().getItem(position);
		Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.putExtra(MapApplication.SELECTED_PLACE_KEY, (Parcelable) placeBean);
		intent.putExtra(MapApplication.CURRENT_LOCAION_KEY, mCurrentLocation);
		startActivity(intent);
	}

	/*
	 * gets parameters which is passed from the calling activity 
	 */
	private void obtainExtraParameters() {
		Intent intent = getActivity().getIntent();
		mCurrentLocation = intent.getParcelableExtra(MapApplication.CURRENT_LOCAION_KEY);
		
		
	}
	
	/*
	 * The Extending ArrayAtapter class for for displaying the list of PlaceBean objects 
	 */
	private class LocationAdapter extends ArrayAdapter<PlaceBean> {
		private Activity activity;
		private List<PlaceBean> placeList;

		public LocationAdapter(Activity activity, List<PlaceBean> placeList) {
			super(activity, R.layout.place_item, placeList);
			this.activity = activity;
			this.placeList = placeList;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View rowView = inflater.inflate(R.layout.place_item, parent, false);
			TextView titleTextView = (TextView) rowView.findViewById(R.id.title);
			TextView snippetTextView = (TextView) rowView.findViewById(R.id.description);
			PlaceBean placeBean = placeList.get(position);
			titleTextView.setText(placeBean.getTitle());
			snippetTextView.setText(placeBean.getSnippet());
			return rowView;
		}

	}
	
}
