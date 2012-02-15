package com.manno.android.skatespotsapp.Utils;

import android.graphics.Bitmap;

import java.lang.ref.SoftReference;
import java.util.HashMap;

public class LRUCache {

	private HashMap<String, SoftReference<Bitmap>> cache
				= new HashMap<String, SoftReference<Bitmap>>();
    
    /**
     * <b>public Bitmap get(long pictureID)</b>
     * <br><br>
     * Returns a SoftReference to the Bitmap for the given identifier
     * <br>
     * 
     * @param  pictureID - the name of a bitmap in cache
     * @return  a <b>SoftReference</b> to a Bitmap or null if it does not exist
     */
    public Bitmap get(long pictureID){
        if(!cache.containsKey(pictureID))
            return null;
        SoftReference<Bitmap> ref=cache.get(pictureID);
        return ref.get();
    }
    
    public void put(long pictureID, Bitmap bitmap){
        cache.put(String.valueOf(pictureID), new SoftReference<Bitmap>(bitmap));
    }

    public void clear() {
        cache.clear();
    }

}
