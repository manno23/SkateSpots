package com.manno.android.skatespotsapp.Map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.manno.android.skatespotsapp.SkateSpotSession;
import com.manno.android.skatespotsapp.Storage.MyDB;
import com.manno.android.skatespotsapp.Utils.Spot;

import java.util.LinkedList;
import java.util.List;

public class ExtendedMapView extends MapView {

	private Context context;
	private List<Overlay> mapOverlays;
	private MyLocationOverlaySkate myLocationOverlay;
	private SkateSpotsOverlay spotLocationOverlays;
	private Handler handler;

	private static final String TAG = "ExtendedMapView";
	
	public ExtendedMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;	
		mapOverlays = this.getOverlays();
		handler = new Handler();	
		
		spotLocationOverlays = new SkateSpotsOverlay(this);
        //check service to find if a location has been found

		myLocationOverlay = new MyLocationOverlaySkate(this);
        mapOverlays.add(spotLocationOverlays);


		setBuiltInZoomControls(true);
    	setSatellite(false);

	}

    public void receiveLocation(Intent intent) {
        Log.d(TAG, "Received Broadcast");
        Location location = intent.getParcelableExtra("location");
        switch(intent.getIntExtra("action", 0)) {
            case SkateSpotSession.LOCATION_AQUIRED_ROUGH:
                animateTo(location);
                break;
            case SkateSpotSession.LOCATION_AQUIRED_ACCURATE:
                myLocationOverlay.setLocation(location);
                mapOverlays.add(myLocationOverlay);
                updateSpotOverlays(600);
                break;
        }
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) { 
			case MotionEvent.ACTION_DOWN:
				handler.removeCallbacks(r);
				break;
			case MotionEvent.ACTION_UP:
				handler.postDelayed(r , 500);
				break;
			}
		return super.onTouchEvent(event);
	}

	public SkateSpotsOverlay getSpotOverlays() {
		return spotLocationOverlays;
	}
		
	public void updateSpotOverlays(int delayTime) {
		handler.postDelayed(r, delayTime);
	}

	private void animateTo(Location location) {
		GeoPoint point = new GeoPoint((int)(location.getLatitude()*1E6),
									  (int)(location.getLongitude()*1E6));
		getController().animateTo(point);
		updateSpotOverlays(600);
	}
	
	public boolean onTopOfSpot() {
		return false;
	}
	
	private Runnable r = new Runnable() {				
		@Override
		public void run() {
            GeoPoint topLeftCoOrds = getProjection().fromPixels(0,0);
            GeoPoint bottomRightCoOrds = getProjection().fromPixels(getWidth(),getHeight());
            int zoom = getZoomLevel();
            new SpotRefreshTask(topLeftCoOrds, bottomRightCoOrds, zoom).execute();
        }
	};

	
	
	
	private class SpotRefreshTask extends AsyncTask<Void,Void,LinkedList<Spot>> {
		
		private GeoPoint topLeftCoOrds;
		private GeoPoint bottomRightCoOrds;
		private int zoomLvl;
		
		public SpotRefreshTask(GeoPoint topLeftCoOrds, GeoPoint bottomRightCoOrds, int zoomLvl) {
			this.topLeftCoOrds = topLeftCoOrds;
			this.bottomRightCoOrds = bottomRightCoOrds;
			this.zoomLvl = zoomLvl;
		}

		@Override
		protected LinkedList<Spot> doInBackground(Void... arg0) {
			
			long startTime = System.currentTimeMillis();

			int tl_Lat = (int)(topLeftCoOrds.getLatitudeE6()/1E6);
			int tl_Long = (int)(topLeftCoOrds.getLongitudeE6()/1E6);
			int br_Lat = (int)(bottomRightCoOrds.getLatitudeE6()/1E6);
			int br_Long = (int)(bottomRightCoOrds.getLongitudeE6()/1E6);
			
			Log.d(TAG, "Task:"+tl_Lat+" "+tl_Long+" "+br_Lat+" "+br_Long);
			
			//Get spotOverlays from local db
			MyDB db = new MyDB(context);
			db.open();  	
	    	LinkedList<Spot> spotList = db.retrieveSpots(topLeftCoOrds, bottomRightCoOrds, zoomLvl);
	    	db.close();
			Log.d(TAG, "time spent loading spot into memory from db and redrawing: " + (System.currentTimeMillis() - startTime)); 
			return spotList;
		}
		
		@Override
		protected void onPostExecute(LinkedList<Spot> spotList) {
			super.onPostExecute(spotList);
			spotLocationOverlays.clearSpotList(); 
	    	for(Spot spot : spotList) {
	    		Log.d(TAG, spot.toString());
	    		spotLocationOverlays.addOverlay(spot);
	    	}
			
	    	spotLocationOverlays.update();
	    	invalidate();
	
		}

	}


}
