package com.misfit.ble.shine.result;


public class OrientationEvent extends Event{
	public int mCount;
	
	public OrientationEvent(long timestamp, int count) {
		super(timestamp);
		mCount = count;
	}
}
