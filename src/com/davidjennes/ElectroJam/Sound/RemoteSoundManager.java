package com.davidjennes.ElectroJam.Sound;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.davidjennes.ElectroJam.Server.NetworkMessage;
import com.davidjennes.ElectroJam.Server.NetworkMessage.Code;

public class RemoteSoundManager extends SoundManager {
	private static final String TAG = RemoteSoundManager.class.getName();
	private static final int BUF_SIZE = 8192;
	
	private Socket m_socket;
	private ObjectInputStream m_reader;
	private ObjectOutputStream m_writer;
	
	private long m_delay;
	private Map<Integer, Boolean> m_soundStatus;
	private Map<Integer, Integer> m_soundDuration;
	
	/**
	 * Constructor
	 * @param context The activity's context
	 * @param socket The server's socket
	 * @throws IOException 
	 */
	public RemoteSoundManager(Context context, Handler handler, Socket socket) throws IOException {
		super(context, handler);
		
		m_socket = socket;
		m_writer = new ObjectOutputStream(m_socket.getOutputStream());
		m_reader = new ObjectInputStream(m_socket.getInputStream());
		m_soundStatus = new HashMap<Integer, Boolean>();
		m_soundDuration = new HashMap<Integer, Integer>();
		m_delay = 0;
		
		// sync timers
		syncTimers();
	}
	
	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#stopManager()
	 */
	public void stopManager() {
		super.stopManager();
		
		if (m_socket != null)
			try {
				m_writer.close();
				m_reader.close();
				m_socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				m_writer = null;
				m_reader = null;
				m_socket = null;
			}
	}
	
	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#loadSound(int)
	 */
	public int loadSound(Uri uri) {
		byte[] buffer = new byte[BUF_SIZE];
		int bytes, id = RANDOM.nextInt();
		
		try {
			Log.d(TAG, "Load sample: " + uri);
			
			// notify server we're uploading a sample
			AssetFileDescriptor afd = m_context.getContentResolver().openAssetFileDescriptor(uri, "r");
			m_writer.writeObject(new NetworkMessage(NetworkMessage.Code.LOAD, id, afd.getLength()));
			
			// send sample file
			FileInputStream fis = afd.createInputStream();
			do {
				bytes = fis.read(buffer);
				if (bytes > 0)
					m_writer.write(buffer, 0, bytes);
			} while (bytes > 0);
			m_writer.flush();
			
			// get duration
			NetworkMessage msg = (NetworkMessage) m_reader.readObject();
			m_soundDuration.put(id, (int) msg.extra);
			m_soundStatus.put(id, false);
		} catch (IOException e) {
			e.printStackTrace();
			id = 0;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return id;
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#unloadSound(int)
	 */
	public void unloadSound(int id) {
		try {
			Log.d(TAG, "Delete sample: " + id);
			m_writer.writeObject(new NetworkMessage(NetworkMessage.Code.DELETE, id));
			m_soundStatus.remove(id);
			m_soundDuration.remove(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#progressListenerRegistered()
	 */
	public void progressListenerRegistered() {
		for (Map.Entry<Integer, Integer> entry : m_soundDuration.entrySet())
			sendProgressMessage(UPDATE_SECONDARY, entry.getKey(), entry.getValue() * 25);
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#playSound(int, boolean)
	 */
	public void playSound(final int id, boolean looped) {
		if (!m_soundStatus.containsKey(id))
			return;
		
		try {
			// schedule (delayed)
			if (looped) {
				ScheduledSound sound = scheduleSound(id);
				if (sound != null)
					sound.setSkipLimit(m_soundDuration.get(id));
			}
			
			// send to server
			m_writer.writeObject(new NetworkMessage(NetworkMessage.Code.START, id, looped));
			if (looped)
				m_soundStatus.put(id, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#stopSound(int)
	 */
	public void stopSound(final int id) {
		if (!m_soundStatus.containsKey(id))
			return;
		
		try {
			unscheduleSound(id);
			
			// send to server
			m_writer.writeObject(new NetworkMessage(NetworkMessage.Code.STOP, id));
			m_soundStatus.put(id, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see com.davidjennes.ElectroJam.Sound.SoundManager#isPlaying(int)
	 */
	public boolean isPlaying(int id) {
		if (m_soundStatus.containsKey(id))
			return m_soundStatus.get(id);
		else
			return false;
	}
	
	/**
	 * Sync our timer with server's timer
	 */
	private void syncTimers() {
		NetworkMessage msg = null;
		long end = 0, start = 0;
		
		// query the server when the next beat is
		// and also measure round trip time
		start = System.currentTimeMillis();
		try {
			m_writer.writeObject(new NetworkMessage(Code.UNTIL_NEXT_BEAT));
			msg = (NetworkMessage) m_reader.readObject();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		end = System.currentTimeMillis();
		
		// restart the timer beat task with a certain delay
		m_delay = (end - start) / 2;
		restartTimer((msg.extra - m_delay > 0) ? msg.extra - m_delay : 0);
		Log.d(TAG, "Delay: " + m_delay);
	}
}
