package com.davidjennes.ElectroJam;

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
	
	private Map<Integer, ServiceInfo> m_services;
	private Map<String, Integer> m_serviceNames;
	
	public JmDNS jmdns;
	
	/**
	 * Constructor
	 */
	public BonjourListener(Map<Integer, ServiceInfo> services) {
		jmdns = null;
		m_services = services;
		m_serviceNames = new HashMap<String, Integer>();;
	}
	
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
				Log.i(TAG, "Found: " + ev.getInfo().getQualifiedName());
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
        jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
    }
}