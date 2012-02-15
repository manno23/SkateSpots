/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.manno.android.skatespotsapp.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.manno.android.skatespotsapp.R;
import com.manno.android.skatespotsapp.Utils.Spot;
import com.manno.android.skatespotsapp.ViewSpot.ViewSpotActivity;

import java.util.ArrayList;
import java.util.Iterator;

public class SkateSpotsOverlay extends BalloonItemizedOverlay<OverlayItem> {

	
	private ArrayList<Spot> spotOverlayList;

	private Context context;
	private int mSize;
	
	private Drawable fiveStar;
	private Drawable fourStar;
	private Drawable threeStar;
	private Drawable twoStar;
	private Drawable oneStar;
	private Spot spotWeAreAt;
	private Spot currentlySelectedSpot;
	
	private static final String TAG = "SkateSpotsOverlay";
	
	
	public SkateSpotsOverlay(MapView mapView) {
		
		super(null, mapView);
		context = mapView.getContext();
		spotOverlayList = new ArrayList<Spot>();
				
		fiveStar = context.getResources().getDrawable(R.drawable.rating_five);
		fourStar = context.getResources().getDrawable(R.drawable.rating_four);
		threeStar = context.getResources().getDrawable(R.drawable.rating_three);
		twoStar = context.getResources().getDrawable(R.drawable.rating_two);
		oneStar = context.getResources().getDrawable(R.drawable.rating_one);
		
		update();
	}

	
	@Override
	protected boolean onBalloonTap(int index) {	
		for(Spot spot : spotOverlayList) {
			if(spot.getSid() == currentlySelectedSpot.getSid())
				currentlySelectedSpot = spot;
		}
		Intent viewSpot = new Intent(context, ViewSpotActivity.class);
		Log.d("Test", "onTopofSpot"+currentlySelectedSpot.toString());
		if(currentlySelectedSpot.equals(spotWeAreAt)) {
			viewSpot.putExtra("onSpot", true);
		}
		viewSpot.putExtra("Spot", currentlySelectedSpot);
		context.startActivity(viewSpot);
		return true;
	}
	
	@Override 
	protected void spotSelected(int i) {
		Log.d("TEST", "Currently selected spot is" +spotOverlayList.get(i).getName());
		currentlySelectedSpot = spotOverlayList.get(i);
	}

	
	@Override
	protected OverlayItem createItem(int i) {
		return spotOverlayList.get(i);
	}

	@Override
	public int size() {
		return mSize;
	}

	
	//Interface
	public void addOverlay(Spot spot) {
		Drawable icon = null;
		float rating = spot.getRating();

		if(rating >= 4.0)
			icon = fiveStar;
		else if(rating >= 3.0)
			icon = fourStar;
		else if(rating >= 2.0)
			icon = threeStar;
		else if(rating >= 1.0)
			icon = twoStar;
		else {
			icon = oneStar;
			Log.d(TAG, "One star");
		}

		spot.setMarker(boundCenter(icon));
		spotOverlayList.add(spot);

	}
	
	public ArrayList<Spot> getSpotList() {
		return spotOverlayList;
	}

	public void clearSpotList() {
		Log.d("TEST", "Clear spotList");
		spotOverlayList.clear();
		update();
	}

	public void update() {	
		mSize = spotOverlayList.size();
		populate();
		setLastFocusedIndex(-1);
	}
	
	public boolean onTopOfSpot(Location currentLocation) {

		double currentLongitude = currentLocation.getLongitude();
		double currentLatitude = currentLocation.getLatitude();
		
		Iterator<Spot> iterator = spotOverlayList.iterator();
		while(iterator.hasNext()) {
			Spot spot = iterator.next();
			double latitudediff = currentLatitude - spot.getLatitude();
			double longitudediff = currentLongitude - spot.getLongitude();
			if( ((-0.0002 < longitudediff)&&(longitudediff < 0.0002))&&
				((-0.0002 < latitudediff)&&(latitudediff < 0.0002)) ) {
				spotWeAreAt = spot;
				return true;
			}
		}
		return false;
	}


}


















