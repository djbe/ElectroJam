package com.davidjennes.ElectroJam.Sound;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.util.Log;

/**
 * Stores MediaPlayers connected to a single sound 
 */
class Sound implements OnErrorListener {
	private final static String TAG = Sound.class.getName();;
	public final static int SAMPLE_LENGTH = 1875;
	
	private MediaPlayer m_mp;
	private Context m_context;
	private Uri m_uri;
	private int m_skipLimit;
	
	/**
	 * Constructor
	 * @param context The activity's context
	 * @param uri The URI to the media file
	 */
	public Sound(Context context, Uri uri) {
		m_mp = create(context, uri);
		m_uri = uri;
		m_context = context;
		
		// calculate skip limit based on duration
		m_skipLimit = (int) m_mp.getDuration() / SAMPLE_LENGTH;
		if (m_skipLimit < 1)
			m_skipLimit = 1;
	}

	/**
	 * Destructor
	 */
	protected void finalize() throws Throwable {
		try {
			m_mp.stop();
			m_mp.release();
		} catch(IllegalStateException e) {
			e.printStackTrace();
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
	 * Get the skip limit (based on base sample length)
	 */
	public int getSkipLimit() {
		return m_skipLimit;
	}
	
	/**
	 * Create MediaPlayer, but don't wait for prepare to finish
	 * @param context The activity context
	 * @param resid The resource ID
	 * @return A MediaPlayer instance
	 */
	private MediaPlayer create(Context context, Uri resource) {
		try {
			MediaPlayer mp = new MediaPlayer();
			mp.setDataSource(context, resource);
			mp.setOnErrorListener(this);
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
	
	/**
	 * In case of error, reset the MediaPlayer so we can play again
	 * @param player Our failed MediaPlayer
	 * @param what The error code
	 * @param extra Extra code, implementation dependent
	 * @return True when error has been handled
	 */
	public boolean onError(MediaPlayer player, int what, int extra) {
		m_mp.release();
		m_mp = create(m_context, m_uri);
		
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Log.e(TAG, "MediaPlayer error: server died");
			break;
		default:
			Log.e(TAG, "MediaPlayer unknown error: " + what + " (" + extra + ")");
	    }
		
		return true;
	}
}
