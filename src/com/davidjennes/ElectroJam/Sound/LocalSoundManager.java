package com.davidjennes.ElectroJam.Sound;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

public class LocalSoundManager extends SoundManager {
	private Map<Integer, Sound> m_sounds;

	/**
	 * Constructor
	 * @param context The activity's context
	 */
	public LocalSoundManager(Context context, Handler handler) {
		super(context, handler);
		
		m_sounds = new HashMap<Integer, Sound>();
	}
	
	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#stopManager()
	 */
	public void stopManager() {
		super.stopManager();
		
		for (Map.Entry<Integer, Sound> entry : m_sounds.entrySet())
			entry.getValue().stop();
		m_sounds.clear();
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#loadSound(int)
	 */
	public int loadSound(Uri uri) {
		int id = RANDOM.nextInt();
		m_sounds.put(id, new Sound(m_context, uri));

		return id;
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#unloadSound(int)
	 */
	public void unloadSound(int id) {
		stopSound(id);
		m_sounds.remove(id);
	}
	
	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#progressListenerRegistered()
	 */
	public void progressListenerRegistered() {
		for (Map.Entry<Integer, Sound> entry : m_sounds.entrySet())
			sendProgressMessage(UPDATE_SECONDARY, entry.getKey(), entry.getValue().getSkipLimit() * 25);
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#playSound(int, boolean)
	 */
	public void playSound(int id, boolean looped) {
		// looped sounds get scheduled to sync with timer beats
		if (looped) {
			ScheduledSound sound = scheduleSound(id);
			if (sound != null)
				sound.setSound(m_sounds.get(id));
			
		// otherwise play directly
		} else
			m_sounds.get(id).play();
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#stopSound(int)
	 */
	public void stopSound(int id) {
		unscheduleSound(id);
		
		// otherwise actually stop playing
		if (!m_soundQueue.containsKey(id) &&  m_sounds.containsKey(id))
			m_sounds.get(id).stop();
	}
	
	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#isPlaying(int)
	 */
	public boolean isPlaying(int id) {
		return m_sounds.get(id).isPlaying();
	}
	
	/**
	 * Get the duration of a certain sound
	 * @param id The sound's ID
	 * @return Duration in number of beats (skips)
	 */
	public int getSoundDuration(int id) {
		return m_sounds.get(id).getSkipLimit();
	}
}
