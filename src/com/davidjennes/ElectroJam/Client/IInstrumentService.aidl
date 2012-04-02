package com.davidjennes.ElectroJam.Client;

import com.davidjennes.ElectroJam.Client.IInstrumentServiceCallback;

interface IInstrumentService {
	/**
	 * List of discovered servers
	 * @return List of server IDs
	 */
	int[] availableServers();
	
	/**
	 * Get discovered server info
	 * @param id Server ID
	 * @return Server info (array of 2 elements, name and description)
	 */
	String[] serverInfo(int id);
	
	/**
	 * Connect to a certain server
	 * @param id Server ID
	 */
	void connect(int id);
	
	/**
	 * Disconnect from server
	 */
	void disconnect();
	
	/**
	 * Check whether we're connected to a server
	 */
	boolean isConnected();
	
	/**
	 * Load samples for current instrument
	 * This WILL take a while, so make sure to use a Handler or aSyncTask
	 * @param samples A list of sample paths
	 * @return A list of IDs
	 */
	int [] loadSamples(in String [] samples);
	
	/**
	 * Unload the specified sounds
	 * @param IDs A list of IDs to unload 
	 */
	void unloadSamples(in int [] IDs);
	
	/**
	 * Send an instrument event
	 * @param sample The ID of the sample
	 * @param looped Whether to play the sample in a looped fashion
	 */
	void playSound(int sample, boolean looped);
	
	/**
	 * Send an instrument event
	 * @param sample The ID of the sample
	 */
	void stopSound(int sample);
	
	/**
	 * Check if a sound is playing
	 * @return True if playing
	 */
	boolean isPlaying(int sample);
	
	/**
	 * Register a callback to receive progress notifications
	 * @param callback A callback instance
	 */
	void registerCallback(IInstrumentServiceCallback callback);
	
	/**
	 * Remove a callback to stop receiving notifications
	 * @param callback A callback instance
	 */
	void unregisterCallback(IInstrumentServiceCallback callback);
}
