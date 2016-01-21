package com.misfit.ble.shine.result;


public class SessionEvent extends Event {
	public static final int SESSION_EVENT_TYPE_START = 0;
	public static final int SESSION_EVENT_TYPE_END = 1;
	
	public int mType;
	
	public SessionEvent(long timestamp, int type) {
		super(timestamp);
		mType = type;
	}
}
