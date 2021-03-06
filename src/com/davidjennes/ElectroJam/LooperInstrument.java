package com.davidjennes.ElectroJam;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import com.davidjennes.ElectroJam.Client.IInstrumentService;
import com.davidjennes.ElectroJam.Client.IInstrumentServiceCallback;

public class LooperInstrument extends Activity {
	private static final String TAG = LooperInstrument.class.getName();
	
	private int[] m_sounds;
	private Map<Integer, Integer> m_buttonToSound, m_soundToProgressID;
	private Map<Integer, ProgressBar> m_soundToProgress;
	private ProgressDialog m_progressDialog;
	private IInstrumentService m_instrumentService;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instrument_looper);
        
        // show progress dialog during loading
        m_progressDialog = ProgressDialog.show(this, getString(R.string.working), getString(R.string.loading_sounds), true, false);
        m_buttonToSound = new HashMap<Integer, Integer>();
        m_soundToProgressID = new HashMap<Integer, Integer>();
        m_soundToProgress = new HashMap<Integer, ProgressBar>();
        
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
		setContentView(R.layout.instrument_looper);
		
		try {
			// light up buttons for playing sounds
			for (Map.Entry<Integer, Integer> entry : m_buttonToSound.entrySet())
				if (m_instrumentService.isPlaying(entry.getValue()))
					((ToggleButton) findViewById(entry.getKey())).setChecked(true);
		
			// re-associate progress bars
			m_instrumentService.unregisterCallback(m_callback);
			for (Map.Entry<Integer, Integer> entry : m_soundToProgressID.entrySet())
				m_soundToProgress.put(entry.getKey(), (ProgressBar) findViewById(entry.getValue()));
			m_instrumentService.registerCallback(m_callback);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    public void onDestroy() {
        super.onDestroy();
        
    	try {
    		m_instrumentService.unregisterCallback(m_callback);
    		m_instrumentService.unloadSamples(m_sounds);
			unbindService(m_connection);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Called on click of one of the buttons
     * @param view The clicked button
     */
    public void buttonClick(View view) {
    	if (view == null)
    		return;
    	
    	// looped is stored in tag field
    	boolean looped = view.getTag().equals("1");
    	
    	// play/stop depending on ToggleButton state, otherwise just play
    	boolean play = true;
    	if (view instanceof ToggleButton && !((ToggleButton) view).isChecked())
    		play = false;
    	
    	// either play or stop    	
		try {
			if (play)
				m_instrumentService.playSound(m_buttonToSound.get(view.getId()), looped);
			else
		    	m_instrumentService.stopSound(m_buttonToSound.get(view.getId()));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Wait until we're connected to the bounded service (if at all)
     * Then connect the ListView with an adapter and start filling it with data
     */
	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			m_instrumentService = IInstrumentService.Stub.asInterface(service);
			new LoadSoundsTask().execute();
		}
		
		public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "Service has unexpectedly disconnected");
	        m_instrumentService = null;
	    }
	};
	

	/**
	 * Callback implementation
	 */
	private final IInstrumentServiceCallback.Stub m_callback = new IInstrumentServiceCallback.Stub() {
		/**
		 * Called when a sound reaches a certain progress while playing
		 * @param sound The sound's ID
		 * @param progress The new progress value
		 */
		public void updateProgress(int sound, int progress) {
			if (m_soundToProgress.containsKey(sound))
				m_soundToProgress.get(sound).setProgress(progress);
		}

		/**
		 * Called to send secondary progress information
		 * @param sound The sound's ID
		 * @param secondaryProgress The new secondary progress value (progress will go from 0 to this value)
		 */
		public void secondaryProgress(int sound, int secondaryProgress) {
			if (m_soundToProgress.containsKey(sound))
				m_soundToProgress.get(sound).setSecondaryProgress(secondaryProgress);
		}
	};
	
	/**
	 * Task to load necessary sounds to server
	 */
    private class LoadSoundsTask extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... params) {
			// Get associations data
	        int[] buttons = getTypedArrayAsIDArray(R.array.buttons);
	        int[] progressBars = getTypedArrayAsIDArray(R.array.progress_bars);
	        String[] sounds = getTypedArrayAsStringArray(R.array.sounds);
	        
	        // load sounds
	        try {
				m_sounds = m_instrumentService.loadSamples(sounds);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	        
	        // associate buttons to sounds
	        for (int i = 0; i < buttons.length; ++i)
	        	m_buttonToSound.put(buttons[i], m_sounds[i]);
	        
	        // associate sounds to progress bars
	        for (int i = 0; i < progressBars.length; ++i) {
        		m_soundToProgressID.put(m_sounds[i], progressBars[i]);
        		m_soundToProgress.put(m_sounds[i], (ProgressBar) findViewById(progressBars[i]));
	        }
	        
	        // register callback
	        try {
				m_instrumentService.registerCallback(m_callback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	        
			return null;
		}
		
		protected void onPostExecute(Void param) {
			m_progressDialog.dismiss();
			m_progressDialog = null;
		}
	}
    
    /**
     * Convert a typed array resource into a primitive array
     * @param id The resource ID
     * @return A primitive array of integers
     */
    private int[] getTypedArrayAsIDArray(int id) {
    	TypedArray array = getResources().obtainTypedArray(id);
    	
    	// convert to primite type
    	int[] result = new int[array.length()];
    	for (int i = 0; i < result.length; ++i)
    		result[i] = array.getResourceId(i, -1);
    	
    	return result;
    }
    
    /**
     * Convert a typed array resource into a primitive array
     * @param id The resource ID
     * @return An array of Strings
     */
    private String[] getTypedArrayAsStringArray(int id) {
    	TypedArray array = getResources().obtainTypedArray(id);
    	
    	// convert to primite type
    	String[] result = new String[array.length()];
    	for (int i = 0; i < result.length; ++i)
    		result[i] = "android.resource://" + getPackageName() + "/raw/" + array.getResourceId(i, -1);
    	
    	return result;
    }
}
