package alisovets.example.hellogooglemap.data;

import java.util.List;

import alisovets.example.hellogooglemap.dto.PlaceBean;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * Simpe data adapter class
 * @author Alexander Lisovets 2014
 *
 */
public class DataAdapter {
	
	private SQLiteDatabase mDb;
	private OpenDataHelper mDbHelper;
	private PlaceDao mPlaceDao;
	

	public DataAdapter(Context context) {
		mDbHelper = new OpenDataHelper(context);
		mDb = mDbHelper.getWritableDatabase();
		mPlaceDao = new PlaceDao();	
		mPlaceDao.setDb(mDb);
	}


	public void close() {
		mDbHelper.close();
	}

	public void beginTransaction(){
		mDb.execSQL("BEGIN;");
	}
	
	public void endTransaction(){
		mDb.execSQL("END;");
	}
	
	public void rollbackTransaction(){ 
		mDb.execSQL("ROLLBACK;");
	}	
	
	/**
	 * inserts one place record in the database 
	 * @param place 
	 * @return id of the record or -1 if insert is fail
	 */
	public long insert(PlaceBean place){
		return mPlaceDao.insert(place);
	}
	
	/**
	 * update the place record in the database 
	 * @param place  
	 * @return the number of rows affected
	 */
	public int update(PlaceBean place){
		return mPlaceDao.update(place);
	}
	
	/**
	 * @return the list of all place records.  
	 */
	public List<PlaceBean> selectAll(){
		return mPlaceDao.selectAll();
	}
	

	/**
	 * @return places which are not further the specified distance from the specified position that is defined by latitude and longitude
	 * @param latitude  of the center position
	 * @param longitude  of the center position
	 * @param distance - max distance for returned objects
	 */
	public List<PlaceBean> selectSurroundings(double latitude, double longitude, double distance){
		return mPlaceDao.selectSurroundings(latitude, longitude, distance);
	}
	
	/**
	 * @return number of rows in the table.  
	 */
	public int count() {
		return  mPlaceDao.count();
	}
	
	/**
	 * delete record with specified id
	 * @param id
	 * @return the number of rows affected
	 */
	public int delete(long id) {
		return  mPlaceDao.delete(id);
	}
}
