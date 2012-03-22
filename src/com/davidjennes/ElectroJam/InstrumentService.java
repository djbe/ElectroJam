package com.davidjennes.ElectroJam;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class InstrumentService extends Service {
	private static final String TAG = "InstrumentService";
	private static final String LOCK_NAME = "ElectroJamInstrument-BonjourLock";
//	private static final String TYPE = "_eljam._tcp.local.";
	private static final String TYPE = "_ssh._tcp.local.";
	private final static Random RANDOM = new Random();
	
	private MulticastLock m_lock;
	private JmDNS m_jmdns;
	private Map<Integer, ServiceInfo> m_services;
	private Map<String, Integer> m_serviceNames;
	private Socket m_socket;
	private PrintWriter m_writer;
	
    public void onCreate() {
        super.onCreate();
        m_socket = null;
        m_writer = null;
        m_services = new HashMap<Integer, ServiceInfo>();
        m_serviceNames = new HashMap<String, Integer>();
        
        // Acquire lock to be able to process multicast
        WifiManager wifi = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
        m_lock = wifi.createMulticastLock(LOCK_NAME);
        m_lock.setReferenceCounted(true);
        m_lock.acquire();
        
        // init JmDNS (async)
        new AsyncTask<Void, Void, Void>() {
        	protected Void doInBackground(Void... params) {
        		initJmDNS();
    			return null;
    		}        	
        }.execute();
    }
	
    public IBinder onBind(Intent intent) {
    	return m_binder;
    }
	
    public void onDestroy() {
    	super.onDestroy();
    	
    	stopJmDNS();
    	if (m_lock != null)
    		m_lock.release();
	}
    
    private void initJmDNS() {
    	try {
			m_jmdns = JmDNS.create();
			m_jmdns.addServiceListener(TYPE, m_listener);
		} catch (IOException e) {
			e.printStackTrace();
			m_jmdns = null;
		}
    }
    
    private void stopJmDNS() {
    	if (m_jmdns == null)
    		return;
    	
	    try {
	    	m_jmdns.removeServiceListener(TYPE, m_listener);
			m_jmdns.close();
		} catch (IOException e) {}
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
			
			// does it still exist
			if (info == null)
				Toast.makeText(getApplicationContext(), R.string.client_error_connect, Toast.LENGTH_SHORT).show();
			else {
				try {
					m_socket = new Socket(info.getHostAddresses()[0], info.getPort());
					m_writer = new PrintWriter(m_socket.getOutputStream(), true);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Load samples for current instrument
		 * This WILL take a while, so make sure to use a Handler or aSyncTask
		 * @param samples A map from sample names to filenames
		 */
		@SuppressWarnings("rawtypes")
		public void loadSamples(Map samples) throws RemoteException {
			Log.d(TAG, "Load samples.");
		}
		
		/**
		 * Send an instrument event
		 * @param sample The name of the sample
		 * @param mode The play mode (single, loop)
		 */
		public void sendEvent(int sample, int mode) throws RemoteException {
			if (m_socket == null || m_writer == null)
				return;
			
			Log.d(TAG, "Send event. sample: " + sample + " mode: " + mode);
			m_writer.println("Sample: " + sample + " mode: " + mode);
		}
	};
	
	/**
	 * Listener for ZeroConf events
	 */
	private final ServiceListener m_listener = new ServiceListener() {
		/**
		 * We found all the info on a service
		 */
		public void serviceResolved(ServiceEvent ev) {
			String name = ev.getInfo().getQualifiedName();
			
			synchronized(m_services) {
				if (m_serviceNames.containsKey(name))
					m_services.put(m_serviceNames.get(name), ev.getInfo());
				else {
					m_serviceNames.put(name, RANDOM.nextInt());
					m_services.put(m_serviceNames.get(name), ev.getInfo());
					Log.d(TAG, "Found: " + ev.getInfo().getQualifiedName());
				}
			}
        }
		
		/**
		 * Remove a (known?) service
		 */
        public void serviceRemoved(ServiceEvent ev) {
        	String name = ev.getInfo().getQualifiedName();
        	
        	synchronized(m_services) {
        		if (m_serviceNames.containsKey(name)) {
        			m_services.remove(m_serviceNames.get(name));
        			m_serviceNames.remove(name);
        		}
			}
        }
        
        /**
         * Found a new service, now resolve it's info
         */
        public void serviceAdded(ServiceEvent event) {
            // Required to force serviceResolved to be called again (after the first search)
            m_jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
        }
    };
}
