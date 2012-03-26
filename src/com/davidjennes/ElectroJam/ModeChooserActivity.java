package com.davidjennes.ElectroJam;

import com.davidjennes.ElectroJam.Client.SelectServerActivity;
import com.davidjennes.ElectroJam.Server.ServerActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

public class ModeChooserActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // strict mode
        if (getResources().getBoolean(R.bool.developer_mode))
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    }
    
    public void chooseInstrument(View view) {
    	Intent intent = new Intent("com.davidjennes.ElectroJam.INSTRUMENT");
    	startActivity(Intent.createChooser(intent, getString(R.string.mode_instrument)));
    }
    
    public void clientActivity(View view) {
    	Intent intent = new Intent(this, SelectServerActivity.class);
    	startActivity(intent);
    }
    
    public void serverActivity(View view) {
    	Intent intent = new Intent(this, ServerActivity.class);
    	startActivity(intent);
    }
}