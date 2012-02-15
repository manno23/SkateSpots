package com.manno.android.skatespotsapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import com.manno.android.skatespotsapp.R;
import com.manno.android.skatespotsapp.Utils.FileCacheManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

public class ImageLoader {

	private Activity activityWithGallery;
	private FileCacheManager fileCacheManager;
	private ImageQueue imageQueue = new ImageQueue();
    private ImageGetter imageGetterThread = new ImageGetter();
    public interface Image {
        /**
         * @return the url of the image
         */
        public String getImageURL();
        /**
         * @return a unique identifier for identifying the image in a cache
         */
        public long getImageID();
    }

	final private static int PLACEHOLDER_IMAGE = R.drawable.placeholder;
	private static final String TAG = "ImageLoader";
	
	
	public ImageLoader(Context context) {
		activityWithGallery = (Activity)context;
		imageGetterThread.setPriority(Thread.NORM_PRIORITY-1);
		fileCacheManager = new FileCacheManager(context);
	}
	
	public void loadImage(Image image, ImageView imageView) {
		
		//image is either cached in file or will need to be downloaded
    	Log.d(TAG, "Queued: " + image);
    	imageView.setImageResource(PLACEHOLDER_IMAGE);
        queueImage(image, imageView);
            
	}
	
	private void queueImage(Image image, ImageView imageView) {
		imageQueue.clean(imageView);
		ImageRef imageConnector = new ImageRef(image, imageView);
		
		synchronized(imageQueue.imageRefQueue) {
			imageQueue.imageRefQueue.add(imageConnector);
			imageQueue.imageRefQueue.notifyAll();
		}
		
		//Begin thread if not started
		if(imageGetterThread.getState() == Thread.State.NEW)
			imageGetterThread.start();
	}
	

	private class ImageRef {
		private Image image;
		private ImageView imageView;
		
		public ImageRef(Image image, ImageView imageView) {
			this.image = image;
			this.imageView = imageView;
		}
	}
	
	private class ImageQueue {
		private LinkedList<ImageRef> imageRefQueue = new LinkedList<ImageRef>();
		
		public void clean(ImageView imageView) {
			for(int i = 0 ;i < imageRefQueue.size();) {
				if(imageRefQueue.get(i).imageView == imageView) {
					imageRefQueue.remove(i);
				} else ++i;
			 }
		}
	}
	

	class ImageGetter extends Thread {
        @Override
		public void run() {
            try {
                while(true)
                {
                    //thread waits until there are any images to load from the queue
                    if(imageQueue.imageRefQueue.size()==0)
                        synchronized(imageQueue.imageRefQueue){
                        	imageQueue.imageRefQueue.wait();
                        }
                    //Get queued url<->imageView pair from stack
                    //for each pair download image from url and store as a bitmap
                    //in filecache
                    if(imageQueue.imageRefQueue.size()!=0)
                    {
                        ImageRef imageConnector;
                        synchronized(imageQueue.imageRefQueue){
                        	imageConnector = imageQueue.imageRefQueue.remove();
                        	Log.d(TAG, "Popped:" + imageConnector.image.toString());
                        }
                        //Download image from url
                        Bitmap bmp = getBitmap(imageConnector.image);
                        //And place in the lrucache
                        BitmapDisplayer bd = new BitmapDisplayer(bmp, imageConnector.imageView);
                        activityWithGallery.runOnUiThread(bd);
                    }
                    if(Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {
                //allow thread to exit
            }
        }
    }

    public void stopThread() {
        imageGetterThread.interrupt();
    }

	private Bitmap getBitmap(Image image)
    {
		//url stored in file cache
        Bitmap bitmap = fileCacheManager.getcachedImage(image.getImageID());
        if(bitmap!=null) {
        	Log.d(TAG, "Retrieved from file cache:" + image.getImageID());
            return bitmap;
        }

        //from web, store in filecache in the process
        try {
        	URL imageUrl = new URL(image.getImageURL());
        	long startTime = System.currentTimeMillis();
        	HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
        	conn.setConnectTimeout(30000);
        	conn.setReadTimeout(30000);
        	InputStream is = conn.getInputStream();
        	Log.d("TEST", "Image download in: " + (System.currentTimeMillis() - startTime));
	        bitmap = fileCacheManager.cacheDownloadedImage(is, image.getImageID());
	        return bitmap;
	    } catch (Exception ex){
	        ex.printStackTrace();
	        return null;
	    }

    }
	
    //Used to display bitmap in the UI thread
    private class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        ImageView imageView;

        public BitmapDisplayer(Bitmap b, ImageView i) {
            bitmap = b;
            imageView = i;
        }
        
        public void run() {
            if(bitmap != null)
            	
                imageView.setImageBitmap(bitmap);
            else
                imageView.setImageResource(PLACEHOLDER_IMAGE);
        }
        
    }
 
}
