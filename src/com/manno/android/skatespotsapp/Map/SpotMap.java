package com.manno.android.skatespotsapp.Map;

import android.os.Bundle;
import com.google.android.maps.MapActivity;
import com.manno.android.skatespotsapp.Service.ServiceHelper;

public class SpotMap extends MapActivity implements ServiceHelper.Receiver {
/*

	private ExtendedMapView mapView;
	private ProgressBar progressWheel;
	private MapController mc; 
	private ServiceHelper helper;
	private static final String TAG = "SpotMap";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	
    	mapView = (ExtendedMapView)findViewById(R.id.map);
    	progressWheel = (ProgressBar)findViewById(R.id.progress_wheel);
    	
		helper = new ServiceHelper(new Handler());	
		helper.setReceiver(this);
    	
    	mc = mapView.getController();
    	if(savedInstanceState==null) {
    		mc.setZoom(12);	
    	} else {
    		mc.setZoom(savedInstanceState.getInt("Map_Zoom_Level")); 	
    	}

    	mapView.updateSpotOverlays(0);
    	

    	//Checks to see how the activity is arrived at
    	//If this activity is started from the login screen then
    	// - request location updates
    	// - start background spot refresh
    	if (getIntent().getBooleanExtra("LOGIN",false)) {


            //Refresh spot list
            Intent intent = new Intent(this, BackgroundDataSync.class);
			intent.putExtra("action", BackgroundDataSync.SPOT_REFRESH);
			intent.putExtra("callback", helper);
			startService(intent);
			getIntent().putExtra("LOGIN", false);
		}
    }

	@Override
	protected void onResume() {
		super.onResume();
		mapView.updateSpotOverlays(600);
	}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
    	outState.putInt("Map_Zoom_Level", mapView.getZoomLevel());
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    { 
        switch (keyCode) 
        {
            case KeyEvent.KEYCODE_DPAD_UP:
                mc.zoomIn();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mc.zoomOut();
                break;
        }
        return super.onKeyDown(keyCode, event);
    } 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.spotmap, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) 
        {
			case R.id.create:
				
				SkateSpotSession sess = (SkateSpotSession)getApplicationContext();
				Toast toast;
				
				//must be logged in to add spots
				if(sess.getUid() == 0) {
					toast = Toast.makeText(this, "Must be logged in to add spots", Toast.LENGTH_SHORT);
					toast.show();
				} else {									//check if co-ordiantes are available, and accurate
					//if facebook id is logged in then change addspot to give option of only sharing with friends
					if(mapView.onTopOfSpot()) {
						toast = Toast.makeText(this, "Waiting on accuracy to improve...", Toast.LENGTH_SHORT);
						toast.show();
					} else {
						//Proceed to addspot activity or viewspot if sitting on an already createdspot 
						Intent i = new Intent(SpotMap.this, AddSpotActivity.class);
						if(mapView.onTopOfSpot()) {
							Toast noLocationMsg = Toast.makeText(this, "Already on a spot dude", Toast.LENGTH_SHORT);
							noLocationMsg.show();
						} else {
							i.putExtra("callback", helper);
							startActivity(i);
						}
					}
				}
				break;
			*/
/*
            case R.id.satellite:
                if(!mapView.isSatellite())
                	mapView.setSatellite(true);
                else
                	mapView.setSatellite(false);
                break;    
            *//*

        }
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		Toast toast;
		
		switch(resultCode) {
		case BackgroundDataSync.STATUS_RUNNING:
			progressWheel.setVisibility(View.VISIBLE);
			Log.d("TEST", "Running...");
			break;
		case BackgroundDataSync.STATUS_ERROR:
			mapView.setDatabaseStatus(BackgroundDataSync.STATUS_FINISHED);
			progressWheel.setVisibility(View.GONE);	
			Log.d("TEST", "ERROR!!!!!!!");
			break;
		case BackgroundDataSync.STATUS_FINISHED:
			mapView.setDatabaseStatus(BackgroundDataSync.STATUS_FINISHED);
			progressWheel.setVisibility(View.GONE);
			Log.d("TEST", "Finished");
			break;
		case BackgroundDataSync.STATUS_DBINUSE:
			mapView.setDatabaseStatus(BackgroundDataSync.STATUS_DBINUSE);
			Log.d("TEST", "Database busy");
			break;
		case BackgroundDataSync.SPOT_UPLOAD_ERROR:
			toast = Toast.makeText(this, "Upload Spot Failed", Toast.LENGTH_SHORT);
			toast.show();
			progressWheel.setVisibility(View.GONE);
			break;
		case BackgroundDataSync.SPOT_UPLOAD_FINISHED:
			toast = Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_SHORT);
			toast.show();
			mapView.updateSpotOverlays(0);
			progressWheel.setVisibility(View.GONE);
			break;
		}
	}
*/

    @Override
    protected boolean isRouteDisplayed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

