package com.davidjennes.ElectroJam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.davidjennes.ElectroJam.Server.ServerActivity;

public class ModeChooserActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // strict mode
        if (getResources().getBoolean(R.bool.developer_mode))
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    }
    
    /**
     * Show settings menu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        
        return true;
    }
    
    /**
     * Show a chooser to pick an instrument
     */
    public void chooseInstrument(View view) {
    	Intent intent = new Intent(getPackageName() + ".INSTRUMENT");
    	startActivity(Intent.createChooser(intent, getString(R.string.mode_instrument)));
    }

    /**
     * Show the server control activity
     */
    public void serverActivity(View view) {
    	startActivity(new Intent(this, ServerActivity.class));
    }
    
    /**
     * A menu option was selected
     */
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case R.id.settings:
        	//startActivity(new Intent(this, About.class));
        	return true;
        default:
        	return super.onOptionsItemSelected(item);
    	}
    }
}
