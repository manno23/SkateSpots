package com.manno.android.skatespotsapp.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.*;

public class FileCacheManager {

	File cacheDirectory;
	private static final String TAG = "FileCacheManager";
	
	
	public FileCacheManager(Context context) {
		cacheDirectory = context.getCacheDir();
		//TODO Possibility on devices - Handle cache directory not appearing
		cacheDirectory.mkdirs();
	}
	
	public Bitmap cacheDownloadedImage(InputStream is, long pictureID) {
		
		String filename = String.valueOf(pictureID);
        File f = new File(cacheDirectory, filename);

        final int buffer_size=1024;	
        try {
    		OutputStream os = new FileOutputStream(f);
            byte[] bytes=new byte[buffer_size];
            for(;;) {
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
            is.close();
            os.close();
        } catch(Exception e){
        	e.printStackTrace();
        }     
        
        purgeCache();
        
        return decodeFile(f);
	}
	
	public Bitmap getcachedImage(long pictureID) {
		String filename = String.valueOf(pictureID);
        File f = new File(cacheDirectory, filename);
        return decodeFile(f);
	}
	
	public Bitmap decodeFile(File f){
	    try {
	        //decode image size
	    	BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f), null, o);
	        
	        //Find the correct scale value. It should be the power of 2.
	        final int REQUIRED_SIZE=200;
	        int width_tmp=o.outWidth, height_tmp=o.outHeight;
	        int scale=1;
	        while(true){
	            if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
	                break;
	            width_tmp/=2;
	            height_tmp/=2;
	            scale*=2;
	        }
	        //decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        o2.inPurgeable = true;
	        InputStream fileInStream = new BufferedInputStream(new FileInputStream(f), 1024*8);
	        return BitmapFactory.decodeStream(fileInStream, null, o2);
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();
	    	return null;
	    }
	}
	 
	private boolean purgeCache() {
		long time = System.currentTimeMillis();
        File[] fileArray = cacheDirectory.listFiles();
        long mb = 0;
		for(int i = 0; i < fileArray.length; i++) {
        	mb = mb+fileArray[i].length();
        }
		Log.d(TAG, "Cache size is: " + mb);
        Log.d(TAG, "Took this long to figure that out: " + (System.currentTimeMillis() - time));
		if(mb>20971520) {   //768kB cache size
			Log.d(TAG, "Deleting from cache");
			for(int i = 0; i < 8; i++) {
	        	fileArray[i].delete();
	        }
		}
		return true;
	}

}
