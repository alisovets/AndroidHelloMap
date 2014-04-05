package alisovets.example.hellogooglemap;

import java.util.ArrayList;
import java.util.List;

import alisovets.example.hellogooglemap.data.DataAdapter;
import alisovets.example.hellogooglemap.dialog.DialogCreator;
import alisovets.example.hellogooglemap.dto.PlaceBean;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Demo activity that demonstrates the work of some features of GoogleMap API
 * 
 * @author Alexander Lisovets 2014
 * 
 */
public class MapActivity extends  ActionBarActivity implements GoogleMap.OnMapClickListener, OnMarkerClickListener {

	private final static int[] MAP_TYPES = {GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_TERRAIN, GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_HYBRID};
	private final static double EQUATOR_LENGTH = 40000;
	private final static double MAP_EQUATOR_LENGTH_DP = 256;
	private final static double UNIT_DENSITY = 160;
	private final static double SCALE_FACTOR = UNIT_DENSITY * EQUATOR_LENGTH / MAP_EQUATOR_LENGTH_DP / 4; 
	private final static float DEFAULT_ZOOM = 14;
	private final static String TAG = "MapActivity log";

	private PlaceBean mSelectedPlace;
	private volatile LatLng mCurrentLatLng;
	private GoogleMap mMap;
	private int mMapType;
	private Criteria mCriteria;
	private LinearLayout mMarkerInfoLayout;
	private int mMaxDistance;
	private Marker mCurrentPositionMarker;
	private MarkerHolder mFocusedMarkerHolder;
	private EditText mMarkerTitleEditText;
	private EditText mMarkerDescriptionEditText;
	private InputMethodManager mInputMethodManager;
	private List<MarkerHolder> mMarkerHolders;
	private DataAdapter mDataAdapter;
	private MenuItem mEditMarkerItem;
	private MenuItem mDeleteMarkerItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map_layout);
		mMarkerTitleEditText = (EditText) findViewById(R.id.markerTitle);
		mMarkerDescriptionEditText = (EditText) findViewById(R.id.markerDescription);
		mMarkerInfoLayout = (LinearLayout) findViewById(R.id.markerInfoLayout);
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if (mMap == null) {
			Log.d(TAG, "The application can not open the map! Some Google Map service components are not available.");
			DialogCreator.messageDialog(this, R.string.failed_to_open_map, R.string.card_cannot_be_opened);
			return;
		}
		
		Log.d(TAG, "The Google Play service is ok.");
		mMapType = GoogleMap.MAP_TYPE_NORMAL;
		mMap.setMapType(mMapType);
		mMap.setOnMarkerClickListener(this);

		initCriteria();

		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mDataAdapter = ((MapApplication) getApplication()).getDataAdapter();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mMap == null){
			return;
		}
		mMap.clear();
		obtainExtraParameters();
		mCurrentPositionMarker = addLocationMarker(mCurrentLatLng, getString(R.string.i_am_here), null, BitmapDescriptorFactory.HUE_AZURE);
		float zoomLevel = calculateZoom(mMaxDistance);
		if (mSelectedPlace != null) {
			LatLng selectedLatLng = new LatLng(mSelectedPlace.getLatitude(), mSelectedPlace.getLongitude());
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, zoomLevel));
		} else {
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, zoomLevel));
		}
		mMap.setOnMapClickListener(this);
		arrangeLocateMarkersFromDB();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map_menu, menu);
		mDeleteMarkerItem = menu.getItem(0);
		mEditMarkerItem = menu.getItem(1);
		return true;
	}
	
	
	private void obtainExtraParameters() {
		Intent intent = getIntent();
		mSelectedPlace = intent.getParcelableExtra(MapApplication.SELECTED_PLACE_KEY);
		Location currentLocation = intent.getParcelableExtra(MapApplication.CURRENT_LOCAION_KEY);
		if (currentLocation != null) {
			mCurrentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			mMaxDistance = intent.getIntExtra(MapApplication.DISTANCE_KEY, 0);
		}
	}

	
	/* 
	 * calculate the max zoom level at which a specified radius circle completely fits on the screen borders. 
	 */
	private float calculateZoom(double radius) {
		if (radius == 0) {
			return DEFAULT_ZOOM;
		}
		DisplayMetrics displayMatrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMatrics);
		
		double scale = SCALE_FACTOR * displayMatrics.widthPixels / displayMatrics.xdpi / radius;
		double zoom = Math.log(scale) / Math.log(2);
		if (zoom < mMap.getMinZoomLevel()) {
			zoom = (int) mMap.getMinZoomLevel();
		}
		if (zoom > mMap.getMaxZoomLevel()) {
			zoom = (int) mMap.getMaxZoomLevel();
		}
		return (float) zoom;
	}

	/*
	 * adds new marker on the map and adds it to the marker holder list
	 */
	private void arrangePlace(PlaceBean placeBean) {
		LatLng latLng = new LatLng(placeBean.getLatitude(), placeBean.getLongitude());
		Marker marker = addLocationMarker(latLng, placeBean.getTitle(), placeBean.getSnippet(), BitmapDescriptorFactory.HUE_RED);
		MarkerHolder markerHolder = new MarkerHolder();
		markerHolder.marker = marker;
		markerHolder.id = placeBean.getId();
		mMarkerHolders.add(markerHolder);

	}


	/*
	 * gets a place list from db and adds markers on the map to the marker
	 * holder list
	 */
	private void arrangeLocateMarkersFromDB() {
		mMarkerHolders = new ArrayList<MarkerHolder>();
		if (mSelectedPlace != null) {
			arrangePlace(mSelectedPlace);
		} else if (mMaxDistance == 0) {
			List<PlaceBean> plaseList = mDataAdapter.selectAll();
			for (PlaceBean placeBean : plaseList) {
				arrangePlace(placeBean);
			}
		} else {
			List<PlaceBean> plaseList = mDataAdapter.selectSurroundings(mCurrentLatLng.latitude, mCurrentLatLng.longitude, mMaxDistance);
			for (PlaceBean placeBean : plaseList) {
				arrangePlace(placeBean);
			}
		}
	}


	/*
	 * adds the marker described by parameters on the map. returns this map
	 */
	private Marker addLocationMarker(LatLng latLng, String title, String snippet, float hue) {

		MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title).snippet(snippet)
				.icon(BitmapDescriptorFactory.defaultMarker(hue));
		return mMap.addMarker(markerOptions);

	}

	private void initCriteria() {
		mCriteria = new Criteria();
		mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
		mCriteria.setPowerRequirement(Criteria.POWER_LOW);
		mCriteria.setAltitudeRequired(false);
		mCriteria.setBearingRequired(false);
		mCriteria.setSpeedRequired(false);
		mCriteria.setCostAllowed(true);
	}


	/*
	 * clears the edited fields to edit marker title and description
	 */
	private void cleanMarkerField() {
		mMarkerTitleEditText.setText("");
		mMarkerDescriptionEditText.setText("");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			DialogCreator.viewDialog(this, R.layout.about);
			return true;
        case R.id.action_map_type:
        	DialogCreator.selectItemDialog(this, R.string.select_map_type, R.array.map_type_items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					int mapType = MAP_TYPES[which];
					if(mapType != mMapType){
						mMapType = mapType;
						mMap.setMapType(mapType);
					}
				}
			});
            return true;
        case R.id.action_edit:
        	editMarker();
        	return true;
        case R.id.action_delete:	
        	deleteMarker(); 
        	return true;
            
        default:
            return super.onOptionsItemSelected(item);
		}
	}
	

	/**
	 * Called when the 'Save' button has been clicked. Saves and recreate new
	 * marker with specified requisites
	 * 
	 * @param v
	 *            the view that was clicked.
	 */
	public void onClickSaveMarker(View v) {
		if (TextUtils.isEmpty(mMarkerTitleEditText.getText().toString()) || TextUtils.isEmpty(mMarkerDescriptionEditText.getText().toString())) {
			return;
		}

		mMarkerInfoLayout.setVisibility(View.GONE);
		mInputMethodManager.hideSoftInputFromWindow(mMarkerInfoLayout.getWindowToken(), 0);

		if (mFocusedMarkerHolder.id == 0) {
			// a new marker
			Marker marker = mFocusedMarkerHolder.marker;
			MarkerHolder markerHolder = new MarkerHolder();

			PlaceBean placeBean = new PlaceBean(0, marker.getPosition().latitude, marker.getPosition().longitude, mMarkerTitleEditText.getText()
					.toString(), mMarkerDescriptionEditText.getText().toString());
			markerHolder.id = mDataAdapter.insert(placeBean);
			if (markerHolder.id > 0) {
				markerHolder.marker = addLocationMarker(marker.getPosition(), mMarkerTitleEditText.getText().toString(), mMarkerDescriptionEditText
						.getText().toString(), BitmapDescriptorFactory.HUE_RED);
				marker.remove();
				mMarkerHolders.add(markerHolder);
			}
		} else {
			// an existing marker
			mFocusedMarkerHolder.marker.setTitle(mMarkerTitleEditText.getText().toString());
			mFocusedMarkerHolder.marker.setSnippet(mMarkerDescriptionEditText.getText().toString());
			Marker marker = mFocusedMarkerHolder.marker;

			PlaceBean placeBean = new PlaceBean(mFocusedMarkerHolder.id, marker.getPosition().latitude, marker.getPosition().longitude,
					mMarkerTitleEditText.getText().toString(), mMarkerDescriptionEditText.getText().toString());
			mDataAdapter.update(placeBean);
		}
		cleanMarkerField();
		mFocusedMarkerHolder.marker.hideInfoWindow();
		mFocusedMarkerHolder = null;
	}

	/**
	 * Called when the 'Cncl' button has been clicked. Hides the requisite
	 * editing fields and removes the marker if it is new.
	 * 
	 * @param v
	 *            the view that was clicked.
	 */
	public void onClickCancel(View v) {
		mMarkerInfoLayout.setVisibility(View.GONE);
		mInputMethodManager.hideSoftInputFromWindow(mMarkerInfoLayout.getWindowToken(), 0);

		if (mFocusedMarkerHolder.id == 0) {
			mFocusedMarkerHolder.marker.remove();
		}
		cleanMarkerField();
		mFocusedMarkerHolder.marker.hideInfoWindow();
		mFocusedMarkerHolder = null;
		mEditMarkerItem.setVisible(false);
		mDeleteMarkerItem.setVisible(false);

	}

	/*
	 * Makes visible the requisite editing fields
	 */
	private void editMarker() {
		if (mFocusedMarkerHolder.id != 0) {
			mMarkerTitleEditText.setText(mFocusedMarkerHolder.marker.getTitle());
			mMarkerDescriptionEditText.setText(mFocusedMarkerHolder.marker.getSnippet());
		}

		mMarkerInfoLayout.setVisibility(View.VISIBLE);
		mEditMarkerItem.setVisible(false);
		mDeleteMarkerItem.setVisible(false);

	}

	/*
	 * deletes the selected marker from the map and database 
	 */
	private void deleteMarker() {
		DialogCreator.questionYesCancelDialog(this, R.string.delete_marker_dlg_title, R.string.delete_marker_dlg_question,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						int count = mDataAdapter.delete(mFocusedMarkerHolder.id);
						if(count > 0){
							
							mEditMarkerItem.setVisible(false);
							mDeleteMarkerItem.setVisible(false);
							mFocusedMarkerHolder.marker.remove();
							mFocusedMarkerHolder = null;
						}
					}
				});
	}

	@Override
	public void onMapClick(LatLng latLng) {
		
		if(mMarkerInfoLayout.getVisibility() == View.VISIBLE){
			return;
		}
		
		mEditMarkerItem.setVisible(false);
		mDeleteMarkerItem.setVisible(false);
		mMarkerInfoLayout.setVisibility(View.GONE);
		
		if (mSelectedPlace != null) {
			mFocusedMarkerHolder = null;
			return;
		}
		

		if ((mFocusedMarkerHolder == null) || (mFocusedMarkerHolder.id > 0)) {
			mFocusedMarkerHolder = new MarkerHolder();
			mFocusedMarkerHolder.marker = addLocationMarker(latLng, "New", null, BitmapDescriptorFactory.HUE_MAGENTA);
			mFocusedMarkerHolder.marker.setDraggable(true);
			mFocusedMarkerHolder.id = 0;

		} else {
			// mFocusedMarkerHolder is unsaved. Move it in new location.
			mFocusedMarkerHolder.marker.setPosition(latLng);
		}
		mFocusedMarkerHolder.marker.showInfoWindow();
		mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
		
		mEditMarkerItem.setVisible(true);
		mEditMarkerItem.setTitle(R.string.add_marker_action_title);
		mEditMarkerItem.setIcon(R.drawable.ic_action_new);

	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		if(mMarkerInfoLayout.getVisibility() == View.VISIBLE){
			return true;
		}
		
		if ((mFocusedMarkerHolder != null) && marker.equals(mFocusedMarkerHolder.marker)) {
			//re-clicking on the same marker
			return false;
		}
		

		if (marker.equals(mCurrentPositionMarker)) {
			// this marker is not editable.
			if ((mFocusedMarkerHolder != null) && (mFocusedMarkerHolder.id == 0)) {
				mFocusedMarkerHolder.marker.remove();
			}
			mFocusedMarkerHolder = null;
			mEditMarkerItem.setVisible(false);
			mDeleteMarkerItem.setVisible(false);
			return false;
		}

		MarkerHolder currentMarkerHolder = null;
		// find for the MarkerHolder that corresponds to the marker
		for (MarkerHolder markerHolder : mMarkerHolders) {
			if (marker.equals(markerHolder.marker)) {
				currentMarkerHolder = markerHolder;
			}
		}

		// an existing marker
		if ((mFocusedMarkerHolder != null) && (mFocusedMarkerHolder.id == 0)) {
			mFocusedMarkerHolder.marker.remove();
			mFocusedMarkerHolder = null;
		}

		if (currentMarkerHolder == null) {
			mDeleteMarkerItem.setVisible(false);
		} else {
			mDeleteMarkerItem.setVisible(true);
		}

		mFocusedMarkerHolder = currentMarkerHolder;
		mEditMarkerItem.setVisible(true);
		mEditMarkerItem.setTitle(R.string.edit_marker_action_title);
		mEditMarkerItem.setIcon(R.drawable.ic_action_edit);
		

		return false;
	}

	/*
	 * holder to hold a map marker and its an id in the database
	 */
	private class MarkerHolder {
		Marker marker;
		long id;
	}

}
