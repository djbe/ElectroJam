package com.davidjennes.ElectroJam.Client;

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
	 * @param samples A list of samples
	 * @return A list of IDs corresponding to these samples
	 */
	int [] loadSamples(in int [] samples);
	
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
}
