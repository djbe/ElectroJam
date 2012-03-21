package com.davidjennes.ElectroJam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

public class SoundManager {
	private final static String TAG = "SoundManager";
	private final static Random RANDOM = new Random();
	
	private Context m_context;
	private Map<Integer, MediaPlayer> m_sounds;
	
	/**
	 * Constructor
	 * @param context The activity's context
	 */
	public SoundManager(Context context) {
		m_context = context;
	    m_sounds = new HashMap<Integer, MediaPlayer>();
	}
	
	/**
	 * Load a sound and prepare it for playback
	 * @param resid The resource ID to load from
	 * @return The sound's ID
	 */
	public int loadSound(int resid) {
		int r = RANDOM.nextInt();
		m_sounds.put(r, create(m_context, resid));
		
		return r;
	}
	
	/**
	 * Free up the resources used by a sound
	 * @param id The sound's ID
	 */
	public void unloadSound(int id) {
		MediaPlayer player = m_sounds.get(id);
		
		player.stop();
		player.release();
		m_sounds.remove(id);
	}
	
	/**
	 * Play a sound, looping if need be
	 * @param id The sound's ID
	 * @param looped Will loop if true
	 */
	public void playSound(int id, boolean looped) {
		MediaPlayer player = m_sounds.get(id);
		
		player.setLooping(looped);
		player.start();
	}
	
	/**
	 * Stop a sound which is playing, and prepare it for playback again
	 * @param id The sound's ID
	 */
	public void stopSound(int id) {
		if (m_sounds.containsKey(id)) {			
			m_sounds.get(id).stop();
			try {
				m_sounds.get(id).prepareAsync();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Create MediaPlayer, but don't wait for prepare to finish 
	 * @param context The activity context
	 * @param resid The resource ID
	 * @return A MediaPlayer instance
	 */
	public static MediaPlayer create(Context context, int resid) {
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
            if (afd == null)
            	return null;

            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepareAsync();
            
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
