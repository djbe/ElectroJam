package com.davidjennes.ElectroJam.Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.davidjennes.ElectroJam.R;
import com.davidjennes.ElectroJam.SoundManager;

public class InstrumentService extends Service {
	private static final String TAG = "InstrumentService";
	private static final String LOCK_NAME = "ElectroJamInstrument-BonjourLock";
	private static final String TYPE = "_eljam._tcp.local.";
	private static enum Mode {LOCAL, REMOTE};
	
	// ZeroConf variables 
	private MulticastLock m_lock;
	private JmDNS m_jmdns;
	private Map<Integer, ServiceInfo> m_services;
	private BonjourListener m_listener;
	
	// Client variables
	private Socket m_socket;
	private PrintWriter m_writer;
	private Mode m_mode;
	
	// Sound variables
	private SoundManager m_soundManager;
	
    public void onCreate() {
        super.onCreate();
        
        m_mode = Mode.LOCAL;
        m_socket = null;
        m_writer = null;
        m_services = new HashMap<Integer, ServiceInfo>();
		m_listener = new BonjourListener(m_services);
		m_soundManager = new SoundManager(getApplicationContext());
        
		new InitTask().execute();
        Log.i(TAG, "Instrument service created.");
    }
	
    public IBinder onBind(Intent intent) {
    	return m_binder;
    }
	
    public void onDestroy() {
    	super.onDestroy();
    	
    	// stop ZeroConf
    	if (m_jmdns != null)
	    	try {
		    	m_jmdns.removeServiceListener(TYPE, m_listener);
				m_jmdns.close();
			} catch (IOException e) {}
    	
    	// Release multicast lock
    	if (m_lock != null)
    		m_lock.release();
    	
        Log.i(TAG, "Instrument service destroyed.");
	}
	
    /**
     * Service implementation
     */
	private final IInstrumentService.Stub m_binder = new IInstrumentService.Stub() {
		/**
		 * List of discovered servers
		 * @return List of server IDs (Integers)
		 */
		@SuppressWarnings("rawtypes")
		public List availableServers() throws RemoteException {
			List<Integer> result = new ArrayList<Integer>();
			
			synchronized(m_services) {
				for (Integer id : m_services.keySet())
					result.add(id);
			}
			
			return result;
		}
		
		/**
		 * Get discovered server info
		 * @param id Server ID
		 * @return Server info (name & description)
		 */
		@SuppressWarnings("rawtypes")
		public Map serverInfo(int id) throws RemoteException {
			synchronized(m_services) {
				if (m_services.containsKey(id)) {
					Map<String, String> result = new HashMap<String, String>();
					result.put("id", Integer.toString(id));
					result.put("name", m_services.get(id).getName());
					result.put("description", m_services.get(id).getNiceTextString());
					return result;
				} else
					return null;
			}
		}
		
		/**
		 * Connect to a certain server
		 * @param id Server ID
		 */
		public void connect(int id) throws RemoteException {
			ServiceInfo info = null;
			
			// get service info
			synchronized(m_services) {
				if (m_services.containsKey(id))
					info = m_services.get(id);
			}
			
			try {
				if (info == null)
					throw new Throwable();
				
				m_socket = new Socket(info.getHostAddresses()[0], info.getPort());
				m_writer = new PrintWriter(m_socket.getOutputStream(), true);
				m_soundManager = null;
				m_mode = Mode.REMOTE;
				Toast.makeText(getApplicationContext(), R.string.client_connected, Toast.LENGTH_SHORT).show();
			} catch (Throwable e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), R.string.client_error_connecting, Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * Disconnect from server
		 */
		public void disconnect() {
			try {
				m_writer.close();
				m_socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				m_writer = null;
				m_socket = null;
				m_soundManager = new SoundManager(getApplicationContext());
				m_mode = Mode.LOCAL;
				Toast.makeText(getApplicationContext(), R.string.client_disconnected, Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * Check whether we're connected to a server
		 */
		public boolean isConnected() {
			return m_mode == Mode.LOCAL;
		}
		
		/**
		 * Load samples for current instrument
		 * This WILL take a while, so make sure to use a Handler or aSyncTask
		 * @param samples A map from sample names to filenames
		 */
		@SuppressWarnings("rawtypes")
		public void loadSamples(Map samples) throws RemoteException {
			if (m_mode == Mode.LOCAL) {
				;
			} else
				Log.d(TAG, "Load samples.");
		}

		/**
		 * Send an instrument event
		 * @param sample The ID of the sample
		 * @param looped Whether to play the sample in a looped fashion
		 */
		public void playSound(int sample, boolean looped) {
			if (m_mode == Mode.LOCAL)
				m_soundManager.playSound(sample, looped);
			else {
				Log.d(TAG, "Send event. sample: " + sample + " looped: " + looped);
				m_writer.println("START" + (looped ? 1 : 2) + sample);
			}
		}
		
		/**
		 * Send an instrument event
		 * @param sample The ID of the sample
		 */
		public void stopSound(int sample) {
			if (m_mode == Mode.LOCAL)
				m_soundManager.stopSound(sample);
			else {
				Log.d(TAG, "Send event. sample: " + sample + " stop!");
				m_writer.println("STOP" + sample);
			}
		}
	};
	
	/**
	 * Task to move some initialization to the background, to comply with strict mode restrictions
	 */
	class InitTask extends AsyncTask<Void, Void, Void> {
    	protected Void doInBackground(Void... params) {
			// Acquire lock to be able to process multicast
	        WifiManager wifi = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
	        m_lock = wifi.createMulticastLock(LOCK_NAME);
	        m_lock.setReferenceCounted(true);
	        m_lock.acquire();

			// Start listening for ZeroConf services
    		try {
    			m_jmdns = JmDNS.create();
    			m_listener.jmdns = m_jmdns;
    			m_jmdns.addServiceListener(TYPE, m_listener);
    		} catch (IOException e) {
    			e.printStackTrace();
    			m_jmdns = null;
    		}
    		
    		Log.i(TAG, "Instrument service initialized.");
			return null;
		}        	
    }
}
