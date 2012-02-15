package com.manno.android.skatespotsapp.ViewSpot;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.manno.android.skatespotsapp.R;
import com.manno.android.skatespotsapp.Service.BackgroundDataSync;
import com.manno.android.skatespotsapp.Service.ServiceHelper;
import com.manno.android.skatespotsapp.Storage.MyDB;
import com.manno.android.skatespotsapp.Utils.Constants;
import com.manno.android.skatespotsapp.Utils.Spot;
import com.manno.android.skatespotsapp.Utils.SpotPicture;
import com.manno.android.skatespotsapp.Utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ViewSpotActivity extends FragmentActivity implements View.OnClickListener, ServiceHelper.Receiver {
	
	private TextView name;
	private TextView ratingDisplay;
	private TextView voteCount;
	private ImageButton like;
	private ImageButton dislike;
    private ImageAdapter adapter;
    private ViewSwitcher switcher;
	private Gallery pictureGallery;
	
	private Spot spot;
	private Spot oldSpot;
	private long uid;
	private int my_rating;
	private int spotNumber;
	private float rating;
	private boolean atSpot;
	private MyDB db;
	private Uri mCapturedImageURI;
	private ServiceHelper helper;
	
	private static final String TAG = "ViewSpotActivity";
	private static final int PICK_PICTURE_CAMERA_REQUEST  = 1;
	private static final int START_PICTURE_ACTIVITY = 2;
	public static final int RATING_SUCCESSFUL = 0x1;
	public static final int RATING_NOT_SUCCESSFUL = 0x2;
	public static final int PHOTO_UPLOAD_SUCCESSFUL = 0x3;
	public static final int PHOTO_UPLOAD_NOT_SUCCESSFUL = 0x4;
	public static final int PHOTO_INFORMATION_DOWNLOAD_SUCCESFUL = 0x5;
	public static final int DELETE_SPOT_SUCCESFUL = 0x6;
	public static final int DELETE_SPOT_UNSUCCESFUL = 0x7;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_spot);
		
		name = (TextView)findViewById(R.id.name);
		ratingDisplay = (TextView)findViewById(R.id.rating_display);
		voteCount = (TextView)findViewById(R.id.vote_count);
		like = (ImageButton)findViewById(R.id.like_button);
		dislike = (ImageButton)findViewById(R.id.dislike_button);
		switcher = (ViewSwitcher)findViewById(R.id.gallery_switcher);
		helper = new ServiceHelper(new Handler());
		helper.setReceiver(this);

		
		//Set up Gallery View
 	    adapter = new ImageAdapter(this);
		pictureGallery = (Gallery) findViewById(R.id.gallery);
		//pictureGallery.setSpacing(23);
	    pictureGallery.setAdapter(adapter);

	    //Set up state, using calling activity and application state
		spot = (Spot)getIntent().getParcelableExtra("Spot");
		atSpot = getIntent().getBooleanExtra("onSpot", false); 
		name.setText(spot.getName());
		voteCount.setText(spot.getVoteCount()+" ratings");
		spotNumber = spot.getSid();
		rating = spot.getRating();
//		uid = ((SkateSpotSession)getApplicationContext()).getUid();
		
		db = new MyDB(this);
		my_rating = db.getMySpotRating(spotNumber);	
		
	    //Set up Rating view
		ratingDisplay.setText(String.valueOf(rating).substring(0,3));
		setRatingColour(rating);

		// TODO if called from screen orientaion change skip this
		if(savedInstanceState==null) {
			//Retrieve SpotPictures
		    Intent getPictures = new Intent(this, BackgroundDataSync.class);
		    getPictures.putExtra("action", BackgroundDataSync.SPOT_PHOTOS_DOWNLOAD);
		    getPictures.putExtra("callback", helper);
		    Bundle data = new Bundle();
		    data.putInt("sid", spotNumber);
		    getPictures.putExtras(data);
		    startService(getPictures);
		} else {
			for(SpotPicture picture : db.getPictures(spotNumber)) {
				adapter.setPicture(picture);
			}
	    	switcher.showNext();
		}
	}


	/* 
	 * @ onResume()  
	 * Rating onClickListeners here because a rating 
	 * may need to update the state of the buttons.
	 */
	@Override
	protected void onResume() {
		super.onResume();
	    
		if(uid != 0) {
			switch(my_rating) {
				case Constants.RATING_ENUM_UNRATED:
					like.setOnClickListener(this);
					dislike.setOnClickListener(this);
					like.setImageResource(R.drawable.like_selected);
					dislike.setImageResource(R.drawable.dislike_selected);
					break;
				case Constants.RATING_ENUM_LIKE:
					like.setImageResource(R.drawable.like_selected);
					break;
				case Constants.RATING_ENUM_DISLIKE:
					dislike.setImageResource(R.drawable.dislike_selected);
					break;
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICK_PICTURE_CAMERA_REQUEST) {
			switch(resultCode) {
				case RESULT_OK:
					
				    String[] projection = {MediaStore.Images.Media.DATA};
				    if(mCapturedImageURI==null)
				    	Log.d(TAG, "ImageURI is null");
					Cursor cursor = managedQuery(mCapturedImageURI, projection, null, null, null);
				    cursor.moveToFirst();
				    int column_index_data = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
				    String path = cursor.getString(column_index_data);
				        
				    //Create new 
				    SpotPicture newPhoto = 
				    	new SpotPicture(constructPictureID(),
				    					uid,
				    					spotNumber,
				    					Utils.getCurrentUTCTime(),
				    					"");
				    adapter.setPicture(newPhoto);

				    File sourceFile = new File(path);
				    File cacheFile = new File(getCacheDir(), String.valueOf(newPhoto.getImageID()));
					try {
						copyFile(sourceFile, cacheFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				    MyDB db = new MyDB(this);
				    db.storePicture(newPhoto);
				    db = null; 
				    
					Intent uploadPhoto = new Intent(this, BackgroundDataSync.class);
					uploadPhoto.putExtra("path", cacheFile.getAbsolutePath());
					uploadPhoto.putExtra("SpotID", spotNumber);
					uploadPhoto.putExtra("pictureID", newPhoto.getImageID());
					uploadPhoto.putExtra("callback", helper);
					uploadPhoto.putExtra("action", BackgroundDataSync.UPLOAD_PICTURE);
					startService(uploadPhoto);
					Log.d(TAG, "photo successful");
					break;
					
				case RESULT_CANCELED:
					getContentResolver().delete(mCapturedImageURI, null, null);
					break;
			}
		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(!savedInstanceState.isEmpty()) {
			mCapturedImageURI = (Uri)savedInstanceState.getParcelable("uri");
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mCapturedImageURI!=null) {
			outState.putParcelable("uri", mCapturedImageURI);
		}
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "ViewSpotActivity object destroyed");
		adapter.imageloader.stopThread();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.viewspot_menu, menu);
	    
//	    if((uid != 0) && atSpot) menu.setGroupEnabled(R.id.user_logged_in, true);
//	    if(uid == spot.getUid()) menu.setGroupEnabled(R.id.user_created_spot, true);
	    return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
        
        
		switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
//			case R.id.menu_addphoto:
//				takePhoto();
//				break;
//		    case R.id.delete_spot:
//		    	AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Do you want to delete your spot?").create();
//				dialog.setIcon(R.drawable.icon);
//				dialog.setButton(DialogInterface.BUTTON1, "Yes", new OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//				    	Intent delete = new Intent(ViewSpotActivity.this, BackgroundDataSync.class);
//				    	delete.putExtra("callback", helper);
//				    	delete.putExtra("action", BackgroundDataSync.DELETE_SPOT);
//				    	Bundle bundle = new Bundle();
//				    	bundle.putInt("spotID", spotNumber);
//				    	delete.putExtras(bundle);
//				    	startService(delete);
//					}
//				});
//				dialog.setButton(DialogInterface.BUTTON2, "No", new OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.cancel();
//					}
//				});
//				dialog.show();
//
//		    	break;
		}
		return super.onOptionsItemSelected(item);
	}
 
	@Override public void onClick(View v) {
		switch(v.getId()) {
			case R.id.like_button:
				liked();
				break;
			case R.id.dislike_button:
				disliked();
				break;		
		}
	}
//
	
	private void liked() {
		my_rating = Constants.RATING_ENUM_LIKE;					
		like.setImageResource(R.drawable.like_selected);
		dislike.setImageResource(R.drawable.dislike_greyed);
		like.setClickable(false);
		dislike.setClickable(false);
		updateRatings();		
	}

	private void disliked() {
		my_rating = Constants.RATING_ENUM_DISLIKE;
		like.setImageResource(R.drawable.like_greyed);
		dislike.setImageResource(R.drawable.dislike_selected);
		like.setClickable(false);
		dislike.setClickable(false);
		updateRatings();
	}

	private void updateRatings() {

		oldSpot = new Spot(spot);
		//1.Using number of votes + current vote determine new ranking
		//2.Update rating view and number of votes
		float tally = spot.getTally();
		float vote_count = spot.getVoteCount();
		if(my_rating == Constants.RATING_ENUM_LIKE) {
			if(vote_count < 25.0) {
				rating = rating + 0.1f;
			} else {
				rating = 2.5f + 2.5f*(tally+1.0f)/(vote_count+1.0f);
			}
			spot.setTally((int)tally+1);
		} else {
			if(vote_count < 25.0) {
				rating = rating - 0.1f;
			} else {
				rating = 2.5f + 2.5f*(tally-1.0f)/(vote_count+1.0f);
			}
			spot.setTally((int)tally-1);
		}
		spot.setRating(rating);
		spot.incrementVoteCount();
		
		Log.d(TAG, "Tally: " + tally + " count: " + vote_count);
		ratingDisplay.setText(String.valueOf(rating).substring(0,3));
		setRatingColour(rating);
		voteCount.setText(spot.getVoteCount()+" ratings");
		
		Intent startUpdate = new Intent(ViewSpotActivity.this, BackgroundDataSync.class);
		startUpdate.putExtra("action", BackgroundDataSync.RATE_SPOT);
		startUpdate.putExtra("callback", helper);	
		startUpdate.putExtra("user_rating", my_rating);
		startUpdate.putExtra("spot", spot);
		startService(startUpdate);
	}
	
	private void takePhoto() {
		
		String pictureID = String.valueOf(System.currentTimeMillis());
		ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, pictureID);
        mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Log.d(TAG, mCapturedImageURI.toString());
        
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
	        //Create intent to start default camera and start it for result
				//Get orientation of picture to set image orientation
			Intent startCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startCamera.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
			startCamera.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, getScreenOrientation());
			startActivityForResult(startCamera, PICK_PICTURE_CAMERA_REQUEST );
		} else {
			Toast noSD = Toast.makeText(this, "No external storage available to take picture", Toast.LENGTH_SHORT);
			noSD.show();
		}
		
	}
	
	@Override public void onReceiveResult(int resultCode, Bundle resultData) {
		MyDB db = new MyDB(this);
		Toast toast;

		switch(resultCode) {
			case RATING_SUCCESSFUL:
				//update state to updatedSpot
				oldSpot = null;
				Log.d(TAG, "rating successful");
				break;
			case RATING_NOT_SUCCESSFUL:
				spot = oldSpot;
				oldSpot = null;
				toast = Toast.makeText(this, "Server error: Rating not updated", Toast.LENGTH_SHORT);
				toast.show();
				//Replace rating and count with previous before fail
				rating = spot.getRating();
				ratingDisplay.setText(String.valueOf(rating).substring(0,3));
				setRatingColour(rating);
				voteCount.setText(spot.getVoteCount()+" ratings");
				//Reset buttons
				like.setImageResource(R.drawable.like_selected);
				dislike.setImageResource(R.drawable.dislike_selected);
				like.setClickable(true);
				dislike.setClickable(true);
				Log.d(TAG, "rating not successful");
				break;
			case PHOTO_UPLOAD_SUCCESSFUL:
				toast = Toast.makeText(this, "Upload successful.", Toast.LENGTH_SHORT);
				toast.show();
				break;
			case PHOTO_UPLOAD_NOT_SUCCESSFUL:
				Log.d(TAG, "photo unsuccessful");
				break;	
			//Picture download successful
			case PHOTO_INFORMATION_DOWNLOAD_SUCCESFUL:
				for(SpotPicture picture : db.getPictures(spotNumber)) {
					adapter.setPicture(picture);
				}
		    	switcher.showNext();
		    	break;
			case DELETE_SPOT_SUCCESFUL:
//				db.removeSpot(spotNumber);
				finish();
				break;
			case DELETE_SPOT_UNSUCCESFUL:
				toast = Toast.makeText(this, "Network error", Toast.LENGTH_SHORT);
				break;
		}

	}

	
	private void setRatingColour(float rating) {
		
		if(rating >= 4.0)
			ratingDisplay.setTextColor(Color.BLUE);
		else if(rating >= 3.0)
			ratingDisplay.setTextColor(Color.GREEN);
		else if(rating >= 2.0)
			ratingDisplay.setTextColor(Color.YELLOW);
		else if(rating >= 1.0)
			ratingDisplay.setTextColor(0x004455);
		else 
			ratingDisplay.setTextColor(Color.RED);
	}

	private long constructPictureID() {
		long bigNum = 500000000+500000000;		
		long id = (System.currentTimeMillis()%bigNum)+spotNumber+uid%10000;
		return id;
	}
	
	private void copyFile(File src, File dst) throws IOException {
		FileChannel inChannel = new FileInputStream(src).getChannel();
		FileChannel outChannel = new FileOutputStream(dst).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if(inChannel != null)
				inChannel.close();
			if(outChannel != null)
				outChannel.close();
		}
		dst.deleteOnExit();
	}
	
	private int getScreenOrientation() {
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			Log.d(TAG, "In Portrait");
			return 90;
		} else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Log.d(TAG, "In Landscpae");
			return 0;
		}
		return 90;
	}

	
}
 