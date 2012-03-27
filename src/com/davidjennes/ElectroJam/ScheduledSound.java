package com.davidjennes.ElectroJam;

/**
 * Used for scheduling sound on next timer beat
 */
class ScheduledSound {
	private int m_skipped, m_skipLimit;
	private boolean m_stop;
	private Sound m_sound;
	
	/**
	 * Constructor
	 * @param sound The sound to be played
	 */
	public ScheduledSound(Sound sound) {
		m_sound = sound;
		m_skipped = -1;
		m_skipLimit = m_sound.skipLimit;
		m_stop = false;
	}
	
	/**
	 * Set whether to stop this sound on the next timer beats sync or not
	 */
	public void setStop(boolean stop) {
		m_stop = stop;
	}
	
	/**
	 * Check whether we've actually stopped
	 */
	public boolean isStopped() {
		return m_stop && m_skipped == 0;
	}
	
	/**
	 * Timer beat, update skip counter and play if we are in sync
	 * @param beats The number of timer beats (which we sync to)
	 */
	public void skipBeat(int beats) {
		// increase skip counter only when we're in sync with the beats counter
		// once in sync, the skip counter will increase with a modulo to the skip limit
		if (m_skipped > -1 || (m_skipped == -1 && (beats % m_skipLimit) == 0))
			m_skipped = (m_skipped + 1) % m_skipLimit;
		
		// update progress bar
		if (m_skipped > -1)
			m_sound.setProgress(m_skipped);
		
		// on sync with beat timer
		if (m_skipped == 0) {
			if (m_stop)
				m_sound.stop();
			else
				m_sound.play();
		}
	}
}
