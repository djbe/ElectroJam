package com.davidjennes.ElectroJam;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

public class ServerMode extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);

        // strict mode
        if (getResources().getBoolean(R.bool.developer_mode))
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
    }
	
	public void startServer(View view) {
		
	}
	
	public void stopServer(View view) {
		
	}
}
