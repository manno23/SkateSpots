package com.manno.android.skatespotsapp.Storage;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.manno.android.skatespotsapp.Utils.Constants;

public class DBHelper extends SQLiteOpenHelper {
	
	private static final int DB_VERSION = 4;
	private static final String DB_NAME = Constants.DATABASE_NAME;

	private static final String CREATE_SPOT_TABLE=
		"CREATE TABLE "+
		Constants.SPOT_TABLE_NAME+" ("+
		Constants.SPOT_ID+" INT PRIMARY KEY, "+
		Constants.USER_ID+" INT, "+
		Constants.SHARING+" INT, "+
		Constants.LONGITUDE+" REAL, "+
		Constants.LATITUDE+" REAL, "+
		Constants.SPOT_NAME+" TEXT, "+
		Constants.ADDRESS+" TEXT, "+
		Constants.NUM_PICS+" INTEGER, "+
		Constants.RATING+" FLOAT, "+
		Constants.VOTE_TALLY+" INT, "+
		Constants.NUM_RATINGS+" INT, "+
		Constants.DESCRIPTION+" TEXT"+
		");";
	private static final String CREATE_USER_RATINGS_TABLE=
		"CREATE TABLE "+
		Constants.USER_RATING_TABLE_NAME+" ("+
		Constants.USER_ID+" INT, "+
		Constants.SPOT_ID+" INT PRIMARY KEY, "+
		Constants.RATED+" SMALLINT DEFAULT "+Constants.RATING_ENUM_UNRATED+","+
		Constants.STATUS+" SMALLINT "+
		");"; 
	private static final String CREATE_SPOT_IMAGE_URI_TABLE=
		"CREATE TABLE "+
		Constants.SPOT_IMAGE_URI_TABLE_NAME+" ("+
		Constants.PICTURE_ID+" INT PRIMARY KEY, "+
		Constants.SPOT_ID+" INT, "+
		Constants.USER_ID+" INT, "+
		Constants.DATE_ENTERED+" DATE, "+
		Constants.PICTURE_CAPTION+" TEXT "+
		");";
	private static final String CREATE_IMAGE_COMMENTS_TABLE=
		"CREATE TABLE "+
		Constants.IMAGE_COMMENTS_TABLE_NAME+" ("+
		Constants.COMMENT_ID+" INT PRIMARY KEY, "+
		Constants.COMMENT+" TEXT, "+
		Constants.CREATOR_NAME+" TEXT, "+
		Constants.CREATOR_ID+" INT, "+
		Constants.PICTURE_ID+" INT, "+
		Constants.DATE_ENTERED+" DATE "+
		");";
    private static final String CREATE_NEWS_ITEMS_TABLE=
        "CREATE TABLE "+
        Constants.NEWS_ITEM_TABLE_NAME+" ("+
        Constants.NEWS_ITEM_ID+" INT PRIMARY KEY,"+
        Constants.NEWS_ITEM_CLIENT_ID+" INT,"+
        Constants.NEWS_ITEM_LINK_URL+" TEXT,"+
        Constants.NEWS_ITEM_PICTURE_URL+" TEXT,"+
        Constants.NEWS_ITEM_CREATION_DATE+" DATE);";

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SPOT_TABLE);
		db.execSQL(CREATE_USER_RATINGS_TABLE);
		db.execSQL(CREATE_SPOT_IMAGE_URI_TABLE);
		db.execSQL(CREATE_IMAGE_COMMENTS_TABLE);
        db.execSQL(CREATE_NEWS_ITEMS_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		// Log the version upgrade.
		Log.d("TEST", "Upgrading from version " +
                oldVersion + " to " +
                newVersion);

		db.execSQL("DROP TABLE IF EXISTS " + Constants.SPOT_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.USER_RATING_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.SPOT_IMAGE_URI_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.IMAGE_COMMENTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.NEWS_ITEM_TABLE_NAME);
		onCreate(db);
	}

}
