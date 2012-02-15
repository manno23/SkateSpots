package com.manno.android.skatespotsapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SkateSpotSession extends Application {

	private HttpClient httpClient;
    public final static String LOCATION_BROADCAST = "com.manno.android.skatespotsapp.locationbroadcast";
	public final static int LOCATION_AQUIRED_ROUGH = 1;
	public final static int LOCATION_AQUIRED_ACCURATE = 2;
	private final String TAG = "Application";
    private Intent broadcast;
    private Location skateSessionLocation = null;       // Only set if it is accurate enough to use 	as a new spot location 
    private boolean initialZoom; 
    private LocationManager locationManager;
	private LocationListener locationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			Log.d(TAG, "Location listener accuracy is: " + location.getAccuracy() + " using the " + location.getProvider());
			if(handleLocationBroadcast(location)) {
		         locationManager.removeUpdates(locationListener);
			}
	    }
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	    public void onProviderEnabled(String provider) {
            requestMyLocation();
        }
	    public void onProviderDisabled(String provider) {
            requestMyLocation();
        }
	};
    
    @Override
	public void onCreate() {
		super.onCreate();
		setUpHttpClient();
		BugSenseHandler.setup(this, "74fe635d");
        broadcast = new Intent(SkateSpotSession.LOCATION_BROADCAST);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        initialZoom = false;
	}

    public HttpClient getHttpClient() {
        return httpClient;
    }

	//Creates an application wide singleton HttpClient to service all requests
	//within the application. A ThreadSafeConnectionManager handles multiple calls
	private void setUpHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpConnectionParams.setConnectionTimeout(params, 4000);
		
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http",
				PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https",
				PlainSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
		httpClient = new DefaultHttpClient(conMgr, params);
		
	}

	@SuppressWarnings("unused")
	private void shutDownHttpClient() {
		if(httpClient!=null && httpClient.getConnectionManager()!=null)	{
			httpClient.getConnectionManager().shutdown();
		}
	}

    /**
     *
     * @return Location lastKnownLocation if it is accurate and recent or
     *          returns null and requests further updates which will be
     *          broadcast.
     */
    public Location requestMyLocation() {

        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MAX_VALUE;
        String bestProvider = null;

        Location lastKnownLocation = null;
        /*
           *    Iterate through all the providers on the system then
           *    choose the best lastKnownLocation based on most accurate
           */
        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider: matchingProviders) {
            lastKnownLocation = locationManager.getLastKnownLocation(provider);
            if (lastKnownLocation != null) {
                float accuracy = lastKnownLocation.getAccuracy();
                
                long age = System.currentTimeMillis() - lastKnownLocation.getTime();
                Log.d(TAG, "Provider: " + provider + " accuracy:" + accuracy + " age:" + age/1000 + " seconds old");
                if((age < bestTime) && (accuracy < bestAccuracy)) {
                    bestTime = age;
                    bestAccuracy = accuracy;
                    bestProvider = provider;
                }
            }
        }
        if(bestProvider!=null )
            lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);

        /*
           *    Determine what category of accuracy the lastKnownLocation falls under
           *    and {@link Broadcast} this to any Receivers
           */
        if (handleLocationBroadcast(lastKnownLocation))
            return lastKnownLocation;
        else {
            requestUpdates(5000);
            return null;
        }
    }

    private void requestUpdates(int timeBetweenUpdates) {

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "searching for location with GPS");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeBetweenUpdates, 0, locationListener, getMainLooper());
        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeBetweenUpdates, 0, locationListener, getMainLooper());

        // If the time between updates is short ( < 1 minute between )
        // cease updates after 1 minute
        if(timeBetweenUpdates < 60000) {
            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            locationManager.removeUpdates(locationListener);
                        }
                    }
                    ,60000);
        }
    }

    /*
     *  Handles the broadcasting of the available location whether it is
     *  just received or is an old stored location.
     */
    private boolean handleLocationBroadcast(Location newLocation) {
        long age = System.currentTimeMillis() - newLocation.getTime();
        float accuracy = newLocation.getAccuracy();
        if((newLocation == null) && (age < 86400000) && (!initialZoom)) {
            Log.d(TAG, "Initial allocation " + initialZoom);
            initialZoom = true;
            broadcast.putExtra("action", LOCATION_AQUIRED_ROUGH);
            broadcast.putExtra("location", newLocation );
            sendBroadcast(broadcast);
        }
        // Accurate enough if less than 2min old and within 20m accuracy
        if((age < 120000) && (accuracy < 20.0)) {  
            Log.d(TAG, "Got an accurate location, broadcasting that shit");
            skateSessionLocation = newLocation;
            broadcast.putExtra("action", LOCATION_AQUIRED_ACCURATE);
            broadcast.putExtra("location", newLocation);
            sendBroadcast(broadcast);
            return true;
        } else return false; 
    }
    
    public Location getCurrentSessionLocation() {
        if(skateSessionLocation != null) {
            long sessionLocationAge = System.currentTimeMillis() - skateSessionLocation.getTime();
            // If skateSessionLocation is less than 5 minutes old we can use it as our location
            if(skateSessionLocation.getTime() < 300000) {
                return skateSessionLocation;
            } else {
                return requestMyLocation();
            }
        } else  return null;
    }
    /*

    Global authorization state for managing login status

    private final String APP_ID = "185603934817991";
	private String FACEBOOK_NAME = "fb_name";
	private String FACEBOOK_ID = "fb_uid";
	private static final String KEY = "facebook-session";
	public final Facebook fb = new Facebook(APP_ID);
	private String fbname;
	private long uid;

 	public String getFbname() {
		return fbname;
	}
	public void setFbname(String fbname) {
		this.fbname = fbname;
	}

	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
	}

//	public Facebook getFacebook() {
//		return fb;
//	}
/*
	public void save() {
		Log.d("TEST", "Save called");
        Editor editor =
            this.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
        editor.putString(FACEBOOK_NAME, fbname);
        editor.putLong(FACEBOOK_ID, uid);
        editor.commit();

        //Update server with name
        if(uid!=0) {
			Intent updateFbName = new Intent(this, BackgroundDataSync.class);
			updateFbName.putExtra("action", BackgroundDataSync.USERS_NAME_SERVER_REFRESH);
			updateFbName.putExtra("uid", uid);
			updateFbName.putExtra("name", fbname);
			startService(updateFbName);
        }
	}

	public void restore() {
		Log.d("TEST", "Restore called for global state");
		SharedPreferences savedSession =
            this.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        setFbname(savedSession.getString(FACEBOOK_NAME, ""));
        setUid(savedSession.getLong(FACEBOOK_ID, 0));
	}
*/
}
