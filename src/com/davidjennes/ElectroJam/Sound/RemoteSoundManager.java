package com.davidjennes.ElectroJam.Sound;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.view.View;

public class RemoteSoundManager extends SoundManager {
	private static final String TAG = SoundManager.class.getName();
	
	private Socket m_socket;
	private PrintWriter m_writer;
	private Map<Integer, Boolean> m_soundStatus;
	
	/**
	 * Constructor
	 * @param context The activity's context
	 * @param socket The server's socket
	 * @throws IOException 
	 */
	public RemoteSoundManager(Context context, Socket socket) throws IOException {
		super(context);
		
		m_socket = socket;
		m_writer = new PrintWriter(m_socket.getOutputStream(), true);
		m_soundStatus = new HashMap<Integer, Boolean>();
	}
	
	/**
	 * Destructor
	 */
	protected void finalize() throws Throwable {
		try {
			m_writer.close();
			m_socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			m_writer = null;
			m_socket = null;
		}
	}
	
	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#loadSound(int)
	 */
	public int loadSound(int resid) {
		AssetFileDescriptor afd = m_context.getResources().openRawResourceFd(resid);
		byte[] buffer = new byte[8192];
		int bytes, id = RANDOM.nextInt();
		
		// notify server we're uploading a sample
		Log.d(TAG, "Load sample: " + id);
		m_writer.println("LOAD:" + id + ":" + afd.getLength());
		
		// send sample file
		try {
			FileInputStream fis = afd.createInputStream();
			BufferedOutputStream bos = new BufferedOutputStream(m_socket.getOutputStream());
			
			// read and send data
			do {
				bytes = fis.read(buffer);
				if (bytes > 0)
					bos.write(buffer);
			} while (bytes > 0);
			
			// store status
			m_soundStatus.put(id, false);
		} catch (IOException e) {
			e.printStackTrace();
			id = 0;
		}
		
		return id;
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#unloadSound(int)
	 */
	public void unloadSound(int id) {
		Log.d(TAG, "Delete sample: " + id);
		m_writer.println("DEL:" + id);
		m_soundStatus.remove(id);
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#playSound(int, boolean)
	 */
	public void playSound(int id, boolean looped) {
		Log.d(TAG, "Play sample: " + id + " looped: " + looped);
		m_writer.println("START:" + (looped ? 1 : 2) + id);
		m_soundStatus.put(id, true);
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#stopSound(int)
	 */
	public void stopSound(int id) {
		Log.d(TAG, "Stop sample: " + id + " stop!");
		m_writer.println("STOP:" + id);
		m_soundStatus.put(id, false);
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#isPlaying(int)
	 */
	public boolean isPlaying(int id) {
		return m_soundStatus.get(id);
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#setProgressBar(java.lang.Integer, android.view.View)
	 */
	public void setProgressBar(Integer id, View progressbar) {
		// TODO: connect id and progress bar
	}
}
