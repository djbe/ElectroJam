package com.davidjennes.ElectroJam;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {
	private SoundPool m_soundPool;
	private AudioManager m_audioManager;
	private Context m_context;
	
	public SoundManager(Context context) {
		m_context = context;
	    m_soundPool = new SoundPool(32, AudioManager.STREAM_MUSIC, 0);
	    m_audioManager = (AudioManager) m_context.getSystemService(Context.AUDIO_SERVICE);
	}

	public int addSound(int id) {
	    return m_soundPool.load(m_context, id, 1);
	}
	
	public int addSound(String file) {
		return m_soundPool.load(file, 1);
	}
	
	public void playSound(int id) {
		playSound(id, false);
	}
	
	public void playSound(int id, boolean looped) {
//		float streamVolume = m_audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//		streamVolume = streamVolume / m_audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	    m_soundPool.play(id, 0.5f, 0.5f, 1, (looped ? -1 : 0), 0f);
	}
	
	public void stopSound(int id) {
	    m_soundPool.stop(id);
	}
	
	public void pauseSound(int id) {
	    m_soundPool.pause(id);
	}
}
