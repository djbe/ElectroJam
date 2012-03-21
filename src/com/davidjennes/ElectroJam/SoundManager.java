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
	
	private Context m_context;
	private Map<Integer, MediaPlayer> m_sounds;
	private final Random RANDOM = new Random();
	
	public SoundManager(Context context) {
		m_context = context;
	    m_sounds = new HashMap<Integer, MediaPlayer>();
	}

	public int addSound(int id) {
		int r = RANDOM.nextInt();
		m_sounds.put(r, create(m_context, id));
		
		return r;
	}
	
	public void playSound(int id) {
		playSound(id, false);
	}
	
	public void playSound(int id, boolean looped) {
		MediaPlayer player = m_sounds.get(id);
		player.setLooping(looped);
		player.start();
	}
	
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
	
	// create MediaPlayer, but don't wait for prepare to finish
	public static MediaPlayer create(Context context, int resid) {
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
            if (afd == null) return null;

            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepareAsync();
            
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "create failed:", ex);
           // fall through
        } catch (SecurityException ex) {
            Log.d(TAG, "create failed:", ex);
            // fall through
        }
        
        return null;
    }
}
