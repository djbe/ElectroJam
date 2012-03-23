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
	public ProgressBar m_progressBar;
	public int id, skipLimit;
	
	/**
	 * Constructor
	 * @param context The activity's context
	 * @param resid The resource ID to load from
	 */
	public Sound(int newID, Context context, int resid) {
		m_mp = create(context, resid);
		
		id = newID;
		m_progressBar = null; 
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
			// reset to beginning
			m_mp.seekTo(0);
			
			// start again if needed
			if (!m_mp.isPlaying())
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
			if (m_mp.isPlaying())
				m_mp.pause();
			m_mp.seekTo(0);
			
			// reset progress bar
			if (m_progressBar != null)
				m_progressBar.setProgress(0);
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
	
	/**
	 * Connect a ProgressBar to this sound
	 * @param bar The new progress bar
	 */
	public void setProgressBar(ProgressBar bar) {
		m_progressBar = bar;
		m_progressBar.setSecondaryProgress(skipLimit * 25);
	}
	
	/**
	 * Show the specified progress on the progress bar associated with this sound
	 * @param progress The new progress to set to (in skips, from 0 to skip limit)
	 */
	public void setProgress(int progress) {
		progress = (progress < skipLimit) ? progress + 1 : skipLimit;
		m_progressBar.setProgress(progress * 25);
	}
	
	/**
	 * Create MediaPlayer, but don't wait for prepare to finish
	 * @param context The activity context
	 * @param resid The resource ID
	 * @return A MediaPlayer instance
	 */
	private MediaPlayer create(Context context, int resid) {
		try {
			AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
			if (afd == null)
				return null;

			MediaPlayer mp = new MediaPlayer();
			mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
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
