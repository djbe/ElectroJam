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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class InstrumentService extends Service {
	private static final String TAG = "InstrumentService";
	private static final String LOCK_NAME = "ElectroJamInstrument-BonjourLock";
	private static final String TYPE = "_workstation._tcp.local.";
	private final static Random RANDOM = new Random();
	
	private MulticastLock m_lock;
	private JmDNS m_jmdns;
	private Map<Integer, ServiceInfo> m_services;
	private Socket m_socket;
	private PrintWriter m_writer;
	
    public void onCreate() {
        super.onCreate();
        m_socket = null;
        m_writer = null;
        
        // Acquire lock to be able to process multicast
        WifiManager wifi = (WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
        m_lock = wifi.createMulticastLock(LOCK_NAME);
        m_lock.setReferenceCounted(true);
        m_lock.acquire();
        
        // init JmDNS
        try {
			m_jmdns = JmDNS.create();
			m_jmdns.addServiceListener(TYPE, m_listener);
		} catch (IOException e) {
			e.printStackTrace();
			m_jmdns = null;
		}
    }
	
    public IBinder onBind(Intent intent) {
    	return m_binder;
    }
	
    public void onDestroy() {
    	if (m_jmdns != null) {
		    try {
		    	m_jmdns.removeServiceListener(TYPE, m_listener);
				m_jmdns.close();
			} catch (IOException e) {}    		
    	}
    	
    	if (m_lock != null)
    		m_lock.release();
	}
	
    /**
     * Service implementation
     */
	private final IInstrumentService.Stub m_binder = new IInstrumentService.Stub() {
		public List availableServers() throws RemoteException {
			synchronized(m_services) {
				List<Integer> result = new ArrayList<Integer>();
				
				for (Integer id : m_services.keySet())
					result.add(id);
				
				return result;
			}
		}
		
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
		
		public void loadSamples(Map samples) throws RemoteException {
			Log.d(TAG, "Load samples.");
		}
		
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
		public void serviceResolved(ServiceEvent ev) {
			synchronized(m_services) {
				m_services.put(RANDOM.nextInt(), ev.getInfo());
			}
        }
		
        public void serviceRemoved(ServiceEvent ev) {
        	String name = ev.getInfo().getQualifiedName();
        	int key = 0;
        	
        	synchronized(m_services) {
        		for (Map.Entry<Integer, ServiceInfo> entry : m_services.entrySet())
        			if (entry.getValue().getQualifiedName().equals(name)) {
        				key = entry.getKey();
        				break;
        			}
        		
        		m_services.remove(key);
			}
        }
        
        public void serviceAdded(ServiceEvent event) {
            // Required to force serviceResolved to be called again (after the first search)
            m_jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
        }
    };
}
