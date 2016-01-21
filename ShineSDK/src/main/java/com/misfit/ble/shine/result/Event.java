package com.misfit.ble.shine.result;

public abstract class Event {
	public long mTimestamp;
	
	protected Event(long timestamp) {
		mTimestamp = timestamp;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Event) {
			Event other = (Event)o;
			if (mTimestamp == other.mTimestamp) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (int) mTimestamp;
	}
}
