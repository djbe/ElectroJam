package com.davidjennes.ElectroJam.Client;

oneway interface IInstrumentServiceCallback {
	/**
	 * Called when a sound reaches a certain progress while playing
	 * @param sound The sound's ID
	 * @param progress The new progress value
	 */
	void updateProgress(int sound, int progress);
	
	/**
	 * Called to send secondary progress information
	 * @param sound The sound's ID
	 * @param secondaryProgress The new secondary progress value (progress will go from 0 to this value)
	 */
	void secondaryProgress(int sound, int secondaryProgress);
}
