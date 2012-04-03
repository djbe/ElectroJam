package com.davidjennes.ElectroJam.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.davidjennes.ElectroJam.Server.NetworkMessage.Code;
import com.davidjennes.ElectroJam.Sound.LocalSoundManager;

public class ServerWorker extends Thread {
	private static final String TAG = ServerWorker.class.getName();
	private static final int BUF_SIZE = 8192;
	
	private Server m_server;
	private Socket m_socket;
	private ObjectInputStream m_reader;
	private ObjectOutputStream m_writer;
	private LocalSoundManager m_soundManager;
	private volatile boolean m_stop;
	
	private UUID m_id;
	private File m_cacheDir;
	private Map<Integer, Integer> m_soundMap;
	
	/**
	 * Constructor
	 * @param server The parent server thread
	 * @param socket The client socket
	 */
	public ServerWorker(Server server, Socket socket, Context context, LocalSoundManager manager) {
		super();
		
		m_server = server;
		m_socket = socket;
		m_stop = false;
		m_soundManager = manager;
		m_id = UUID.randomUUID();
		m_soundMap = new HashMap<Integer, Integer>();
		
		// get the cache directory
		m_cacheDir = context.getExternalCacheDir();
		if (m_cacheDir == null)
			m_cacheDir = context.getCacheDir();
		Log.i(TAG, "Cache directory: " + m_cacheDir);
	}

	/**
	 * Shut down this worker thread
	 */
	public void shutdown() {
		m_stop = true;
	}
	
	/**
	 * Worker thread.
	 * Accept sample data and play commands
	 */
	public void run() {
		try {
			Log.d("ServerWorker", "Client connected! port: " + m_socket.getLocalPort());
			m_writer = new ObjectOutputStream(m_socket.getOutputStream());
			m_reader = new ObjectInputStream(m_socket.getInputStream());
			
			// listen for events
			while (!m_stop) {
				NetworkMessage msg = (NetworkMessage) m_reader.readObject();
				if (msg != null)
					processMessage(msg);
			}
			
			// close connection
			m_writer.close();
			m_reader.close();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			m_writer = null;
			m_socket = null;
			
			// remove sounds 
			for (Map.Entry<Integer, Integer> entry : m_soundMap.entrySet()) {
				m_soundManager.unloadSound(entry.getValue());
				deleteFile(entry.getKey());
			}
			m_server.threadFinished(this);
		}
	}
	
	/**
	 * Process a received network message
	 * @param msg The message to be handled
	 */
	private void processMessage(NetworkMessage msg) {
		switch (msg.code) {
		case LOAD:
			Log.d(TAG, "Load sound: " + msg.id);
			try {
				Uri file = receiveFile(msg.id, msg.extra);
				int sound = m_soundManager.loadSound(file);
				m_soundMap.put(msg.id, sound);
				m_writer.writeObject(new NetworkMessage(Code.DURATION, msg.id, m_soundManager.getSoundDuration(sound)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case DELETE:
			Log.d(TAG, "Delete sound: " + msg.id);
			m_soundManager.unloadSound(m_soundMap.get(msg.id));
			m_soundMap.remove(msg.id);
			deleteFile(msg.id);
			break;
		case START:
			Log.d(TAG, "Start " + msg.id + ", looped: " + msg.getExtraBoolean());
			m_soundManager.playSound(m_soundMap.get(msg.id), msg.getExtraBoolean());
			break;
		case STOP:
			Log.d(TAG, "Stop " + msg.id);
			m_soundManager.stopSound(m_soundMap.get(msg.id));
			break;
		case UNTIL_NEXT_BEAT:
			try {
				m_writer.writeObject(new NetworkMessage(Code.UNTIL_NEXT_BEAT, 0, m_soundManager.timeUntilNextBeat()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
	/**
	 * Receive a file from client
	 * @param id The file ID (used for filename)
	 * @param size THe file size
	 */
	private Uri receiveFile(int id, long size) {
		try {
			byte[] buffer = new byte[BUF_SIZE];
			long done = 0;
			int read;
			
			// create storage file
			File file = new File(m_cacheDir, m_id.toString() + id + ".m4a");
			FileOutputStream fos = new FileOutputStream(file);
			
			// receive file from client
			while (done < size) {
				read = (int) (size - done > BUF_SIZE ? BUF_SIZE : size - done);
				read = m_reader.read(buffer, 0, read);
				
				if (read > -1) {
					fos.write(buffer, 0, read);
					done += read;
				}
			}
			fos.close();
			
			return Uri.parse("file://" + file.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Delete a sound from the cache
	 * @param id The sound's ID
	 */
	private void deleteFile(int id) {
		File file = new File(m_cacheDir, m_id.toString() + id + ".m4a");
		
		if (!file.delete())
			Log.i(TAG, "Could not delete cache file!");
	}
}
