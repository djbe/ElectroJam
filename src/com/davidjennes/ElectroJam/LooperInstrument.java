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
import android.widget.ToggleButton;

import com.davidjennes.ElectroJam.Client.IInstrumentService;
import com.davidjennes.ElectroJam.Sound.SoundManager;
import com.davidjennes.ElectroJam.Sound.LocalSoundManager;

public class LooperInstrument extends Activity {
	private static final String TAG = "LooperInstrument";
	
	private SoundManager m_soundManager;
	private Map<Integer, Integer> m_buttonSound, m_buttonProgress;
	private ProgressDialog m_progressDialog;
	private IInstrumentService m_instrumentService;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instrument_looper);
        
        // show progress dialog during loading
        m_progressDialog = ProgressDialog.show(this, getString(R.string.working), getString(R.string.loading_sounds), true, false);
        
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
		
		for (Map.Entry<Integer, Integer> entry : m_buttonSound.entrySet()) {
			int sound = entry.getValue();
			
			// light up buttons for playing sounds
			if (m_soundManager.isPlaying(sound))
				((ToggleButton) findViewById(entry.getKey())).setChecked(true);
			
			// re-associate progress bars
			if (m_buttonProgress.containsKey(entry.getKey()))
				m_soundManager.setProgressBar(sound, findViewById(m_buttonProgress.get(entry.getKey())));
		}
    }
    
    public void onDestroy() {
        super.onDestroy();
        
        for (Map.Entry<Integer, Integer> entry : m_buttonSound.entrySet())
    		m_soundManager.unloadSound(entry.getValue());
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
			if (play) {
				m_soundManager.playSound(m_buttonSound.get(view.getId()), looped);
				m_instrumentService.playSound(m_buttonSound.get(view.getId()), looped);
			} else {
		    	m_soundManager.stopSound(m_buttonSound.get(view.getId()));
		    	m_instrumentService.stopSound(m_buttonSound.get(view.getId()));
			}
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
	 * Task to load necessary sounds to server
	 */
    private class LoadSoundsTask extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... params) {
			int button, progress, sound;
			m_soundManager = new LocalSoundManager(getBaseContext());
			m_buttonSound = new HashMap<Integer, Integer>();
	        m_buttonProgress = new HashMap<Integer, Integer>();
	        
	        // Get associations data
	        TypedArray buttons = getResources().obtainTypedArray(R.array.buttons);
	        TypedArray progressBars = getResources().obtainTypedArray(R.array.progress_bars);
	        TypedArray sounds = getResources().obtainTypedArray(R.array.sounds);
	        
	        // load sounds and find progress bars
	        for (int i = 0; i < buttons.length(); ++i) {
	        	button = buttons.getResourceId(i, -1);
	        	sound = m_soundManager.loadSound(sounds.getResourceId(i, -1));
	        	
	        	m_buttonSound.put(button, sound);
	        	if (i < progressBars.length()) {
	        		progress = progressBars.getResourceId(i, -1);
	        		m_buttonProgress.put(button, progress);
	        		m_soundManager.setProgressBar(sound, findViewById(progress));
	        	}
	        }
	        
			return null;
		}
		
		protected void onPostExecute(Void param) {
			m_progressDialog.dismiss();
			m_progressDialog = null;
		}
	}
}
