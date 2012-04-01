package com.davidjennes.ElectroJam.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.davidjennes.ElectroJam.R;

public class SelectServerActivity extends Activity {
	private static final String TAG = SelectServerActivity.class.getName();;
	private static final int REPEAT_DISCOVERY = 2000;
	private static final String InfoField_ID = "id";
	private static final String InfoField_NAME = "name";
	private static final String InfoField_DESCRIPTION = "description";
	
	private List<Map<String, String>> m_data;
	private SimpleAdapter m_adapter;
	private IInstrumentService m_instrumentService;
	private Timer m_timer = new Timer();
    final Handler handler = new Handler();
	
    /**
     * Wait until we're connected to the bounded service (if at all)
     * Then connect the ListView with an adapter and start filling it with data
     */
	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			m_instrumentService = IInstrumentService.Stub.asInterface(service);
			initListView();
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
        m_data = new ArrayList<Map<String, String>>();
        
        // automated server discovery every few seconds
        m_timer.scheduleAtFixedRate(new RepeatServerDiscovery(), 0, REPEAT_DISCOVERY);
        
        // connect to bounded instrument service
        Intent intent = new Intent();
        intent.setClassName(getPackageName(), getPackageName() + ".Client.InstrumentService");
        if (!bindService(intent, m_connection, Context.BIND_AUTO_CREATE))
        	Log.e(TAG, "Couldn't bind to local service");
    }

    /**
     * Do not reload sounds on screen rotation 
     */
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	setContentView(R.layout.select_server);
    	
    	// re-init view list
    	initListView();
    }
    
    public void onDestroy() {
		super.onDestroy();
    	
		m_timer.cancel();
		m_timer = null;
		unbindService(m_connection);
    }
    
    /**
     * Finish initializing the list view (called when the service is connected)
     */
    private void initListView() {
    	// connect ListView to data
        m_adapter = new SimpleAdapter(this, m_data,
        		android.R.layout.simple_list_item_2,
        		new String[] {InfoField_NAME, InfoField_DESCRIPTION}, // map keys to views
        		new int[] {android.R.id.text1, android.R.id.text2});
        ListView listView = (ListView) findViewById(R.id.server_list);
        listView.setAdapter(m_adapter);
        
        // listen to clicks on items
        listView.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		Log.d(TAG, "Clicked item: " + position);
        		Integer server = Integer.parseInt(m_data.get(position).get(InfoField_ID));
        		new ServerConnectTask().execute(server);
        	}
        });
        
        // fetch servers
        ServerDiscoveryTask task = new ServerDiscoveryTask();
    	task.execute();
    }
    
    /**
     * Fetch discovered servers from local service
     */
	private class ServerDiscoveryTask extends AsyncTask<Void, Void, Void> {
		private List<Map<String, String>> m_tempData;
		
		/**
		 * Fetch discovered servers and their info
		 */
		protected Void doInBackground(Void... params) {
			try {
				m_tempData = new ArrayList<Map<String, String>>();
				
				// fetch server IDs
				int[] serverIDs = m_instrumentService.availableServers();
				
				// fetch info for each server
				for (int id : serverIDs) {
					String[] info = m_instrumentService.serverInfo(id);
					
					// convert array to map 
					if (info != null) {
						Map<String, String> mappedInfo = new HashMap<String, String>();
						mappedInfo.put(InfoField_ID, Integer.toString(id));
						mappedInfo.put(InfoField_NAME, info[0]);
						mappedInfo.put(InfoField_DESCRIPTION, info[1]);
						m_tempData.add(mappedInfo);
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		/**
		 * Replace data and notify observer
		 */
		protected void onPostExecute(Void param) {
			m_data.clear();
			m_data.addAll(m_tempData);
			m_adapter.notifyDataSetChanged();
		}
	}
	
	/**
     * Connect local instrument service to a given server
     */
	private class ServerConnectTask extends AsyncTask<Integer, Void, Void> {
		protected Void doInBackground(Integer... params) {
			if (params.length != 1)
				return null;
			
			try {
				// connect
				m_instrumentService.connect(params[0]);
				
				// open instrument
		    	Intent intent = new Intent(getPackageName() + ".INSTRUMENT");
		    	startActivity(Intent.createChooser(intent, getString(R.string.mode_instrument)));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			return null;
		}
	}

	/**
	 * Server discovery timer task
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
