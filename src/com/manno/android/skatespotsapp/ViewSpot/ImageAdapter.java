package com.manno.android.skatespotsapp.ViewSpot;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.manno.android.skatespotsapp.ImageLoader;
import com.manno.android.skatespotsapp.Utils.SpotPicture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    
    private Activity activity;
    private ImageView imageView;
    private final List<SpotPicture> pictures = Collections.synchronizedList(new ArrayList<SpotPicture>());
    public ImageLoader imageloader;
    
	public ImageAdapter(Activity activity) {
		this.activity = activity;
		imageloader = new ImageLoader(activity);
	}

	@Override public Object getItem(int position) {
        return pictures.get(position);
    }

	@Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
    	imageView = (ImageView)convertView;
        if(convertView == null)
        	imageView = new ImageView(activity);
        	
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        //Let imageLoader determine best way to gather image and place in the ImageView
        imageloader.loadImage(pictures.get(position), imageView);
  
        return imageView;
    }

	@Override public int getCount() {
		if(pictures!=null)
			return pictures.size();
		else return 0;
	}

	public void setPicture(SpotPicture picture) {
		pictures.add(picture);
		notifyDataSetChanged();
	}

	public void removePicture(long pictureID) {
        Iterator<SpotPicture> iterator = pictures.iterator();
        while(iterator.hasNext()) {
            if(iterator.next().getImageID() == pictureID)
                iterator.remove();
        }
		notifyDataSetChanged();
	}

}