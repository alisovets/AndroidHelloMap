package alisovets.example.hellogooglemap;

import java.util.ArrayList;
import java.util.List;

import alisovets.example.hellogooglemap.dialog.DialogCreator;
import alisovets.example.hellogooglemap.net.ConnectChecker;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 * 
 * @author Alexander Lisovets 2014
 * 
 */
public class MainFragment extends Fragment implements OnClickListener {

	private final static long OBTAIN_LOCATION_TIMEOUT_MS = 25000L;
	private final static String TAG = "StartActivity log";
	private final static int[] DISTANCES = { 1, 2, 3, 5, 8, 10, 0 };

	private EditText mLocationEditText;
	private LinearLayout mHidingLayout;
	private Spinner mDistanceSpinner;
	private ArrayAdapter<DistanceItem> mDistanceArrayAdapter;
	private Criteria mCriteria;
	private AlertDialog mWaitLocationDialog;
	private LocationManager mLocationManager;
	private volatile Location mCurrentLocation;
	private Button mSelectButton;
	private Button mShowMapButton;
	private Button mDeterminateLocationButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View viewHierarchy = inflater.inflate(R.layout.fragment_main, container, false);

		mLocationEditText = (EditText) viewHierarchy.findViewById(R.id.informaion_field);
		mDistanceSpinner = (Spinner) viewHierarchy.findViewById(R.id.distance_spinner);
		mDistanceSpinner.setPrompt(getString(R.string.select_distance));
		mHidingLayout = (LinearLayout) viewHierarchy.findViewById(R.id.hiding_layout);
		mSelectButton = (Button) viewHierarchy.findViewById(R.id.select_button);
		mSelectButton.setOnClickListener(this);
		mShowMapButton = (Button) viewHierarchy.findViewById(R.id.show_map_button);
		mShowMapButton.setOnClickListener(this);
		mDeterminateLocationButton = (Button) viewHierarchy.findViewById(R.id.determinate_button);
		mDeterminateLocationButton.setOnClickListener(this);

		prepareSpinnerAdapter();
		initCriteria();

		mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		setHasOptionsMenu(true);

		return viewHierarchy;
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
	public void onResume() {
		super.onResume();

		MapApplication application = (MapApplication) getActivity().getApplication();
		mCurrentLocation = application.getCurrentLocation();

		if (!ConnectChecker.isNetworkAvailable(getActivity())) {
			DialogCreator.noNetworkChangeSettingOrCloseDialog(getActivity(), R.string.check_your_conn_setting_message);
		}

		if (mCurrentLocation != null) {
			mLocationEditText.setText(mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
			mHidingLayout.setVisibility(View.VISIBLE);
		}

		if (application.getDataAdapter().count() > 0) {
			mSelectButton.setVisibility(View.VISIBLE);
		} else {
			mSelectButton.setVisibility(View.GONE);
		}

	}

	@Override
	public void onPause() {
		super.onPause();

		((MapApplication) getActivity().getApplication()).setCurrentLocation(mCurrentLocation);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.determinate_button:
			determinate();
			break;

		case R.id.show_map_button:
			showOnMap();
			break;

		case R.id.select_button:
			selectLocation();
		}

	}

	/*
	 * Starts MapActivity to show the map
	 */
	private void showOnMap() {
		Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.putExtra(MapApplication.DISTANCE_KEY, ((DistanceItem) mDistanceSpinner.getSelectedItem()).getDistance());
		intent.putExtra(MapApplication.CURRENT_LOCAION_KEY, mCurrentLocation);
		startActivity(intent);
	}

	/*
	 * Starts SelectLocationActivity to select a saved location
	 */
	private void selectLocation() {
		Intent intent = new Intent(getActivity(), SelectPlaceActivity.class);
		intent.putExtra(MapApplication.CURRENT_LOCAION_KEY, mCurrentLocation);
		startActivity(intent);
	}

	/*
	 * Starts current location determination.
	 */
	private void determinate() {
		mHidingLayout.setVisibility(View.INVISIBLE);
		mLocationEditText.setText("");
		mCurrentLocation = null;
		String bestProvider = mLocationManager.getBestProvider(mCriteria, true);
		if (bestProvider == null) {
			mLocationEditText.setText(R.string.no_location_provider);
			DialogCreator.determinateLocationProblemChangeSettingDialog(getActivity(), R.string.no_location_provider,
					R.string.check_your_location_setting_message);
			return;
		}

		mLocationManager.requestLocationUpdates(bestProvider, OBTAIN_LOCATION_TIMEOUT_MS, 100, mLocationListener);
		mWaitLocationDialog = DialogCreator.openWaitDialog(getActivity(), R.layout.wait_location, mOnDismissListener);

		Handler handler = new Handler();
		handler.postDelayed(new WaitLocationDialogCloser(mWaitLocationDialog), OBTAIN_LOCATION_TIMEOUT_MS);
	}

	private void initCriteria() {
		mCriteria = new Criteria();
		mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
		mCriteria.setPowerRequirement(Criteria.POWER_LOW);
		mCriteria.setAltitudeRequired(false);
		mCriteria.setBearingRequired(false);
		mCriteria.setSpeedRequired(false);
		mCriteria.setCostAllowed(false);
	}

	/*
	 * inits the select distance spinner
	 */
	private void prepareSpinnerAdapter() {
		List<DistanceItem> distanceList = new ArrayList<DistanceItem>();
		for (int i = 0; i < DISTANCES.length; i++) {
			distanceList.add(new DistanceItem(DISTANCES[i]));
		}

		mDistanceArrayAdapter = new ArrayAdapter<DistanceItem>(getActivity(), android.R.layout.simple_spinner_item, distanceList);
		mDistanceArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mDistanceSpinner.setAdapter(mDistanceArrayAdapter);
		mDistanceSpinner.setSelection(0);
	}

	/*
	 * gets and returns the last known location
	 */
	private Location obtainLastKnownLocation() {
		Location location = null;
		LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(mCriteria, true);

		if ((providers == null) || (providers.size() == 0)) {
			providers = locationManager.getProviders(true);
		}

		for (String currentProvider : providers) {
			Location tmpLocation = locationManager.getLastKnownLocation(currentProvider);
			if (tmpLocation == null) {
				continue;
			}
			if ((location == null) || (location.getTime() < tmpLocation.getTime())) {
				location = tmpLocation;
			}
		}

		return location;
	}

	/*
	 * synchronized getter
	 */
	private synchronized Location obtainCurrentLocation() {
		return mCurrentLocation;
	}

	/*
	 * synchronized setter
	 */
	private synchronized void changeCurrentLocation(Location location) {
		mCurrentLocation = location;
	}

	private class DistanceItem {
		private int distance;

		DistanceItem(int distance) {
			this.distance = distance;
		}

		int getDistance() {
			return distance;
		}

		@Override
		public String toString() {
			if (distance > 0) {
				return distance + " " + getString(R.string.kilometer_short);
			} else {
				return getString(R.string.unlimited);
			}
		}
	}

	private OnDismissListener mOnDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			mLocationManager.removeUpdates(mLocationListener);
			if (obtainCurrentLocation() != null) {
				// the currrent location has been obtained already
				return;
			}
			mLocationEditText.setText(getString(R.string.no_location_found));
		}
	};

	private LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			changeCurrentLocation(location);
			mHidingLayout.setVisibility(View.VISIBLE);
			mLocationEditText.setText(location.getLatitude() + ", " + location.getLongitude());
			mWaitLocationDialog.dismiss();
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	};

	/*
	 * the inner class implements the Runnable interface for closing an awaiting
	 * location dialog after timeout is ended.
	 */
	private class WaitLocationDialogCloser implements Runnable {
		private AlertDialog waitLocationDialog;

		WaitLocationDialogCloser(AlertDialog waitLocationDialog) {
			this.waitLocationDialog = waitLocationDialog;
		}

		@Override
		public void run() {
			Log.d(TAG, "end timeout");
			if (obtainCurrentLocation() != null) {
				return;
			}
			if (!waitLocationDialog.isShowing()) {
				return;
			}

			final Location location = obtainLastKnownLocation();
			if (location != null) {
				DialogCreator.determinateLocationProblemUseLastKnownDialog(getActivity(), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						changeCurrentLocation(location);
						mHidingLayout.setVisibility(View.VISIBLE);
						mLocationEditText.setText(location.getLatitude() + ", " + location.getLongitude() + " ("
								+ getString(R.string.maybe_not_actually) + ")");

					}
				});
			} else {

				DialogCreator.determinateLocationProblemChangeSettingDialog(getActivity(), R.string.no_location_found,
						R.string.check_your_location_setting_message);
				mLocationEditText.setText(R.string.no_location_found);
			}

			waitLocationDialog.dismiss();
		}

	};

}
