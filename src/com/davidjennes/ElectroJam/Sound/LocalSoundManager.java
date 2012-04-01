package com.davidjennes.ElectroJam.Sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

public class LocalSoundManager extends SoundManager {
	private final static int BEATS_LIMIT = 4;
	
	private Map<Integer, Sound> m_sounds;
	private Timer m_timer = new Timer();
	private int m_beats;
	
	private Map<Integer, ScheduledSound> m_soundQueue;

	/**
	 * Constructor
	 * @param context The activity's context
	 */
	public LocalSoundManager(Context context) {
		super(context);
		m_sounds = new HashMap<Integer, Sound>();
		m_soundQueue = new HashMap<Integer, ScheduledSound>();
		m_beats = -1;
		
		m_timer.scheduleAtFixedRate(new Action(), 0, Sound.SAMPLE_LENGTH);
	}
	
	/**
	 * Destructor
	 */
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
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#loadSound(int)
	 */
	public int loadSound(int resid) {
		Sound s = new Sound(RANDOM.nextInt(), m_context, resid);
		m_sounds.put(s.id, s);

		return s.id;
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#unloadSound(int)
	 */
	public void unloadSound(int id) {
		stopSound(id);
		m_sounds.remove(id);
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#playSound(int, boolean)
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
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#stopSound(int)
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
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#isPlaying(int)
	 */
	public boolean isPlaying(int id) {
		return m_sounds.get(id).isPlaying();
	}
	
	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#setProgressBar(java.lang.Integer, android.view.View)
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
