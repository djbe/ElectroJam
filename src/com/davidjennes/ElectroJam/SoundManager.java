package com.davidjennes.ElectroJam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class SoundManager {
	private Context m_context;
	private Map<Integer, MediaPlayer> m_sounds;
	private final Random RANDOM = new Random();
	
	public SoundManager(Context context) {
		m_context = context;
	    m_sounds = new HashMap<Integer, MediaPlayer>();
	}

	public int addSound(int id) {
		int r = RANDOM.nextInt();
		m_sounds.put(r, MediaPlayer.create(m_context, id));
		Log.d("SoundManager", "MediaPlayer: " + r);
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
				m_sounds.get(id).prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
