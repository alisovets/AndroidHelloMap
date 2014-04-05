package alisovets.example.hellogooglemap.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import alisovets.example.hellogooglemap.dto.PlaceBean;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * DAO to store PlaseBean objects in database 
 * @author Alexander Lisovets 2014
 *
 */
public class PlaceDao {
	public final static String TAG = "PlaceDao";

	public final static String TABLE_NAME = "places";
	public final static String ID_COLUMN = "id";
	public final static String LONGITUDE_COLUMN = "longitude";
	public final static String LATITUDE_COLUMN = "latitude";
	public final static String TITLE_COLUMN = "title";
	public final static String SNIPPET_COLUMN = "snippet";
	public final static String ALL_COLUMNS = "id, longitude, latitude, title, snippet ";
	private final static double RADIANS_IN_DEGREE = Math.PI / 180;
	public final static double KM_PER_DEGREE = 111.2;

	public static final String CREATE_TABLE_SQL = "create table " + TABLE_NAME + "( " + ID_COLUMN
			+ " integer primary key autoincrement, " + LONGITUDE_COLUMN + " double not null, " + LATITUDE_COLUMN
			+ " double not null, " + TITLE_COLUMN + " text not null, " + SNIPPET_COLUMN + " text " + ");";

	private final static String SELECT_SQL = "select " + ALL_COLUMNS + " from " + TABLE_NAME + " where id=?;";
	private final static String SELECT_COUNT_SQL = "select count(1) from " + TABLE_NAME + ";";

	private SQLiteDatabase mDb;

	/*
	 * lng - longitude, lat - latitude, maxdegree - square of maximum deviation
	 * in degrees, without correction for the latitude, coslat = cos(latitude)
	 */
	private final static String SELECT_SURROUNDING_SQL = "select "
			+ ALL_COLUMNS
			+ "from "
			+ "(select id,  longitude, latitude, title, snippet, abs(longitude - par.lng) * coslat  as diflng, abs(latitude - par.lat) as diflat "
			+ "from " + "places, (select (%f) as lng, (%f) as lat , (%f) as maxdegree, (%f) as coslat ) par "
			+ "where " + "(diflng * diflng + diflat * diflat  < par.maxdegree) "
			+ "or ((diflng > 350) and ((360 - diflng) * (360 - diflng) + diflat * diflat < par.maxdegree)) " + ");";
	
	private final static String SELECT_ALL = "select " + ALL_COLUMNS + " from " + TABLE_NAME + " order by title;";

	public void setDb(SQLiteDatabase db) {
		mDb = db;
	}

	/**
	 * inserts one place record in the database 
	 * @param place 
	 * @return id of the record or -1 if insert is fail
	 */
	public long insert(PlaceBean place) {
		ContentValues values = new ContentValues();
		values.put(LONGITUDE_COLUMN, place.getLongitude());
		values.put(LATITUDE_COLUMN, place.getLatitude());
		values.put(TITLE_COLUMN, place.getTitle());
		values.put(SNIPPET_COLUMN, place.getSnippet());

		long id = mDb.insert(TABLE_NAME, null, values);
		if (id > 0) {
			Log.d(getClass().getName(), "Place inserted with id: " + id);
		} else {
			Log.d(TAG,
					"Failed to add the place to the database. lat: " + place.getLongitude() + ". lon: "
							+ place.getLatitude() + ", title: '" + place.getTitle() + "', snip: '" + place.getSnippet()
							+ "'");
		}
		return id;
	}

	/**
	 * update the place record in the database 
	 * @param place  
	 * @return the number of rows affected
	 */
	public int update(PlaceBean place) {
		if (place.getId() <= 0) {
			throw new IllegalArgumentException("Study id is empty!");
		}

		ContentValues values = new ContentValues();
		values.put(LONGITUDE_COLUMN, place.getLongitude());
		values.put(LATITUDE_COLUMN, place.getLatitude());
		values.put(TITLE_COLUMN, place.getTitle());
		values.put(SNIPPET_COLUMN, place.getSnippet());

		int count = mDb.update(TABLE_NAME, values, ID_COLUMN + "=" + place.getId(), null);
		if(count == 1){
			Log.d(getClass().getName(), "Place updated with id: " + place.getId());
		}
		else{
			Log.d(TAG,
					"Failed to update the place with id: " + place.getId() + " to the database. lat: " + place.getLongitude() + ". lon: "
							+ place.getLatitude() + ", title: '" + place.getTitle() + "', snip: '" + place.getSnippet()
							+ "'");
		}
		
		return count;
	}

	/**
	 * delete record with specified id
	 * @param id
	 * @return the number of rows affected
	 */
	public int delete(long id) {
		int count = mDb.delete(TABLE_NAME, ID_COLUMN + " = " + id, null);
		Log.d(getClass().getName(), "Place with id: " + id + " deleted from db");
		return count;
	}

	/**
	 * delete record in the table 
	 * @return the number of rows affected
	 */
	public int deleteAll() {
		int count = mDb.delete(TABLE_NAME, "1", null);
		Log.d(getClass().getName(), count + " (all) places deleted from db");
		return count;
	}

	/**
	 * @return the list of all place records.  
	 */
	public List<PlaceBean> selectAll() {
		List<PlaceBean> places = new ArrayList<PlaceBean>();
		Cursor cursor = mDb.rawQuery(SELECT_ALL, null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				PlaceBean place = cursorToPlace(cursor);
				places.add(place);
				cursor.moveToNext();
			}
		} finally {
			cursor.close();
		}
		return places;
	}

	/**
	 * @return the place record with specified id  or null if it not exist.  
	 */
	public PlaceBean select(long id) {
		String sql = SELECT_SQL;
		String[] args = { "" + id };
		PlaceBean place = null;
		Cursor cursor = mDb.rawQuery(sql, args);
		try {
			cursor.moveToFirst();
			place = cursorToPlace(cursor);
		} finally {
			cursor.close();
		}
		return place;
	}


	/**
	 * @return places which are not further the specified distance from the specified position that is defined by latitude and longitude
	 * @param latitude of the center position
	 * @param longitude of the center position
	 * @param distance - max distance for returned objects
	 */
	public List<PlaceBean> selectSurroundings(double latitude, double longitude, double distance) {
		String fullSql = String.format(Locale.US, SELECT_SURROUNDING_SQL, longitude, latitude, distance / KM_PER_DEGREE
				* distance / KM_PER_DEGREE, Math.cos(latitude * RADIANS_IN_DEGREE));
		List<PlaceBean> places = new ArrayList<PlaceBean>();
		Cursor cursor = mDb.rawQuery(fullSql, null);
		try {
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				PlaceBean place = cursorToPlace(cursor);
				places.add(place);
				cursor.moveToNext();
			}
		} finally {
			cursor.close();
		}
		return places;
	}

	
	/**
	 * @return number of rows in the table.  
	 */
	public int count() {
		long count = 0;
		Cursor cursor = mDb.rawQuery(SELECT_COUNT_SQL, null);
		try {
			cursor.moveToFirst();
			count = cursor.getLong(0);
		} finally {
			cursor.close();
		}
		return (int)count;
	}

	/**
	 * Creates and returns a PlaseBean object that initialized of the fields of the specified cursor. 
	 * @param cursor
	 */
	private PlaceBean cursorToPlace(Cursor cursor) {
		if (cursor.getCount() == 0) {
			return null;
		}
		PlaceBean place = new PlaceBean();
		place.setId(cursor.getLong(0));
		place.setLongitude(cursor.getDouble(1));
		place.setLatitude(cursor.getDouble(2));
		place.setTitle(cursor.getString(3));
		place.setSnippet(cursor.getString(4));
		return place;
	}

}
