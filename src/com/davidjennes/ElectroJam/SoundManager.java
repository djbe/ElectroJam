package com.davidjennes.ElectroJam;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

public class SoundManager {
	private final static String TAG = "SoundManager";
	private final static Random RANDOM = new Random();
	private final static int SAMPLE_LENGTH = 1875;

	private Context m_context;
	private Map<Integer, Sound> m_sounds;
	private Timer m_timer = new Timer();
	
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
		
		m_timer.scheduleAtFixedRate(new Action(), 0, SAMPLE_LENGTH);
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
				sound.stop();
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
	 * Beat timer task
	 */
	class Action extends TimerTask {
		public void run() {
			List<ScheduledSound> soundQueue = null;
			
			// get queued sounds
			synchronized (m_soundQueue) {
				soundQueue = new LinkedList<ScheduledSound>(m_soundQueue);
			}
			
			// play each scheduled sound
			for (ScheduledSound schedule : soundQueue) {
				Sound sound = m_sounds.containsKey(schedule.id) ? m_sounds.get(schedule.id) : null;
				
				// keep track of skip count and play only when skip limit is reached
				if (sound != null) {
					schedule.skipped = (schedule.skipped + 1) % sound.skipLimit;
					
					if (schedule.skipped == 0)
						sound.play();
				}
			}
		}
	}
	
	/**
	 * Stores MediaPlayers connected to a single sound 
	 */
	class Sound {
		private MediaPlayer m_mp1, m_mp2;
		private volatile MediaPlayer m_current;
		public int id, skipLimit;
		
		/**
		 * Constructor
		 * @param context The activity's context
		 * @param resid The resource ID to load from
		 */
		public Sound(int id, Context context, int resid) {
			m_mp1 = create(context, resid);
			m_mp2 = create(context, resid);
			m_current = null;
			this.id = id;
			
			skipLimit = (int) m_mp1.getDuration() / SAMPLE_LENGTH;
			Log.d(TAG, skipLimit + " - " + m_mp1.getDuration());
		}

		/**
		 * Destructor
		 */
		protected void finalize() throws Throwable {
			try {
				m_mp1.stop();
				m_mp2.stop();
				m_mp1.release();
				m_mp2.release();
			} finally {
				super.finalize();
			}
		}
		
		/**
		 * Start playing (with new media player, and prepare old one)
		 */
		public void play() {
			try {
				// stop old one
				if (m_current != null) {
					m_current.stop();
					m_current.prepareAsync();
				}
				
				// switch players
				m_current = (m_current == m_mp1) ? m_mp2 : m_mp1;

				// start new one
				m_current.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Stop current media player and prepare it again
		 */
		public void stop() {
			try {
				// stop current (and prepare it again)
				if (m_current != null) {
					m_current.stop();
					m_current.prepareAsync();
				}
				
				// switch players
				m_current = (m_current == m_mp1) ? m_mp2 : m_mp1;
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Check whether this sound is playing or not
		 * @return True if playing
		 */
		public boolean isPlaying() {
			return m_current != null && m_current.isPlaying();
		}
		
		/**
		 * Create MediaPlayer, but don't wait for prepare to finish
		 * @param context The activity context
		 * @param resid The resource ID
		 * @return A MediaPlayer instance
		 */
		private MediaPlayer create(Context context, int resid) {
			try {
				AssetFileDescriptor afd = context.getResources().openRawResourceFd(
						resid);
				if (afd == null)
					return null;

				MediaPlayer mp = new MediaPlayer();
				mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
						afd.getLength());
				afd.close();
				mp.prepare();

				return mp;
			} catch (IOException ex) {
				Log.d(TAG, "create failed:", ex);
			} catch (IllegalArgumentException ex) {
				Log.d(TAG, "create failed:", ex);
			} catch (SecurityException ex) {
				Log.d(TAG, "create failed:", ex);
			}

			return null;
		}
	}
	
	/**
	 * Used for scheduling sound until next timer beat
	 */
	class ScheduledSound {
		public int id, skipped;
		public boolean looped;
		
		public ScheduledSound(int i, boolean b) {
			id = i;
			looped = b;
			skipped = -1;
		}
	}
}
