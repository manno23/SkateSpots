package com.manno.android.skatespotsapp.Utils;

import android.os.Parcel;
import android.os.Parcelable;
import com.manno.android.skatespotsapp.ImageLoader;

public class SpotPicture implements Parcelable, ImageLoader.Image {

	private long creatorID;
	private int spotID;
	private String caption;
    private String date;
    private long pictureID;

    public SpotPicture(long pictureID, long creatorID, int spotID,
			String date, String caption) {
        this.pictureID = pictureID;
		this.creatorID = creatorID;
		this.spotID = spotID;
        this.date = date;
		this.caption = caption;
	}

	public long getCreatorID() {
		return creatorID;
	}
	public int getSpotID() {
		return spotID;
	}
    public String getDate() {
        return date;
    }
	public String getCaption() {
		return caption;
	}

    public static final Parcelable.Creator<SpotPicture> CREATOR
		= new Parcelable.Creator<SpotPicture>() {
		
		public SpotPicture createFromParcel(Parcel in) {
			return new SpotPicture(in.readLong(), in.readLong(), in.readInt(),
					in.readString(), in.readString());
		}

		public SpotPicture[] newArray(int size) {
			return new SpotPicture[size];
		}
	};

    @Override public int describeContents() {
		return 0;
	}
	
	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(pictureID);
		dest.writeLong(creatorID);
		dest.writeInt(spotID);
		dest.writeString(date);
		dest.writeString(caption);
	}

	@Override public String toString() {
		super.toString();
		return "Picture "+pictureID+" of spot ID "+spotID;
	}

    @Override public String getImageURL() {
        return "http://www.therealskatespot.com/image.php?image="+spotID+
                    "/"+pictureID+".jpg&height=350";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public long getImageID() {
        return pictureID;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
