package com.davidjennes.ElectroJam;

interface IInstrumentService {
	/**
	 * List of discovered servers
	 * @return List of server IDs (Integers)
	 */
	List availableServers();
	
	/**
	 * Get discovered server info
	 * @param id Server ID
	 * @return Server info (name & description)
	 */
	Map serverInfo(int id);
	
	/**
	 * Connect to a certain server
	 * @param id Server ID
	 */
	void connect(int id);
	
	/**
	 * Load samples for current instrument
	 * This WILL take a while, so make sure to use a Handler or aSyncTask
	 * @param samples A map from sample names to filenames
	 */
	void loadSamples(in Map samples);
	
	/**
	 * Send an instrument event
	 * @param sample The name of the sample
	 * @param mode The play mode (single, loop)
	 */
	void sendEvent(int sample, int mode);
}
