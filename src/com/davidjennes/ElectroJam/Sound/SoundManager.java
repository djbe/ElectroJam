package com.davidjennes.ElectroJam.Sound;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.content.Context;
import android.net.Uri;
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
	private long m_lastBeat;
	public int id = new Random().nextInt(100);
	
	/**
	 * Constructor
	 * @param context The activity's context
	 */
	public SoundManager(Context context, Handler handler) {
		m_context = context;
		m_handler = handler;
		m_beats = -1;
		m_lastBeat = 0;
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
			stopManager();
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * Stop this manager (and any of it's playing sounds)
	 */
	public void stopManager() {
		if (m_timer != null)
			m_timer.cancel();
		m_timer = null;
	}
	
	/**
	 * Load a sound and prepare it for playback
	 * @param uri The path to the media file
	 * @return The sound's ID
	 */
	public abstract int loadSound(Uri uri);

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
	public abstract void playSound(int id, boolean looped);

	/**
	 * Stop a sound which is playing, and prepare it for playback again
	 * @param id The sound's ID
	 */
	public abstract void stopSound(int id);

	/**
	 * Check if a sound is playing
	 * @param id The sound's ID
	 * @return True if playing
	 */
	public abstract boolean isPlaying(int id);
	
	/**
	 * Check when the next beat will happen
	 * @return Time in milliseconds
	 */
	public long timeUntilNextBeat() {
		long tick = m_lastBeat + Sound.SAMPLE_LENGTH * ((BEATS_LIMIT - m_beats) % BEATS_LIMIT);
		long now = System.currentTimeMillis();
		
		return (tick > now) ? tick - now : 0;
	}
	
	/**
	 * Schedule a sound to be looped
	 * @param id The sound's ID
	 */
	protected ScheduledSound scheduleSound(int id) {
		m_soundQueue.putIfAbsent(id, new ScheduledSound());
		
		ScheduledSound sound = m_soundQueue.get(id);
		if (sound != null)
			sound.setStop(false);
		
		return sound;
	}
	
	/**
	 * Remove a sound from loop schedule
	 * @param id The sound's ID
	 */
	protected void unscheduleSound(int id) {
		ScheduledSound sound = m_soundQueue.get(id);
		
		// Stop looping if it is
		if (sound != null)
			sound.setStop(true);
	}
	
	/**
	 * Restart the beat timer with a certain delay 
	 * @param delay Delay in milliseconds
	 */
	protected void restartTimer(long delay) {
		m_timer.cancel();
		m_beats = -1;
		
		m_timer = new Timer();
		m_timer.scheduleAtFixedRate(new Action(), delay, Sound.SAMPLE_LENGTH);
	}
	
	/**
	 * Send a progress update to clients
	 * @param what Update kind
	 * @param sound Which sound
	 * @param progress Progress value (between 0 and 100)
	 */
	protected void sendProgressMessage(int what, int sound, int progress) {
		if (m_handler == null)
			return;
		
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
			m_lastBeat = System.currentTimeMillis();
			
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