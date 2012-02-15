package com.manno.android.skatespotsapp.Storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.manno.android.skatespotsapp.NewsFragment;
import com.manno.android.skatespotsapp.Utils.Constants;
import com.manno.android.skatespotsapp.Utils.Spot;
import com.manno.android.skatespotsapp.Utils.SpotPicture;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Locale;

public class MyDB {
	
	private SQLiteDatabase db;
	private DBHelper dbHelper;

	private static final String TAG = "MyDB";
	
	
	public MyDB(Context c) {
		dbHelper = new DBHelper(c);
	}
	
	public void open() throws SQLiteException {
		try {
			db = dbHelper.getWritableDatabase();
		} catch(SQLiteException e) {
			Log.v("Open database exception caught", e.getMessage());
			db = dbHelper.getReadableDatabase();
		}
		
		db.setLocale(Locale.getDefault());
		//I'm not certain of whether the db is accessed by multiple threads i'll have to check
		//..which should be pretty easy if i just query the thread in this method lol
		db.setLockingEnabled(true);
	}
	
	public void close() {
		db.close();
		db = null;
	}

	
	public long insertSpot(Spot spot) {
		ContentValues cv = new ContentValues();
		cv.put(Constants.SPOT_ID, spot.getSid());
		cv.put(Constants.LATITUDE, spot.getLatitude());
		cv.put(Constants.LONGITUDE, spot.getLongitude());
		cv.put(Constants.SPOT_NAME, spot.getName());
		cv.put(Constants.ADDRESS, spot.getAddress());
		cv.put(Constants.RATING, spot.getRating());
		cv.put(Constants.USER_ID, spot.getUid());
		cv.put(Constants.SHARING, spot.getSharing());
		cv.put(Constants.VOTE_TALLY, spot.getTally());
		cv.put(Constants.NUM_RATINGS, spot.getVoteCount());
		cv.put(Constants.DESCRIPTION, spot.getDescription());
		Log.d(TAG, spot.toString());
		return db.insert(Constants.SPOT_TABLE_NAME, null, cv);	
	}
	
	public void insertRating(int sid, String rating ) {
		ContentValues cv = new ContentValues();
		cv.put(Constants.SPOT_ID, sid);
		if(rating.contentEquals("GOOD")) {
			Log.d(TAG, "GOOD");
			cv.put(Constants.RATED, Constants.RATING_ENUM_LIKE); }
		if(rating.contentEquals("SHIT")) {
			Log.d(TAG, "SHIT");
			cv.put(Constants.RATED, Constants.RATING_ENUM_DISLIKE);
		}		
		db.insert(Constants.USER_RATING_TABLE_NAME, null, cv);
	}

	
	public Cursor retrieveAll() {
		String[] columns = {Constants.SPOT_ID, Constants.LATITUDE, Constants.LONGITUDE, Constants.SPOT_NAME, Constants.ADDRESS, Constants.RATING };
		return db.query(Constants.SPOT_TABLE_NAME, columns, null, null, null, null, null);
	}
	
	public LinkedList<Spot> retrieveSpots(GeoPoint topLeftCornerReference, GeoPoint bottomRightCornerReference, int zoomLvl) {
		double latTop = (double)topLeftCornerReference.getLatitudeE6()/1E6;
		double latBottom = (double)bottomRightCornerReference.getLatitudeE6()/1E6;
		double longLeft = (double)topLeftCornerReference.getLongitudeE6()/1E6;
		double longRight = (double)bottomRightCornerReference.getLongitudeE6()/1E6;
		String where = null;;
		if(longLeft > 0 && longRight < 0) {
			longRight = Math.abs(longRight);
			if(longLeft > longRight)
				where = "abs("+Constants.LONGITUDE+") > " + longRight;
			else
				where = "abs("+Constants.LONGITUDE+") > " + longLeft;
		}
		else {
			where = Constants.LONGITUDE + " < " + longRight + " AND " + Constants.LONGITUDE + " > " + longLeft;
		}
		if(zoomLvl < 9)
			where = where + " AND " + Constants.RATING + " = 5 LIMIT 50";
		else if(zoomLvl < 11)
			where = where + " AND " + Constants.RATING + " >= 3 LIMIT 50";
		else if(zoomLvl < 15)
			where = where + " AND " + Constants.RATING + " >= 2 LIMIT 50";
		else if(zoomLvl < 18)
			where = where + " AND " + Constants.RATING + " >= 0";
		where = Constants.LATITUDE + " > " + latBottom + " AND " + Constants.LATITUDE + " < " + latTop + " AND " + where; 
		Log.d(TAG, where);
		String[] columns = {Constants.SPOT_ID, Constants.LATITUDE, Constants.LONGITUDE,
				Constants.SPOT_NAME, Constants.ADDRESS, Constants.RATING, 
				Constants.USER_ID, Constants.SHARING, Constants.VOTE_TALLY, Constants.NUM_RATINGS,
				Constants.DESCRIPTION};
		Cursor c = db.query(Constants.SPOT_TABLE_NAME, columns, where, null, null, null, null);
		LinkedList<Spot> spotList = new LinkedList<Spot>();
		if(c.moveToFirst()) {
    		do {	
    			spotList.add(new Spot(
						c.getInt(0),     //sid
    					c.getDouble(1),  //latitude
    					c.getDouble(2),  //longitude
    					c.getString(3),  //name
    					c.getString(4),  //address
    					c.getFloat(5),   //rating
    					c.getLong(6),	  //uid
    					c.getInt(7),     //sharing 
    					c.getInt(8),	  //vote tally
						c.getInt(9), 	  //number of votes
						c.getString(10)) //description         			
    			);
    		} while(c.moveToNext());
    	}
    	c.close();
		
		return spotList;
	}

	
	public int getMySpotRating(int spotID) {
		String[] columns = {Constants.RATED};
		open();
		Cursor c = db.query(Constants.USER_RATING_TABLE_NAME, columns, Constants.SPOT_ID + "=" + spotID, null, null, null, null);
		int result;
		//if spot has been rated and exists in database retrieve the users rating
		if(c.moveToFirst()) {			
			result = c.getInt(0);
			Log.d(TAG, "DB retrieved Entry of " + result + " for " + spotID);
		} else {
			ContentValues cv = new ContentValues();
			cv.put(Constants.SPOT_ID, spotID);
			cv.put(Constants.RATED, Constants.RATING_ENUM_UNRATED);
			cv.put(Constants.STATUS, Constants.RATING_CONFIRMED_UPDATED);
			db.insert(Constants.USER_RATING_TABLE_NAME, null, cv);
			result =  Constants.RATING_ENUM_UNRATED;
		}
		Log.d(TAG, "spot rating recieved with " + result);
		c.close();
		close();
		return result;
	}
	
	public void setRating(Spot spot, int rating) {
		long sid = spot.getSid();
		//Set users rating
		ContentValues cv = new ContentValues();
		cv.put(Constants.RATED, rating);
		db.update(Constants.USER_RATING_TABLE_NAME, cv, Constants.SPOT_ID+"="+sid, null);
		//Set updated spot rating
		cv.clear();
		cv.put(Constants.RATING, spot.getRating());
		cv.put(Constants.VOTE_TALLY, spot.getTally());		
		cv.put(Constants.NUM_RATINGS, spot.getVoteCount());
		int result = db.update(Constants.SPOT_TABLE_NAME, cv, Constants.SPOT_ID+"="+sid, null);
		if(result == 1)
			Log.d(TAG, "Successfully updated spot" + spot.getRating() + " " + spot.getVoteCount());
		else
			Log.d(TAG, "Update sql unsuccessfuk");
	}


    public ArrayList<NewsFragment.NewsItem> getNewsItems() {
        ArrayList<NewsFragment.NewsItem> newsItems = new ArrayList<NewsFragment.NewsItem>();
        open();
        // Grab everything from the news items
        Cursor c = db.query(Constants.NEWS_ITEM_TABLE_NAME, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                newsItems.add(new NewsFragment.NewsItem(
                        c.getLong(c.getColumnIndex(Constants.NEWS_ITEM_ID)),
                        c.getString(c.getColumnIndex(Constants.NEWS_ITEM_CREATION_DATE)),
                        c.getString(c.getColumnIndex(Constants.NEWS_ITEM_PICTURE_URL)),
                        c.getString(c.getColumnIndex(Constants.NEWS_ITEM_LINK_URL)),
                        c.getLong(c.getColumnIndex(Constants.NEWS_ITEM_CLIENT_ID))));
            } while (c.moveToNext());
        }
        c.close();
        close();
        return newsItems;
    }
    
    public void insertNewsItem(long id, String date, long client_id, String link_url, String picture_url) {
        ContentValues values = new ContentValues();
        values.put(Constants.NEWS_ITEM_ID, id);
        values.put(Constants.NEWS_ITEM_CREATION_DATE, date);
        values.put(Constants.NEWS_ITEM_CLIENT_ID, client_id);
        values.put(Constants.NEWS_ITEM_LINK_URL, link_url);
        values.put(Constants.NEWS_ITEM_PICTURE_URL, picture_url);
        open();
        db.insert(Constants.NEWS_ITEM_TABLE_NAME, null, values);
        close();
    }

	public void storePicture(SpotPicture picture) {
		Log.d("ViewSpotActivity", "Storing " + picture.toString());
		ContentValues cv = new ContentValues();
		cv.put(Constants.PICTURE_ID, picture.getImageID());
		cv.put(Constants.SPOT_ID, picture.getSpotID());
		cv.put(Constants.USER_ID, picture.getCreatorID());
		cv.put(Constants.DATE_ENTERED, picture.getDate());
		cv.put(Constants.PICTURE_CAPTION, picture.getCaption());
		open();
		long insert = db.insert(Constants.SPOT_IMAGE_URI_TABLE_NAME, null, cv);
		Log.d("ViewSpotActivity", "Insert returns" + insert);
		close();
		cv.clear();
	}
	
	public LinkedHashSet<SpotPicture> getPictures(int spotID) {
		String where = Constants.SPOT_ID+"="+spotID;
		open();
		Cursor c = db.query(Constants.SPOT_IMAGE_URI_TABLE_NAME, null, where, null, null, null, null);
		Log.d("ViewSpotActivity", "" + c.getCount());
		LinkedHashSet<SpotPicture> set = new LinkedHashSet<SpotPicture>();
		//retrieves them in a reverse order to create the same LinkedHashSet order as was saved to the DB
		if(c.moveToFirst()) {
			 do {
				set.add(new SpotPicture(
						c.getLong(c.getColumnIndex(Constants.PICTURE_ID)),
						c.getLong(c.getColumnIndex(Constants.USER_ID)),
						c.getInt(c.getColumnIndex(Constants.SPOT_ID)),
						c.getString(c.getColumnIndex(Constants.DATE_ENTERED)),
						c.getString(c.getColumnIndex(Constants.PICTURE_CAPTION))  ));
			} while(c.moveToNext());
		} else
			Log.d("ViewSpotActivity", "DB Query for spot returned null");
		c.close();
		close();
		return set;
	}
	
	public void removePicture(long pictureID) {
		open();
		int result = db.delete(Constants.SPOT_IMAGE_URI_TABLE_NAME, Constants.PICTURE_ID + "=" + pictureID, null);
		Log.d("ViewSpotActivity", "Deletion returned" + result);
		close();	
	}

/*
	public boolean removeSpot(int spotID) {
		open();
		int result = db.delete(Constants.SPOT_TABLE_NAME, "SID = "+spotID, null);
		close();
		if(result!=0) return true;
		else return false;
	}
	
	Comments
	public void storeComments(List<Comment> comments) {
		open();
		ContentValues values = new ContentValues();
		for(Comment comment : comments) {
			values.put(Constants.COMMENT_ID, comment.getCommentID());
			values.put(Constants.COMMENT, comment.getComment());
			values.put(Constants.CREATOR_NAME, comment.getName());
			values.put(Constants.CREATOR_ID, comment.getCreatorID());
			values.put(Constants.PICTURE_ID, comment.getPictureID());
			values.put(Constants.DATE_ENTERED, comment.getDate());
			db.insert(Constants.IMAGE_COMMENTS_TABLE_NAME, null, values);
		}
		close();
	}

	public void storeComment(Comment comment) {
		open();
		ContentValues values = new ContentValues();
		values.put(Constants.COMMENT_ID, comment.getCommentID());
		values.put(Constants.COMMENT, comment.getComment());
		values.put(Constants.CREATOR_NAME, comment.getName());
		values.put(Constants.CREATOR_ID, comment.getCreatorID());
		values.put(Constants.PICTURE_ID, comment.getPictureID());
		values.put(Constants.DATE_ENTERED, comment.getDate());
		db.insert(Constants.IMAGE_COMMENTS_TABLE_NAME, null, values);
		close();	
	}
	
	public List<Comment> getPictureComments(long pictureID) {
		open();
		String where = Constants.PICTURE_ID + " = " + pictureID;
		Cursor c = db.query(Constants.IMAGE_COMMENTS_TABLE_NAME, null, where, null, null, null, Constants.DATE_ENTERED+" DESC");
		ArrayList<Comment> comments = new ArrayList<Comment>();
		if(c.moveToFirst()) {
			do {
				comments.add(new Comment(
						c.getLong(c.getColumnIndex(Constants.COMMENT_ID)),
						c.getString(c.getColumnIndex(Constants.COMMENT)),
						c.getString(c.getColumnIndex(Constants.CREATOR_NAME)),
						c.getLong(c.getColumnIndex(Constants.CREATOR_ID)),
						c.getLong(c.getColumnIndex(Constants.PICTURE_ID)),
						c.getString(c.getColumnIndex(Constants.DATE_ENTERED))));
			} while(c.moveToNext());
		}
		c.close();
		close();
		return comments;
	}
	
	public boolean deleteComment(long commentID) {
		open();
		int result = db.delete(Constants.IMAGE_COMMENTS_TABLE_NAME, Constants.COMMENT_ID+" = "+commentID, null);
		close();
		if(result!=0) return true;
		else return false;
	}
	 */

	
	public void clear() {
		db.delete(Constants.SPOT_TABLE_NAME, null,null);
		db.delete(Constants.USER_RATING_TABLE_NAME, null, null);
		db.delete(Constants.SPOT_IMAGE_URI_TABLE_NAME, null, null);
		db.delete(Constants.IMAGE_COMMENTS_TABLE_NAME, null, null);
	}






}
