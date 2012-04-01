package com.davidjennes.ElectroJam.Client;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.util.Log;

/**
 * Listener for ZeroConf events
 */
public class BonjourListener implements ServiceListener {
	private static final String TAG = "BonjourListener";
	private final static Random RANDOM = new Random();
	
	private Map<Integer, ServiceInfo> m_serviceIDToInfo;
	private Map<String, Integer> m_serviceNameToID;
	
	public JmDNS jmdns;
	
	/**
	 * Constructor
	 */
	public BonjourListener(Map<Integer, ServiceInfo> services) {
		jmdns = null;
		m_serviceIDToInfo = services;
		m_serviceNameToID = new HashMap<String, Integer>();
	}
	
	/**
	 * We found all the info on a service
	 */
	public void serviceResolved(ServiceEvent ev) {
		String name = ev.getInfo().getQualifiedName();
		
		synchronized(m_serviceIDToInfo) {
			if (m_serviceNameToID.containsKey(name))
				m_serviceIDToInfo.put(m_serviceNameToID.get(name), ev.getInfo());
			else {
				m_serviceNameToID.put(name, RANDOM.nextInt());
				m_serviceIDToInfo.put(m_serviceNameToID.get(name), ev.getInfo());
				Log.i(TAG, "Found: " + name);
			}
		}
    }
	
	/**
	 * Remove a (known?) service
	 */
    public void serviceRemoved(ServiceEvent ev) {
    	String name = ev.getInfo().getQualifiedName();
    	
    	synchronized(m_serviceIDToInfo) {
    		if (m_serviceNameToID.containsKey(name)) {
    			m_serviceIDToInfo.remove(m_serviceNameToID.get(name));
    			m_serviceNameToID.remove(name);
    		}
		}
    }
    
    /**
     * Found a new service, now resolve it's info
     */
    public void serviceAdded(ServiceEvent event) {
        // Required to force serviceResolved to be called again (after the first search)
        jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
    }
}