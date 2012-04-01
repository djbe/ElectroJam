package com.davidjennes.ElectroJam.Sound;

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
	public ScheduledSound() {
		m_sound = null;
		m_skipped = -1;
		m_skipLimit = 1;
		m_stop = false;
	}
	
	/**
	 * Set which sound is scheduled
	 * @param sound The affected sound
	 */
	public void setSound(Sound sound) {
		m_sound = sound;
		m_skipLimit = (sound != null) ? sound.getSkipLimit() : 1;
	}
	
	/**
	 * Set the skip limit
	 * @param limit The new skip limit value
	 */
	public void setSkipLimit(int limit) {
		m_skipLimit = limit;
	}
	
	/**
	 * Set whether to stop this sound on the next timer beats sync or not
	 * @param stop If true looping will stop, otherwise it will continue
	 */
	public void setStop(boolean stop) {
		m_stop = stop;
	}
	
	/**
	 * Check whether we've actually stopped
	 * @return True if stopped
	 */
	public boolean isStopped() {
		return m_stop && m_skipped == 0;
	}
	
	/**
	 * Get the progress (based on skip count)
	 * @return Returns a value between 0 and 100
	 */
	public int getProgress() {
		if (isStopped())
			return 0;
		else
			return 25 * ((m_skipped < m_skipLimit) ? m_skipped + 1 : m_skipLimit);
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
		
		// on sync with beat timer
		if (m_skipped == 0 && m_sound != null) {
			if (m_stop)
				m_sound.stop();
			else
				m_sound.play();
		}
	}
}
