package com.davidjennes.ElectroJam;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class ServerMode extends Activity implements Runnable {
	private final String TAG = "ServerMode";
	private final String TYPE = "_eljam._tcp.local.";
	private final int PORT = 7654;
	
	private ServiceInfo m_serviceInfo;
	private JmDNS m_jmdns;
	private String m_name, m_description;
	private ProgressDialog m_progress;
	private volatile boolean m_stop = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server);

        // strict mode
        if (getResources().getBoolean(R.bool.developer_mode))
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        
        // create ZeroConf instance
        try {
			m_jmdns = JmDNS.create();
		} catch (IOException e) {
			Log.e(TAG, "Unable to create JmDNS instance.");
			e.printStackTrace();
			m_jmdns = null;
		}
    }
	
	public void onDestroy() {
		try {
			m_jmdns.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toggleServer(View view) {
		ToggleButton button = (ToggleButton) view;
		
		if (button.isChecked()) {
			m_name = ((EditText) findViewById(R.id.edit_name)).getText().toString();
			m_description = ((EditText) findViewById(R.id.edit_description)).getText().toString();

			m_progress = ProgressDialog.show(this, getString(R.string.working), getString(R.string.starting_server), true, false);
			Thread thread = new Thread(this);
			thread.start();
		} else {
			m_progress = ProgressDialog.show(this, getString(R.string.working), getString(R.string.stopping_server), true, false);
			m_stop = true;
		}
	}
	
	public void run() {
		m_stop = false;
		
		// startup sequence
		try {
			m_serviceInfo = ServiceInfo.create(TYPE, m_name, PORT, m_description);
			m_jmdns.registerService(m_serviceInfo);
		} catch (IOException e) {
			Log.e(TAG, "Error while starting JmDNS service.");
			e.printStackTrace();
		}
		m_progress.dismiss();
		
		// loop!
		while (!m_stop)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {}
		
		// shutdown sequence
		m_jmdns.unregisterAllServices();
		m_progress.dismiss();
	}
}
