package com.davidjennes.ElectroJam;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Server implements Runnable {
	private final String TAG = "Server";
	private final String TYPE = "_eljam._tcp.local.";
	private final int PORT = 7654;
	
	private JmDNS m_jmdns;
	private ProgressDialog m_progress;
	private String m_name, m_description;
	private Context m_context;
	private final Set<ServerWorker> m_workers; 
	private volatile boolean m_running, m_stop;
	
	/**
	 * Constructor
	 * @param context The activity's context
	 */
	public Server(Context context) {
		super();
		
		m_context = context;
		m_workers = new CopyOnWriteArraySet<ServerWorker>();
		m_running = false;
		m_stop = true;
		initJmDNS();
	}
	
	/**
	 * Destructor
	 */
	protected void finalize() throws Throwable {
		try {
			m_jmdns.close();
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * Start running the server
	 * @param name The server's name
	 * @param description The server's description
	 */
	public void start(String name, String description) {
		if (m_running)
			return;
		
		// store info
		m_name = name;
		m_description = description;
		
		// progress dialog
		m_progress = ProgressDialog.show(m_context, m_context.getString(R.string.working), m_context.getString(R.string.starting_server), true, false);
		Thread thread = new Thread(this);
		thread.start();
	}
	
	/**
	 * Stop the server if it's running
	 */
	public void stop() {
		if (!m_running)
			return;
		
		// progress dialog
		m_progress = ProgressDialog.show(m_context, m_context.getString(R.string.working), m_context.getString(R.string.stopping_server), true, false);
		m_stop = true;
	}
	
	/**
	 * Server thread.
	 * Listen for incoming instrument connections and publish service on ZeroConf
	 */
	public void run() {
		try {
			m_stop = false;
			m_running = true;
			ServerSocket server = new ServerSocket(PORT);
			
			// --- startup sequence ---
			try {
				ServiceInfo info = ServiceInfo.create(TYPE, m_name, PORT, m_description);
				m_jmdns.registerService(info);
			} catch (IOException e) {
				Log.e(TAG, "Error while starting server.");
				e.printStackTrace();
			}
			m_progress.dismiss();
			m_progress = null;
			
			// --- main loop ---
			while (!m_stop) {
				Socket client = server.accept();
				ServerWorker worker = new ServerWorker(this, client);
				m_workers.add(worker);
				worker.start();
			}
			
			// --- shutdown sequence ---
			m_jmdns.unregisterAllServices();
			for (ServerWorker worker : m_workers)
				worker.shutdown();
			m_workers.clear();
			
			m_progress.dismiss();
			m_progress = null;
			m_running = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Called when a worker thread finished
	 * @param thread The worker thread
	 */
	public void threadFinished(Thread thread) {
		m_workers.remove(thread);
	}
	
	/**
	 * Create ZeroConf instance (off-loaded to another thread)
	 */
	private void initJmDNS() {
        new AsyncTask<Void, Void, Void>() {
    		protected Void doInBackground(Void... params) {
    			try {
    				m_jmdns = JmDNS.create();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			return null;
    		}
    	}.execute();
	}
}