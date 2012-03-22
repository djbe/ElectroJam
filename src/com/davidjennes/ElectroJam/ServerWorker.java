package com.davidjennes.ElectroJam;

import java.net.Socket;

import android.util.Log;

public class ServerWorker implements Runnable {
	Socket m_socket;
	
	public ServerWorker(Socket socket) {
		super();
		
		m_socket = socket;
	}
	
	public void run() {
		Log.d("ServerWorker", "Client connected!");
	}
}
