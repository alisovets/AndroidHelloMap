package alisovets.example.hellogooglemap;

import alisovets.example.hellogooglemap.data.DataAdapter;
import android.app.Application;
import android.location.Location;

/**
 * Simple demo application to demonstrate of using of some features of
 * location-based services and Google map API. The class initializes and stores
 * some common variables for several activities.
 * 
 * @author Alexander Lisovets 2014
 * 
 */
public class MapApplication extends Application {
	final static String SELECTED_PLACE_KEY = "selected_place";
	final static String CURRENT_LOCAION_KEY = "current_location";
	final static String DISTANCE_KEY = "distance";

	private DataAdapter mDataAdapter;
	private Location mCurrentLocation;

	@Override
	public void onCreate() {
		super.onCreate();
		mDataAdapter = new DataAdapter(getApplicationContext());
	}

	public DataAdapter getDataAdapter() {
		return mDataAdapter;
	}

	public Location getCurrentLocation() {
		return mCurrentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		mCurrentLocation = currentLocation;
	}
	
	
}
