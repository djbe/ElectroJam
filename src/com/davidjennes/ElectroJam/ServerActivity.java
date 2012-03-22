package com.davidjennes.ElectroJam;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class ServerActivity extends Activity {
	private Server m_server;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);

        // strict mode
        if (getResources().getBoolean(R.bool.developer_mode))
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        
        // random name
        EditText e = (EditText) findViewById(R.id.edit_name);
        e.setText(e.getText().toString() + "-" + new Random().nextInt());
        
        // init server
        m_server = new Server(this);
    }
	
	public void onDestroy() {
		super.onDestroy();
		m_server = null;
	}
	
	/**
	 * Turn the server on and off
	 * @param view The clicked button
	 */
	public void toggleServer(View view) {
		ToggleButton button = (ToggleButton) view;
		
		if (button.isChecked()) {
			String name = ((EditText) findViewById(R.id.edit_name)).getText().toString();
			String description = ((EditText) findViewById(R.id.edit_description)).getText().toString();
			
			m_server.start(name,  description);
		} else
			m_server.stop();
	}
}
