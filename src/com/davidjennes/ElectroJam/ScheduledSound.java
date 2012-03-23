package com.davidjennes.ElectroJam;

/**
 * Used for scheduling sound on next timer beat
 */
class ScheduledSound {
	private int m_skipped;
	private Sound m_sound;
	
	/**
	 * Constructor
	 * @param aID
	 * @param aLooped
	 */
	public ScheduledSound(Sound sound) {
		m_sound = sound;
		m_skipped = -1;
	}
	
	/**
	 * Timer beat, update skip counter and play if we are in sync
	 * @param beats The number of timer beats (which we sync to)
	 */
	public void skipBeat(int beats) {
		// increase skip counter only when we're in sync with the beats counter
		// once in sync, the skip counter will increase with a modulo to the skip limit
		if (m_skipped > -1 || (m_skipped == -1 && (beats % m_sound.skipLimit) == 0))
			m_skipped = (m_skipped + 1) % m_sound.skipLimit;
		
		// update progress bar
		if (m_skipped > -1)
			m_sound.setProgress(m_skipped);
		
		// play on sync with beat timer
		if (m_skipped == 0)
			m_sound.play();
	}
}
