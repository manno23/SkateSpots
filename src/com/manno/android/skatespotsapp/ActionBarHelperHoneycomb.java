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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * An extension of {@link ActionBarHelper} that provides Android 3.0-specific functionality for
 * Honeycomb tablets. It thus requires API level 11.
 */
public class ActionBarHelperHoneycomb extends ActionBarHelper {
    private Menu mOptionsMenu;
    private View mRefreshIndeterminateProgressView = null;
    private View mAddSpotView = null;

    protected ActionBarHelperHoneycomb(Activity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        mActivity.getActionBar().setDisplayUseLogoEnabled(true);
        mActivity.getActionBar().setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void setRefreshActionItemState(boolean refreshing) {
        Log.d("HoneyComb Helper", "setRefreshIcon state");
        // On Honeycomb, we can set the state of the refresh button by giving it a custom
        // action view.
        if (mOptionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                if (mRefreshIndeterminateProgressView == null) {
                    LayoutInflater inflater = (LayoutInflater)
                            getActionBarThemedContext().getSystemService(
                                    Context.LAYOUT_INFLATER_SERVICE);
                    mRefreshIndeterminateProgressView = inflater.inflate(
                            R.layout.actionbar_indeterminate_progress, null);
                }

                refreshItem.setActionView(mRefreshIndeterminateProgressView);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    @Override
    public void setAddSpotItemState(boolean ready) {
        Log.d("HoneyComb Helper", "setAddSpot state:"+ready);
        // On Honeycomb, we can set the state of the refresh button by giving it a custom
        // action view.
        if (mOptionsMenu == null) {
            return;
        }

        final MenuItem addSpotItem = mOptionsMenu.findItem(R.id.menu_add_spot_ready);
        if (addSpotItem != null) {
            if (!ready) {
                if (mAddSpotView == null) {
                    LayoutInflater inflater = (LayoutInflater)
                            getActionBarThemedContext().getSystemService(
                                    Context.LAYOUT_INFLATER_SERVICE);
                    mAddSpotView = inflater.inflate(
                            R.layout.actionbar_addspotitem, null);
                }

                addSpotItem.setActionView(mAddSpotView);
            } else {
                addSpotItem.setActionView(null);
            }
        }
    }

    /**
     * Returns a {@link Context} suitable for inflating layouts for the action bar. The
     * implementation for this method in {@link ActionBarHelperICS} asks the action bar for a
     * themed context.
     */
    protected Context getActionBarThemedContext() {
        return mActivity;
    }

}
