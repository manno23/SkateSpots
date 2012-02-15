/*
 * Copyright 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.manno.android.skatespotsapp;

import android.content.*;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.*;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.manno.android.skatespotsapp.Map.ExtendedMapView;
import com.manno.android.skatespotsapp.Service.BackgroundDataSync;
import com.manno.android.skatespotsapp.Service.ServiceHelper;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends ActionBarActivity implements ServiceHelper.Receiver {

    private String TAG = "MainActivity";

    private TabHost mTabHost;
    private TabManager mTabManager;
    private View mapViewContainer;
    private ServiceHelper helper;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            
            ExtendedMapView mapView = ((ExtendedMapView)mapViewContainer.findViewById(R.id.mapView));
            Log.d("Application", "Broadcast recieved as: "+intent.toString());
            switch (intent.getIntExtra("action", 0)) {
                case SkateSpotSession.LOCATION_AQUIRED_ROUGH:
                    mapView.receiveLocation(intent);
                    break;
                case SkateSpotSession.LOCATION_AQUIRED_ACCURATE:
                    Log.d("Application", "handling accurate location, should be marking on the map");
                    getActionBarHelper().setAddSpotItemState(true);
                    mapView.receiveLocation(intent);
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mapViewContainer = LayoutInflater.from(this).inflate(R.layout.skate_map, null);

        //Disable add spot and request updates
        getActionBarHelper().setAddSpotItemState(false);

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
        if(savedInstanceState == null) {
            mTabManager.addTab(mTabHost.newTabSpec("news").setIndicator(setUpTabView(mTabHost.getContext(),"News")), NewsFragment.News.class, null);
            mTabManager.addTab(mTabHost.newTabSpec("map").setIndicator(setUpTabView(mTabHost.getContext(),"Map")),
                               Map.class, null);
        }

        helper = new ServiceHelper(new Handler());
        helper.setReceiver(this);

        // Refresh the spot list
        Intent intent = new Intent(this, BackgroundDataSync.class);
        intent.putExtra("action", BackgroundDataSync.SPOT_REFRESH);
        intent.putExtra("callback", helper);
        startService(intent);
        getIntent().putExtra("LOGIN", false);

    }

    private View setUpTabView(Context context, String title) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab, null);
        TextView tv = (TextView)view.findViewById(R.id.tab_text);
        tv.setText(title);
        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        Log.d(TAG, "onResume");
        registerReceiver(broadcastReceiver, new IntentFilter(SkateSpotSession.LOCATION_BROADCAST));
        ((SkateSpotSession)getApplication()).requestMyLocation();

    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    boolean ready = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_add_spot_ready:
                Intent addSpotIntent = new Intent(MainActivity.this, AddSpotActivity.class);
                // Attach current location to the intent
                Location currentLocation = ((SkateSpotSession)getApplication()).getCurrentSessionLocation();
                if(currentLocation!=null) {
                    addSpotIntent.putExtra("latitude", currentLocation.getLatitude());
                    addSpotIntent.putExtra("longitude", currentLocation.getLongitude());
                }
                startActivity(addSpotIntent);
                break;
            
//            case R.id.menu_add_spot_not_ready:
//                Toast.makeText(this, "Finding Location...", Toast.LENGTH_SHORT).show();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public static class TabManager implements TabHost.OnTabChangeListener {
        private final FragmentActivity mActivity;
        private final TabHost mTabHost;
        private final int mContainerId;
        private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
        TabInfo mLastTab;

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
            mActivity = activity;
            mTabHost = tabHost;
            mContainerId = containerId;
            mTabHost.setOnTabChangedListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mActivity));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }

            mTabs.put(tag, info);
            mTabHost.addTab(tabSpec);
        }

        @Override
        public void onTabChanged(String tabId) {
            TabInfo newTab = mTabs.get(tabId);
            if (mLastTab != newTab) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                if (mLastTab != null) {
                    if (mLastTab.fragment != null) {
                        ft.detach(mLastTab.fragment);
                    }
                }
                if (newTab != null) {
                    if (newTab.fragment == null) {
                        newTab.fragment = Fragment.instantiate(mActivity,
                                newTab.clss.getName(), newTab.args);
                        ft.add(mContainerId, newTab.fragment, newTab.tag);
                    } else {
                        ft.attach(newTab.fragment);
                    }
                }

                mLastTab = newTab;
                ft.commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        }
    }

    public static class Map extends Fragment {

        private View mapViewContainer;
        private ExtendedMapView mapView;
        private int resumed;

        public static Map newInstance() {
            Map fragment = new Map();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
            MainActivity mainActivity = (MainActivity)getActivity();
            mapViewContainer = mainActivity.mapViewContainer;
            mapView = (ExtendedMapView)mapViewContainer.findViewById(R.id.mapView);
            Log.d("MapFragment", "onCreateView called");
            return mapViewContainer;
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d("MapFragment", "onResume called");
            MainActivity mainActivity = (MainActivity)getActivity();
            resumed++;
            if( resumed > 0 ) {
                ViewGroup mapFragmentViewGroup = (ViewGroup)getView();
                mapFragmentViewGroup.removeAllViews();
    
                ViewGroup parentViewGroup = (ViewGroup)getView();
                if(parentViewGroup != null)
                    parentViewGroup.removeAllViews();
    
                mapFragmentViewGroup.addView(((MainActivity)getActivity()).mapViewContainer);
            }
            mapView.updateSpotOverlays(1100);
        }

        @Override
        public void onPause() {
            super.onPause();
            ViewGroup parentGroup = (ViewGroup)mapViewContainer.getParent();
            if(null != parentGroup)
                parentGroup.removeView(mapViewContainer);
        }

    }

}
