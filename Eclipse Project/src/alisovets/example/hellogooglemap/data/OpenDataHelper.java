package alisovets.example.hellogooglemap.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author Alexander Lisovets, 2014
 *
 */
public class OpenDataHelper extends SQLiteOpenHelper { 
	
	private static final String DATABASE_NAME = "Places.db";
	private static final int DATABASE_VERSION = 1;
	
	Context mContext;
	
	/**
	 * creates OpenDataHelper object
	 * @param context
	 */
	public OpenDataHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db){ 
		db.execSQL(PlaceDao.CREATE_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		//Do nothing. Other versions do not exist yet.
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		db.execSQL("PRAGMA foreign_keys=ON;");
		super.onOpen(db);
	}

}
