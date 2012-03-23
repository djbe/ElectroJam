package com.davidjennes.ElectroJam;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.ProgressBar;

/**
 * Stores MediaPlayers connected to a single sound 
 */
class Sound {
	private final static String TAG = "Sound";
	public final static int SAMPLE_LENGTH = 1875;
	
	private MediaPlayer m_mp1, m_mp2;
	private volatile MediaPlayer m_current;
	
	public ProgressBar progressBar = null;
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
				if (m_current.isPlaying())
					m_current.pause();
				m_current.seekTo(0);
			}
			
			// switch players
			m_current = (m_current == m_mp1) ? m_mp2 : m_mp1;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		
		// reset progress bar
		progressBar.setProgress(0);
	}
	
	/**
	 * Check whether this sound is playing or not
	 * @return True if playing
	 */
	public boolean isPlaying() {
		return m_current != null && m_current.isPlaying();
	}
	
	public void setProgressBar(ProgressBar bar) {
		progressBar = bar;
		progressBar.setSecondaryProgress(skipLimit * 25);
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
