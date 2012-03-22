package com.davidjennes.ElectroJam;

import java.net.Socket;

import android.util.Log;

public class ServerWorker extends Thread {
	private Server m_server;
	private Socket m_socket;
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
			Log.d("ServerWorker", "Client connected!");
			while (!m_stop)
				Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			m_server.threadFinished(this);
		}
	}
}
