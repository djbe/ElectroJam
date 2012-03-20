package com.davidjennes.ElectroJam;

import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

public class InstrumentActivity extends Activity {
	private static final String TAG = "InstrumentActivity";
	
	private IInstrumentService m_instrumentService;
	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d(TAG, "Service has connected");
			m_instrumentService = IInstrumentService.Stub.asInterface(service);
		}
		
		public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "Service has unexpectedly disconnected");
	        m_instrumentService = null;
	    }
	};
	
	private class ReallyLongTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... files) {
			try {
				m_instrumentService.loadSamples(new HashMap<String, String>());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			return "";
		}
		
		protected void onPostExecute(String result) {
			Log.d(TAG, "Finished long task");
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instrument);
        
        // strict mode
        if (getResources().getBoolean(R.bool.developer_mode))
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        
        // connect to service
        Intent intent = new Intent();
        intent.setClassName("com.davidjennes.ElectroJam", "com.davidjennes.ElectroJam.InstrumentService");
        boolean bound = getApplicationContext().bindService(intent, m_connection, Context.BIND_AUTO_CREATE);
        if (!bound)
        	Log.e(TAG, "Couldn't bind!");
    }
	
	public void contactService(View view) {
		if (m_instrumentService == null)
			return;
		
		ReallyLongTask task = new ReallyLongTask();
		task.execute("Hey", "Hallo");
	}
}
