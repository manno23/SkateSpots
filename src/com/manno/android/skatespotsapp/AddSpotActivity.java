package com.manno.android.skatespotsapp;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.manno.android.skatespotsapp.Service.BackgroundDataSync;

public class AddSpotActivity extends ActionBarActivity {

	public static final int WITH_FRIENDS_ONLY = 1;
	public static final int WITH_PUBLIC = 0;
	
	private EditText spotNameInput;
	private EditText description;
	private Button submit_but;
    private CheckBox checkBoxBanks;
    private CheckBox checkBoxBowls;
    private CheckBox checkBoxDrops;
    private CheckBox checkBoxEuros;
    private CheckBox checkBoxGaps;
    private CheckBox checkBoxLedges;
    private CheckBox checkBoxManualPads;
    private CheckBox checkBoxRails;
    private CheckBox checkBoxRamps;
    private CheckBox checkBoxStairs;

	private int share;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Set Up UI
        setContentView(R.layout.add_spot);
		spotNameInput = (EditText)findViewById(R.id.name_fill);
		description = (EditText)findViewById(R.id.description);
		submit_but = (Button)findViewById(R.id.submit_but);
        checkBoxBanks = (CheckBox)findViewById(R.id.check_banks);
        checkBoxBowls = (CheckBox)findViewById(R.id.check_bowls); 
        checkBoxDrops = (CheckBox)findViewById(R.id.check_drops);
        checkBoxEuros = (CheckBox)findViewById(R.id.check_euros);
        checkBoxGaps =  (CheckBox)findViewById(R.id.check_gaps);
        checkBoxLedges = (CheckBox)findViewById(R.id.check_ledges);
        checkBoxManualPads = (CheckBox)findViewById(R.id.check_manual_pads);
        checkBoxRails = (CheckBox)findViewById(R.id.check_rails);
        checkBoxRamps = (CheckBox)findViewById(R.id.check_ramps);
        checkBoxStairs =  (CheckBox)findViewById(R.id.check_stairs);

		//Collect and Display bundled and global info - longitude, latitude, uid and name	
		SkateSpotSession session = (SkateSpotSession)getApplicationContext();

		//Set Up Button Listeners //		share_but.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				buttonView.setText("Share with everyone!");
//				if(isChecked) {
//					share_display.setText(getText(R.string.share_with_public));
//					share = WITH_PUBLIC;
//				} else {
//					share_display.setText(getText(R.string.share_with_friends));
//					share = WITH_FRIENDS_ONLY;
//				}
//			}
//		});
		submit_but.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent newSpotIntent = prepareNewSpotSendIntent();
                if(newSpotIntent != null) {
                    Log.d("AddSpot", newSpotIntent.getExtras().toString());
//                    startService(newSpotIntent);
                    AddSpotActivity.this.finish();
                }
			}
		});

	}
    
    private Intent prepareNewSpotSendIntent() {

        // Set up Intent
        Intent uploadSpot = new Intent(AddSpotActivity.this, BackgroundDataSync.class);
        uploadSpot.putExtra("action", BackgroundDataSync.UPLOAD_SPOT);
//        uploadSpot.putExtra("callback", extras.getParcelable("callback"));

        // Check name length
        if(spotNameInput.getText().length() < 6) {
            Toast toast = Toast.makeText(AddSpotActivity.this, "Name must be at least 5 characters", Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }

        // Retrieve current location co-ordinates
        Intent currentLocationIntent = getIntent();
        double latitude = currentLocationIntent.getDoubleExtra("latitude", -200.0);
        double longitude = currentLocationIntent.getDoubleExtra("longitude", -200.0);
        if( latitude == -200.0 || longitude == -200.0) {
            Toast toast = Toast.makeText(AddSpotActivity.this, "Location is not accurate", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        } else {
            uploadSpot.putExtra("longitude", longitude);
            uploadSpot.putExtra("latitude", latitude);
        }


        // Insert values
        uploadSpot.putExtra("name", spotNameInput.getText().toString());
        uploadSpot.putExtra("description", description.getText().toString());
        uploadSpot.putExtra("banks", checkBoxBanks.isChecked());
        uploadSpot.putExtra("bowls", checkBoxBowls.isChecked());
        uploadSpot.putExtra("drops", checkBoxDrops.isChecked());
        uploadSpot.putExtra("euros", checkBoxEuros.isChecked());
        uploadSpot.putExtra("gaps", checkBoxGaps.isChecked());
        uploadSpot.putExtra("ledges", checkBoxLedges.isChecked());
        uploadSpot.putExtra("manualpads", checkBoxManualPads.isChecked());
        uploadSpot.putExtra("rails", checkBoxRails.isChecked());
        uploadSpot.putExtra("ramps", checkBoxRamps.isChecked());
        uploadSpot.putExtra("stairs", checkBoxStairs.isChecked());
        return uploadSpot;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_spot, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}

