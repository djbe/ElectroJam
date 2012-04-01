package com.davidjennes.ElectroJam.Client;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
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
import com.davidjennes.ElectroJam.Sound.LocalSoundManager;
import com.davidjennes.ElectroJam.Sound.RemoteSoundManager;
import com.davidjennes.ElectroJam.Sound.SoundManager;

public class InstrumentService extends Service {
	private static final String TAG = InstrumentService.class.getName();
	private static final String LOCK_NAME = "ElectroJamInstrument-BonjourLock";
	private static final String TYPE = "_eljam._tcp.local.";
	
	// ZeroConf variables
	private MulticastLock m_lock;
	private JmDNS m_jmdns;
	private Map<Integer, ServiceInfo> m_services;
	private BonjourListener m_listener;
	
	// Client variables
	private SoundManager m_soundManager;
	
    public void onCreate() {
        super.onCreate();
        
        m_services = new HashMap<Integer, ServiceInfo>();
		m_listener = new BonjourListener(m_services);
		m_soundManager = new LocalSoundManager(this);
        
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
		 * @return List of server IDs
		 */
		public int[] availableServers() throws RemoteException {
			Integer[] services = null;
			
			// get IDs
			synchronized(m_services) {
				services = (Integer[]) m_services.keySet().toArray();
			}
			
			// convert to primitives array
			int[] result = new int[services.length];
			for (int i = 0; i < result.length; ++i)
				result[i] = services[i];
			
			return result;
		}
		
		/**
		 * Get discovered server info
		 * @param id Server ID
		 * @return Server info (array of 2 elements, name and description)
		 */
		public String[] serverInfo(int id) throws RemoteException {
			synchronized(m_services) {
				if (m_services.containsKey(id))
					return new String[] {
							m_services.get(id).getName(),
							m_services.get(id).getNiceTextString()
						};
				else
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
				
				// connect and create (fake) sound manager
				Socket socket = new Socket(info.getHostAddresses()[0], info.getPort());
				m_soundManager = new RemoteSoundManager(InstrumentService.this, socket);
				Toast.makeText(InstrumentService.this, R.string.client_connected, Toast.LENGTH_SHORT).show();
			} catch (Throwable e) {
				e.printStackTrace();
				Toast.makeText(InstrumentService.this, R.string.client_error_connecting, Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * Disconnect from server
		 */
		public void disconnect() {
			try {
				m_soundManager = null;
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				m_soundManager = new LocalSoundManager(InstrumentService.this);
				Toast.makeText(InstrumentService.this, R.string.client_disconnected, Toast.LENGTH_SHORT).show();
			}
		}

		/**
		 * Check whether we're connected to a server
		 */
		public boolean isConnected() {
			return (m_soundManager instanceof RemoteSoundManager);
		}
		
		/**
		 * Load samples for current instrument
		 * This WILL take a while, so make sure to use a Handler or aSyncTask
		 * @param samples A list of samples, will be replaced by a list of IDs
		 */
		public void loadSamples(int[] samples) throws RemoteException {
			for (int i = 0; i < samples.length; ++i)
				samples[i] = m_soundManager.loadSound(samples[i]);
		}
		
		/**
		 * Unload the specified sounds
		 * @param IDs A list of IDs to unload 
		 */
		public void unloadSamples(int [] IDs) {
			for (int id : IDs)
				m_soundManager.unloadSound(id);
		}

		/**
		 * Send an instrument event
		 * @param sample The ID of the sample
		 * @param looped Whether to play the sample in a looped fashion
		 */
		public void playSound(int sample, boolean looped) {
			m_soundManager.playSound(sample, looped);
		}
		
		/**
		 * Send an instrument event
		 * @param sample The ID of the sample
		 */
		public void stopSound(int sample) {
			m_soundManager.stopSound(sample);
		}

		/**
		 * Check if a sound is playing
		 * @return True if playing
		 */
		public boolean isPlaying(int sample) {
			return m_soundManager.isPlaying(sample);
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
