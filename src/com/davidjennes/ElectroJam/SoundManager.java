package com.davidjennes.ElectroJam;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
	
	private Queue<ScheduledSound> m_soundQueue;
	private Map<Integer, ScheduledSound> m_soundQueueMap;

	/**
	 * Constructor
	 * @param context The activity's context
	 */
	public SoundManager(Context context) {
		m_context = context;
		m_sounds = new HashMap<Integer, Sound>();
		m_soundQueue = new LinkedList<ScheduledSound>();
		m_soundQueueMap = new HashMap<Integer, ScheduledSound>();
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
				if (!m_soundQueueMap.containsKey(id)) {
					ScheduledSound schedule = new ScheduledSound(id, looped);
					m_soundQueue.add(schedule);
					m_soundQueueMap.put(id, schedule);
				}
			}
		
		// otherwise play directly (if not already playing)
		else {
			Sound sound = m_sounds.get(id);
			if (!sound.isPlaying())
				sound.play();
		}
	}

	/**
	 * Stop a sound which is playing, and prepare it for playback again
	 * @param id The sound's ID
	 */
	public void stopSound(int id) {
		synchronized (m_soundQueue) {
			if (m_sounds.containsKey(id))
				m_sounds.get(id).stop();
			
			if (m_soundQueueMap.containsKey(id)) {
				ScheduledSound schedule = m_soundQueueMap.get(id);
				
				m_soundQueue.remove(schedule);
				m_soundQueueMap.remove(id);
			}
		}
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
			List<ScheduledSound> soundQueue = null;
			m_beats = (m_beats + 1) % BEATS_LIMIT;
			
			// get queued sounds
			synchronized (m_soundQueue) {
				soundQueue = new LinkedList<ScheduledSound>(m_soundQueue);
			}
			
			// play each scheduled sound
			for (ScheduledSound schedule : soundQueue) {
				Sound sound = m_sounds.containsKey(schedule.id) ? m_sounds.get(schedule.id) : null;
				if (sound == null)
					continue;
				
				// keep track of skip count, connected to beats counter
				if (schedule.getSkipped() != -1 || (schedule.getSkipped() == -1 && (m_beats % sound.skipLimit) == 0))
					schedule.skip();
				
				// play only when skip limit is reached
				if (schedule.getSkipped() == 0)
					sound.play();
			}
		}
	}
	
	/**
	 * Used for scheduling sound until next timer beat
	 */
	class ScheduledSound {
		private int m_skipped;
		public int id;
		public boolean looped;
		
		public ScheduledSound(int i, boolean b) {
			id = i;
			looped = b;
			m_skipped = -1;
		}
		
		/**
		 * Check how many times we've been skipped
		 * @return The number of skip-times
		 */
		public int getSkipped() {
			return m_skipped;
		}
		
		/**
		 * Update the skip count
		 * @param limit Limit the skip count, past which it'll be reset to 0
		 */
		public void skip() {
			Sound sound = m_sounds.get(id);
			
			// update skip counter
			m_skipped = (m_skipped + 1) % sound.skipLimit;
			
			// update progress bar
			sound.progressBar.setProgress((m_skipped + 1) * 25);
		}
	}
}
