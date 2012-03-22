package com.davidjennes.ElectroJam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import android.util.Log;

public class ServerWorker extends Thread {
	private static final String TAG = "ServerWorker";
	
	private Server m_server;
	private Socket m_socket;
	private BufferedReader m_reader;
	private volatile boolean m_stop;
	
	/**
	 * Constructor
	 * @param server The parent server thread
	 * @param socket The client socket
	 */
	public ServerWorker(Server server, Socket socket) {
		super();
		
		m_server = server;
		m_socket = socket;
		m_stop = false;
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
			m_reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
			
			while (!m_stop) {
				String line = m_reader.readLine();
				Log.d(TAG, "Received: " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			m_server.threadFinished(this);
		}
	}
}
