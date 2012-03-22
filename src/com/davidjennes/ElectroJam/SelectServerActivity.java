package com.davidjennes.ElectroJam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SelectServerActivity extends Activity {
	private static final String TAG = "InstrumentActivity";
	private static final String APP_ID = "com.davidjennes.ElectroJam";
	private static final int REPEAT_DISCOVERY = 2000;
	
	private List<Map<String, String>> m_data;
	private SimpleAdapter m_adapter;
	private IInstrumentService m_instrumentService;
	private Timer m_timer = new Timer();
    final Handler handler = new Handler();
	
    /**
     * Wait until we're connected to the local service (if at all)
     */
	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			m_instrumentService = IInstrumentService.Stub.asInterface(service);
			
	        // connect ListView to data
	        m_data = new ArrayList<Map<String, String>>();
	        m_adapter = new SimpleAdapter(getApplicationContext(), m_data,
	        		android.R.layout.simple_list_item_2,
	        		new String[] {"name", "description"},
	        		new int[] {android.R.id.text1, android.R.id.text2});
	        ListView listView = (ListView) findViewById(R.id.server_list);
	        listView.setAdapter(m_adapter);
			
	        // fetch servers
	        ServerDiscoveryTask task = new ServerDiscoveryTask();
	    	task.execute();
		}
		
		public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "Service has unexpectedly disconnected");
	        m_instrumentService = null;
	    }
	};
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_server);
        
        // strict mode
        if (getResources().getBoolean(R.bool.developer_mode))
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        
        // repeat server discovery
        m_timer.scheduleAtFixedRate(new RepeatServerDiscovery(), 0, REPEAT_DISCOVERY);

        // connect to service
        Intent intent = new Intent();
        intent.setClassName(APP_ID, APP_ID + ".InstrumentService");
        boolean bound = getApplicationContext().bindService(intent, m_connection, Context.BIND_AUTO_CREATE);
        if (!bound)
        	Log.e(TAG, "Couldn't bind to local service");
    }
    
    public void onDestroy() {
		super.onDestroy();
    	
		m_timer.cancel();
		m_timer = null;
    }
    
    /**
     * Fetch discovered servers from local service
     */
	private class ServerDiscoveryTask extends AsyncTask<Void, Void, Void> {
		private List<Map<String, String>> m_tempData;
		
		@SuppressWarnings("unchecked")
		protected Void doInBackground(Void... params) {
			try {
				m_tempData = new ArrayList<Map<String, String>>();
				
				// fetch server IDs
				List<Integer> serverIDs = m_instrumentService.availableServers();
				
				// fetch info for each server
				for (int id : serverIDs) {
					Map<String, String> info = m_instrumentService.serverInfo(id);
					if (info != null)
						m_tempData.add(info);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		protected void onPostExecute(Void param) {
			m_data.clear();
			m_data.addAll(m_tempData);
			m_adapter.notifyDataSetChanged();
		}
	}

	/**
	 * server discovery timer task
	 */
	class RepeatServerDiscovery extends TimerTask {
		public void run() {
			handler.post(new Runnable() {
                public void run() {
                	if (m_instrumentService != null) {
                		ServerDiscoveryTask task = new ServerDiscoveryTask();
            	    	task.execute();
                	}
                }
			});
		}
	}
}
