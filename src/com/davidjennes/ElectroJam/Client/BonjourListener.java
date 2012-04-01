package com.davidjennes.ElectroJam.Client;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.util.Log;

/**
 * Listener for ZeroConf events
 */
public class BonjourListener implements ServiceListener {
	private static final String TAG = BonjourListener.class.getName();
	private final static Random RANDOM = new Random();
	
	private ConcurrentMap<Integer, ServiceInfo> m_serviceIDToInfo;
	private ConcurrentMap<String, Integer> m_serviceNameToID;
	
	public JmDNS jmdns;
	
	/**
	 * Constructor
	 */
	public BonjourListener(ConcurrentMap<Integer, ServiceInfo> services) {
		jmdns = null;
		m_serviceIDToInfo = services;
		m_serviceNameToID = new ConcurrentHashMap<String, Integer>();
	}
	
	/**
	 * We found all the info on a service
	 */
	public void serviceResolved(ServiceEvent ev) {
		String name = ev.getInfo().getQualifiedName();
		Integer id = m_serviceNameToID.get(name);
		
		// found a new service
		if (id == null) {
			id = RANDOM.nextInt();
			Log.i(TAG, "Found: " + name);
		}
		
		// store info
		m_serviceNameToID.putIfAbsent(name, id);
		m_serviceIDToInfo.put(id, ev.getInfo());
    }
	
	/**
	 * Remove a (known?) service
	 */
    public void serviceRemoved(ServiceEvent ev) {
    	String name = ev.getInfo().getQualifiedName();
    	
    	m_serviceIDToInfo.remove(m_serviceNameToID.get(name));
		m_serviceNameToID.remove(name);
    }
    
    /**
     * Found a new service, now resolve it's info
     */
    public void serviceAdded(ServiceEvent event) {
        // Required to force serviceResolved to be called again (after the first search)
        jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
    }
}