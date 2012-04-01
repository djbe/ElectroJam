package com.davidjennes.ElectroJam.Sound;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public abstract class SoundManager {
	public final static int UPDATE_PROGRESS = 1;
	public final static int UPDATE_SECONDARY = 2;
	protected final static Random RANDOM = new Random();
	private final static int BEATS_LIMIT = 4;
	
	protected Context m_context;
	protected ConcurrentMap<Integer, ScheduledSound> m_soundQueue;
	private Handler m_handler;
	private Timer m_timer;
	private int m_beats;
	
	/**
	 * Constructor
	 * @param context The activity's context
	 */
	public SoundManager(Context context, Handler handler) {
		m_context = context;
		m_handler = handler;
		m_beats = -1;
		m_timer = new Timer();
		m_soundQueue = new ConcurrentHashMap<Integer, ScheduledSound>();
		
		// start repeat timer
		m_timer.scheduleAtFixedRate(new Action(), 0, Sound.SAMPLE_LENGTH);
	}
	
	/**
	 * Destructor
	 */
	protected void finalize() throws Throwable {
		try {
			m_timer.cancel();
			m_timer = null;
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * Load a sound and prepare it for playback
	 * @param resid The resource ID to load from
	 * @return The sound's ID
	 */
	public abstract int loadSound(int resid);

	/**
	 * Free up the resources used by a sound
	 * @param id The sound's ID
	 */
	public abstract void unloadSound(int id);
	
	/**
	 * Called when a progress listener registered itself and we need to notify
	 * it with some basic data 
	 */
	public abstract void progressListenerRegistered();

	/**
	 * Play a sound, looping if need be
	 * @param id The sound's ID
	 * @param looped Will loop if true
	 */
	public void playSound(int id, boolean looped) {
		m_soundQueue.putIfAbsent(id, new ScheduledSound());
		
		ScheduledSound sound = m_soundQueue.get(id);
		if (sound != null)
			sound.setStop(false);
	}

	/**
	 * Stop a sound which is playing, and prepare it for playback again
	 * @param id The sound's ID
	 */
	public void stopSound(int id) {
		ScheduledSound sound = m_soundQueue.get(id);
		
		// Stop looping if it is
		if (sound != null)
			sound.setStop(true);
	}

	/**
	 * Check if a sound is playing
	 * @param id The sound's ID
	 * @return True if playing
	 */
	public abstract boolean isPlaying(int id);
	
	/**
	 * Send a progress update to clients
	 * @param what Update kind
	 * @param sound Which sound
	 * @param progress Progress value (between 0 and 100)
	 */
	protected void sendProgressMessage(int what, int sound, int progress) {
		Message msg = new Message();
		
		msg.what = what;
		msg.arg1 = sound;
		msg.arg2 = progress;
		
		m_handler.sendMessage(msg);
	}
	
	/**
	 * Beat timer task
	 */
	class Action extends TimerTask {
		public void run() {
			m_beats = (m_beats + 1) % BEATS_LIMIT;
			
			// play each scheduled sound
			for (Map.Entry<Integer, ScheduledSound> entry : m_soundQueue.entrySet()) {
				entry.getValue().skipBeat(m_beats);
				
				// send progress updates
				sendProgressMessage(UPDATE_PROGRESS, entry.getKey(), entry.getValue().getProgress());
			}
			
			// remove stopped sounds
			Iterator<Map.Entry<Integer, ScheduledSound>> it = m_soundQueue.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, ScheduledSound> entry = it.next();
				
				if (entry.getValue().isStopped())
					it.remove();
			}
		}
	}
}