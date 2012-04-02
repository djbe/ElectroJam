package com.davidjennes.ElectroJam.Server;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
	public static enum Code {LOAD, DELETE, START, STOP, DATA, UNTIL_NEXT_BEAT, DURATION}
	
	private static final long serialVersionUID = -5713869934297578231L;
	
	public Code code;
	public int id;
	public long extra;
	
	/**
	 * Constructor with no arguments
	 * @param code Message code
	 * @param id ID argument
	 */
	public NetworkMessage(Code code) {
		this.code = code;
		this.id = 0;
		this.extra = 0;
	}
	
	/**
	 * Constructor with only an ID
	 * @param code Message code
	 * @param id ID argument
	 */
	public NetworkMessage(Code code, int id) {
		this.code = code;
		this.id = id;
		this.extra = 0;
	}
	
	/**
	 * Constructor with an ID and extra integer argument
	 * @param code Message code
	 * @param id ID argument
	 * @param extra Extra integer argument
	 */
	public NetworkMessage(Code code, int id, long extra) {
		this.code = code;
		this.id = id;
		this.extra = extra;
	}
	
	/**
	 * Constructor with an ID and extra boolean argument
	 * @param code Message code
	 * @param id ID argument
	 * @param extra Extra boolean argument
	 */
	public NetworkMessage(Code code, int id, boolean extra) {
		this.code = code;
		this.id = id;
		this.extra = extra ? 1 : 0;
	}
	
	/**
	 * Get the extra argument as a boolean
	 * @return False if extra is 0, otherwise true
	 */
	public boolean getExtraBoolean() {
		return extra == 1;
	}
}
