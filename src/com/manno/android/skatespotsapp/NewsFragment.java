package com.manno.android.skatespotsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import com.manno.android.skatespotsapp.Service.BackgroundDataSync;
import com.manno.android.skatespotsapp.Service.ServiceHelper;
import com.manno.android.skatespotsapp.Storage.MyDB;

import java.util.ArrayList;

public class NewsFragment extends FragmentActivity {

    private static final String TAG = "NewsFragment";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TEST", "FragmentActivity called");
        
        FragmentManager fm = getSupportFragmentManager();

        Context context = this;
        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            News list = new News();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    public static class NewsItem implements ImageLoader.Image {
        
        private long newsID;
        private String creationDate;
        private String imageURL;
        private String linkURL;
        private long clientID;

        public NewsItem(long newsID, String creationDate, String imageURL, String linkURL, long clientID) {
            this.newsID = newsID;
            this.creationDate = creationDate;
            this.imageURL = imageURL;
            this.linkURL = linkURL;
            this.clientID = clientID;
        }

        public String getCreationDate() {
            return creationDate;
        }

        public String getLinkURL() {
            return linkURL;
        }

        public long getClientID() {
            return clientID;
        }

        @Override public String getImageURL() {
            return imageURL;
        }

        @Override public long getImageID() {
            return newsID;
        }
    }
    
    public static class NewsAdapter extends BaseAdapter {

        private Context context;
        private ImageLoader imageLoader;
        private ArrayList<NewsItem> newsItems;
        
        public NewsAdapter(Context context) {
            this.context = context;
            this.imageLoader = new ImageLoader(context);
            newsItems = new ArrayList<NewsItem>();
        }
        
        public void getNewsItems() {
            MyDB db = new MyDB(context);
            newsItems = db.getNewsItems();
        }

        @Override public int getCount() {
            return newsItems.size();  
        }

        @Override public Object getItem(int position) {
            return newsItems.get(position);
        }

        @Override public long getItemId(int position) {
            return newsItems.get(position).getImageID();  
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = (ImageView)convertView;
            if(convertView == null)
                imageView = new ImageView(context);

            imageView.setPadding(10,10,10,10);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //Let imageLoader determine best way to gather image and place in the ImageView
            imageLoader.loadImage(newsItems.get(position), imageView);

            return imageView;
        }
        
    }
    
    public static class News extends ListFragment implements ServiceHelper.Receiver {

        private NewsAdapter newsAdapter;
        private ServiceHelper helper;
        
        @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setEmptyText("Loading...");

            helper = new ServiceHelper(new Handler());
            helper.setReceiver(this);

            //Send off to get new news items
            Intent syncNewsItemsIntent = new Intent(getActivity(), BackgroundDataSync.class);
            syncNewsItemsIntent.putExtra("action", BackgroundDataSync.REFRESH_NEWS_ITEMS);
            syncNewsItemsIntent.putExtra("callback", helper);
            getActivity().startService(syncNewsItemsIntent);

            //Create the adapter
            newsAdapter = new NewsAdapter(getActivity());
            setListAdapter(newsAdapter);
        }

        @Override public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            NewsItem newsItem = (NewsItem)newsAdapter.getItem(position);
            if(newsItem.getLinkURL().startsWith("http://")) {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getLinkURL()));
                startActivity(browser);
            }
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            //update adapter
            newsAdapter.getNewsItems();
            if(newsAdapter.getCount() == 0)
                setEmptyText("No New News Now");
            newsAdapter.notifyDataSetChanged();
            Log.d(TAG, "on Receive Result "+resultData.toString());
        }
    }

}
