package com.manno.android.skatespotsapp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;

/**
 * User: jason
 * Date: 13/02/12
 * Time: 8:01 PM
 */
public class SpotsListFragment extends FragmentActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TEST", "FragmentActivity called");

        FragmentManager fm = getSupportFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            Spots list = new Spots();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    public static class Spots extends ListFragment {

        @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }
    }

}
