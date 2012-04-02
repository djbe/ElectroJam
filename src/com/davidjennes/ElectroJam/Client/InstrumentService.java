package com.davidjennes.ElectroJam.Client;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
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
	private ConcurrentMap<Integer, ServiceInfo> m_services;
	private BonjourListener m_listener;
	
	// Client variables
	private RemoteCallbackList<IInstrumentServiceCallback> m_callbacks;
	private SoundManager m_soundManager;
	private Handler m_toastHandler;
	
    public void onCreate() {
        super.onCreate();
        
        m_services = new ConcurrentHashMap<Integer, ServiceInfo>();
		m_listener = new BonjourListener(m_services);
		m_soundManager = new LocalSoundManager(this, m_handler);
		m_callbacks = new RemoteCallbackList<IInstrumentServiceCallback>();
		m_toastHandler = new Handler();
        
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
    	
    	// Unregister all callbacks
    	m_callbacks.kill();
        Log.i(TAG, "Instrument service destroyed.");
	}
    
    /**
     * Display a toast message
     * @param resid The text message's ID to show
     */
	private void showToast(final int resid) {
		m_toastHandler.post(new Runnable() {
			public void run() {
				Toast.makeText(InstrumentService.this, resid, Toast.LENGTH_SHORT).show();
			}
		});
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
			Integer[] services = (Integer[]) m_services.keySet().toArray();
			
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
			ServiceInfo info = m_services.get(id);
			
			if (info != null)
				return new String[] {info.getName(), info.getNiceTextString()};
			else
				return null;
		}
		
		/**
		 * Connect to a certain server
		 * @param id Server ID
		 */
		public void connect(int id) throws RemoteException {
			try {
				// get info
				ServiceInfo info = m_services.get(id);
				if (info == null)
					throw new Throwable();
				
				// connect and create (fake) sound manager
				Socket socket = new Socket(info.getHostAddresses()[0], info.getPort());
				m_soundManager = new RemoteSoundManager(InstrumentService.this, m_handler, socket);
				showToast(R.string.client_connected);
			} catch (Throwable e) {
				e.printStackTrace();
				showToast(R.string.client_error_connecting);
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
				m_soundManager = new LocalSoundManager(InstrumentService.this, m_handler);
				showToast(R.string.client_disconnected);
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
		
		/**
		 * Register a callback to receive progress notifications
		 * @param callback A callback instance
		 */
		public void registerCallback(IInstrumentServiceCallback callback) {
			if (callback != null) {
				m_callbacks.register(callback);
				m_soundManager.progressListenerRegistered();
			}
		}
		
		/**
		 * Remove a callback to stop receiving notifications
		 * @param callback A callback instance
		 */
		public void unregisterCallback(IInstrumentServiceCallback callback) {
			if (callback != null)
				m_callbacks.unregister(callback);
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
	
	/**
	 * Handle messages from SoundManager
	 */
	private final Handler m_handler = new Handler() {
		public void handleMessage(Message msg) {
			final int N = m_callbacks.beginBroadcast();
			
			switch (msg.what) {
			case SoundManager.UPDATE_PROGRESS:
                for (int i = 0; i < N; ++i)
                    try {
                    	m_callbacks.getBroadcastItem(i).updateProgress(msg.arg1, msg.arg2);
                    } catch (RemoteException e) {}
				break;
			case SoundManager.UPDATE_SECONDARY:
				for (int i = 0; i < N; ++i)
                    try {
                    	m_callbacks.getBroadcastItem(i).secondaryProgress(msg.arg1, msg.arg2);
                    } catch (RemoteException e) {}
				break;
			default:
				super.handleMessage(msg);
			}
			
            m_callbacks.finishBroadcast();
		}
	};
}
