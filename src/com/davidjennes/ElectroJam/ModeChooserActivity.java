package com.davidjennes.ElectroJam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ModeChooserActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // strict mode
        if (getResources().getBoolean(R.bool.developer_mode))
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        
        final Button client = (Button) findViewById(R.id.button_client);
        client.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// TODO: choose instruments
            	Intent intent = new Intent(v.getContext(), InstrumentActivity.class);
            	startActivity(intent);
            }
        });
        
        final Button server = (Button) findViewById(R.id.button_server);
        server.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(v.getContext(), ServerMode.class);
            	startActivity(intent);
            }
        });
        
        Log.d("Mode Chooser", "Hey!");
    }
}