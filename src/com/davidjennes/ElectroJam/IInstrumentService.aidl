package com.davidjennes.ElectroJam;

interface IInstrumentService {
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
	void sendEvent(String sample, int mode);
}
