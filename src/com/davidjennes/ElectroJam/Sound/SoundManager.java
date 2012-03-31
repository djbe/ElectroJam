package com.davidjennes.ElectroJam.Sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

public class SoundManager {
	private final static Random RANDOM = new Random();
	private final static int BEATS_LIMIT = 4;

	private Context m_context;
	private Map<Integer, Sound> m_sounds;
	private Timer m_timer = new Timer();
	private int m_beats;
	
	private Map<Integer, ScheduledSound> m_soundQueue;

	/**
	 * Constructor
	 * @param context The activity's context
	 */
	public SoundManager(Context context) {
		m_context = context;
		m_sounds = new HashMap<Integer, Sound>();
		m_soundQueue = new HashMap<Integer, ScheduledSound>();
		m_beats = -1;
		
		m_timer.scheduleAtFixedRate(new Action(), 0, Sound.SAMPLE_LENGTH);
	}
	
	protected void finalize() throws Throwable {
		try {
			m_timer.cancel();
			m_timer = null;
			
			for (Map.Entry<Integer, Sound> entry : m_sounds.entrySet())
				entry.getValue().stop();
			m_sounds.clear();
		} finally {
			super.finalize();
		}
	}

	/**
	 * Load a sound and prepare it for playback
	 * @param resid The resource ID to load from
	 * @return The sound's ID
	 */
	public int loadSound(int resid) {
		Sound s = new Sound(RANDOM.nextInt(), m_context, resid);
		m_sounds.put(s.id, s);

		return s.id;
	}

	/**
	 * Free up the resources used by a sound
	 * @param id The sound's ID
	 */
	public void unloadSound(int id) {
		stopSound(id);
		m_sounds.remove(id);
	}

	/**
	 * Play a sound, looping if need be
	 * @param id The sound's ID
	 * @param looped Will loop if true
	 */
	public void playSound(int id, boolean looped) {
		// looped sounds get scheduled to sync with timer beats
		if (looped)
			synchronized (m_soundQueue) {
				if (m_soundQueue.containsKey(id))
					m_soundQueue.get(id).setStop(false);
				else
					m_soundQueue.put(id, new ScheduledSound(m_sounds.get(id)));
			}
		// otherwise play directly
		else
			m_sounds.get(id).play();
	}

	/**
	 * Stop a sound which is playing, and prepare it for playback again
	 * @param id The sound's ID
	 */
	public void stopSound(int id) {
		synchronized (m_soundQueue) {
			// Stop looping if it is
			if (m_soundQueue.containsKey(id)) {
				m_soundQueue.get(id).setStop(true);
				return;
			}
			
		}
		
		// otherwise actually stop playing
		if (m_sounds.containsKey(id))
			m_sounds.get(id).stop();
	}
	
	/**
	 * Check if a sound is playing
	 * @param id The sound's ID
	 * @return True if playing
	 */
	public boolean isPlaying(int id) {
		return m_sounds.get(id).isPlaying();
	}
	
	/**
	 * Associate a progress bar with a sound
	 * @param id The sound's ID
	 * @param progressbar The ProgressBar to associate with
	 */
	public void setProgressBar(Integer id, View progressbar) {
		m_sounds.get(id).setProgressBar((ProgressBar) progressbar);
	}
	
	/**
	 * Beat timer task
	 */
	class Action extends TimerTask {
		public void run() {
			Map<Integer, ScheduledSound> soundQueue = null;
			m_beats = (m_beats + 1) % BEATS_LIMIT;
			
			// get queued sounds
			synchronized (m_soundQueue) {
				soundQueue = new HashMap<Integer, ScheduledSound>(m_soundQueue);
			}
			
			// play each scheduled sound
			for (Map.Entry<Integer, ScheduledSound> entry : soundQueue.entrySet())
				entry.getValue().skipBeat(m_beats);
			
			// remove stopped sounds
			synchronized (m_soundQueue) {
				for (Iterator<Map.Entry<Integer, ScheduledSound>> it = m_soundQueue.entrySet().iterator(); it.hasNext();) {
					Map.Entry<Integer, ScheduledSound> entry = it.next();
					
					if (entry.getValue().isStopped())
						it.remove();
				}
			}
		}
	}
}
