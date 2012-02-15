package com.manno.android.skatespotsapp.Map;

import android.graphics.Canvas;
import android.location.Location;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.manno.android.skatespotsapp.R;

public class MyLocationOverlaySkate extends ItemizedOverlay<OverlayItem> {

	
	private Location currentLocation;

	protected static String TAG = "MyLocationOverlay";
	
	public MyLocationOverlaySkate(MapView mapView) {
		super(boundCenter(mapView.getContext().getResources().getDrawable(R.drawable.location)));
	}
		
	public Location getCurrentLocation() {
		if(currentLocation!=null)
			return currentLocation;
		else
			return null;
	}

	public void setLocation(Location location) {
		currentLocation = location;
		populate();
		setLastFocusedIndex(-1);
	}

	@Override
	public boolean onTap(GeoPoint p, MapView map) {
		return false;
	}

	@Override
	protected OverlayItem createItem(int i) {	
		
		if(currentLocation==null) {
			Log.d(TAG, "CurrentLocation is null");
			return new OverlayItem(new GeoPoint(0,0),null,null);
		} else {
			GeoPoint point = new GeoPoint((int)(currentLocation.getLatitude()*1E6),
					   (int)(currentLocation.getLongitude()*1E6));
			Log.d(TAG, "CurrentLocation exists");
			return new OverlayItem(point,null,null);
		}
	}

	@Override
	public int size() {
		return 1;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
	}

}
