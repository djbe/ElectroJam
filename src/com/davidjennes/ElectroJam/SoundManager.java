package com.davidjennes.ElectroJam;

import java.util.HashMap;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {
	private SoundPool m_soundPool;
	private HashMap<Integer, Integer> m_soundMap;
	private AudioManager m_audioManager;
	private Context m_context;
	
	public void initSounds(Context theContext) {
	    m_context = theContext;
	    m_soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
	    m_soundMap = new HashMap<Integer, Integer>();
	    m_audioManager = (AudioManager) m_context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void addSound(int index, int SoundID) {
	    m_soundMap.put(index, m_soundPool.load(m_context, SoundID, 1));
	}
	
	public void playSound(int index) {
		playSound(index, false);
	}
	
	public void playSound(int index, boolean looped) {
		float streamVolume = m_audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume / m_audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	    m_soundPool.play(m_soundMap.get(index), streamVolume, streamVolume, 1, (looped ? -1 : 0), 1f);
	}
	
	public void stopSound(int index) {
	    m_soundPool.stop(m_soundMap.get(index));
	}
	
	public void pauseSound(int index) {
	    m_soundPool.pause(m_soundMap.get(index));
	}
}
