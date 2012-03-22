package com.davidjennes.ElectroJam;


public class SoundToPlay {
	private int id;
	private boolean looped;
	
	public SoundToPlay(int i, boolean b) {
		id = i;
		looped =b;
	}
	
	public int returnSoundToPlayId() {
		return this.id;
	}
	
	public boolean returnSoundToPlayLooped() {
		return this.looped;
	}
}
