package com.davidjennes.ElectroJam.Sound;

import java.util.Random;

import android.content.Context;
import android.view.View;

public abstract class SoundManager {
	protected final static Random RANDOM = new Random();
	protected Context m_context;
	
	/**
	 * Constructor
	 * @param context The activity's context
	 */
	public SoundManager(Context context) {
		m_context = context;
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
	 * Associate a progress bar with a sound
	 * @param id The sound's ID
	 * @param progressbar The ProgressBar to associate with
	 */
	public abstract void setProgressBar(Integer id, View progressbar);

}