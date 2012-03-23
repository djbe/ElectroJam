package com.davidjennes.ElectroJam;

import java.util.Random;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class ServerActivity extends Activity {
	private ServerService m_service;
	private boolean m_running;
	
	private ServiceConnection m_connection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	m_service = ((ServerService.LocalBinder) service).getService();
	    	m_running = m_service.isServerRunning();
	    	
	    	updateUI();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        m_service = null;
	        m_running = false;
	    }
	};
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);
        
        // strict mode
        if (getResources().getBoolean(R.bool.developer_mode))
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        
        // random name
        EditText e = (EditText) findViewById(R.id.edit_name);
        e.setText(e.getText().toString() + "-" + new Random().nextInt());
        
        // connect to service
        m_running = false;
        bindService(new Intent(this, ServerService.class), m_connection, Context.BIND_AUTO_CREATE);
    }
    
    /**
     * Do not reload sounds on screen rotation 
     */
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
        setContentView(R.layout.server);
        
        // update UI
        updateUI();
    }
	
	public void onDestroy() {
		super.onDestroy();
		unbindService(m_connection);
	}
	
	/**
	 * Turn the server on and off
	 * @param view The clicked button
	 */
	public void toggleServer(View view) {
		ToggleButton button = (ToggleButton) view;
		m_running = button.isChecked();
		
		if (m_running) {
			Intent intent = new Intent(this, ServerService.class);
			intent.setAction("start");
			intent.putExtra("name", ((EditText) findViewById(R.id.edit_name)).getText().toString());
			intent.putExtra("description", ((EditText) findViewById(R.id.edit_description)).getText().toString());
			m_service.startService(intent);
		} else {
			Intent intent = new Intent(this, ServerService.class);
			intent.setAction("stop");
			m_service.startService(intent);
		}
		
		updateUI();
	}
	
	/**
	 * Update fields depending on server status
	 */
	private void updateUI() {
		ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleServer);
		View name = findViewById(R.id.edit_name);
		View description = findViewById(R.id.edit_description);
		
		if (m_service != null)
			toggle.setEnabled(true);
		
		// toggle = server status
		toggle.setChecked(m_running);
		
		// disable edit fields when running
		name.setEnabled(!m_running);
		description.setEnabled(!m_running);
	}
}
