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
	
	private MediaPlayer m_mp;
	public ProgressBar progressBar;
	public int id, skipLimit;
	
	/**
	 * Constructor
	 * @param context The activity's context
	 * @param resid The resource ID to load from
	 */
	public Sound(int newID, Context context, int resid) {
		m_mp = create(context, resid);
		
		id = newID;
		progressBar = null; 
		skipLimit = (int) m_mp.getDuration() / SAMPLE_LENGTH;
	}

	/**
	 * Destructor
	 */
	protected void finalize() throws Throwable {
		try {
			m_mp.stop();
			m_mp.release();
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * Start playing (with new media player, and prepare old one)
	 */
	public void play() {
		try {
			// start new one
			if (m_mp.isPlaying()) {
				m_mp.pause();
				m_mp.seekTo(0);
			}
			
			m_mp.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Stop current media player and prepare it again
	 */
	public void stop() {
		try {
			// stop player (and prepare it again)
			m_mp.pause();
			m_mp.seekTo(0);
			
			// reset progress bar
			if (progressBar != null)
				progressBar.setProgress(0);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check whether this sound is playing or not
	 * @return True if playing
	 */
	public boolean isPlaying() {
		return m_mp.isPlaying();
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
