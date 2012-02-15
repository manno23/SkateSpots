package com.manno.android.skatespotsapp.Utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * @author Nihilator
 *
 *
 */
public class Spot extends OverlayItem implements Parcelable{

	private int sid;
	private double longitude;
	private double latitude;
	private String name;
	private String address;
	private float rating;
	private long uid;
	private int sharing;
	private GeoPoint geo;
	private int tally;
	private int voteCount;
	private String description;

	
	public Spot(int sid, double latitude, double longitude, 
			String name, String address,  float rating, 
			long uid, int sharing, int tally, int voteCount, String description) {
		/* TODO
		 * Upgrade GeoLocation to create snippet address from geopoint
		 * Or add to online database from either side, may be a server side geocoding
		 * 
		 * http://code.google.com/apis/maps/documentation/places/
		 */	
		super(new GeoPoint((int)(latitude*1E6),(int)(longitude*1E6)), name, address);
		this.sid = sid;
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.address = address;
		this.rating = rating;
		this.uid = uid;
		this.sharing = sharing;
		this.tally = tally;
		this.voteCount = voteCount;
		this.description = description;
	}

	public Spot(Spot spot) {
		this(spot.getSid(), spot.getLatitude(), spot.getLongitude(), 
				spot.getName(), spot.getAddress(),  spot.getRating(), 
				spot.getUid(), spot.getSharing(), spot.getTally(),
				spot.getVoteCount(), spot.getDescription());
	}
	
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(sid);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeString(name);
		dest.writeString(address);
		dest.writeFloat(rating);
		dest.writeLong(uid);
		dest.writeInt(sharing);
		dest.writeInt(tally);
		dest.writeInt(voteCount);
		dest.writeString(description);
	}
	
	public static final Parcelable.Creator<Spot> CREATOR 
		= new Parcelable.Creator<Spot>() {
		public Spot createFromParcel(Parcel in) {
			Log.d("TEST", "Spot is created from parcel");
			return new Spot(in.readInt(), in.readDouble(), in.readDouble(), 
					in.readString(), in.readString(),  in.readFloat(), 
					in.readLong(), in.readInt(), in.readInt(),
					in.readInt(), in.readString());
		}

		public Spot[] newArray(int size) {
			return new Spot[size];
		}
	};

	@Override
	public String toString() {
		return "Spot "+name+": Long:"+longitude+" Lat:"+latitude+" Rating:"+rating+" uid:"+uid+
				" sharing:"+sharing+" tally:"+tally+" vote count:"+voteCount+" Description: "+description;
	}
	
	//Getters & Setters	
	public int getSid() {
		return sid;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public float getRating() {
		return rating;
	}
	public void setRating(float rating) {
		this.rating = rating;
	}
	
	public long getUid() {
		return uid;
	}
	
	public GeoPoint getGeo() {
		return geo;
	}
	
	public int getTally() {
		return tally;
	}
	public void setTally(int tally) {
		this.tally = tally;
	}
	
 	public int getVoteCount() {
		return voteCount;
	}
	
	public void incrementVoteCount() {
		voteCount++;
	}
 	
	public int getSharing() {
		return sharing;
	}
	
	public String getDescription() {
		return description;
	}
}
