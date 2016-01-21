package com.misfit.ble.shine.result;



public class TapEventSummary extends TapEvent {
	
	public int mCount;
	
	public TapEventSummary(long timestamp, int tapType, int count) {
		super(timestamp, tapType);
		mCount = count;
	}
	
	@Override
	public boolean equals(Object o) {
		if (super.equals(o) == false)
			return false;
		
		if (o instanceof TapEventSummary) {
			TapEventSummary other = (TapEventSummary)o;
            if (mTapType == other.mTapType && mCount == other.mCount) {
            	return true;
            }
        }
        return false;
	}
	
	@Override
	public int hashCode() {
		return (int) (mTimestamp + mTapType + mCount);
	}
}
