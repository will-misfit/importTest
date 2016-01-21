package com.misfit.ble.shine.result;


public class TapEvent extends Event {
	
	public static final int TAP_TYPE_SINGLE = 0;
	public static final int TAP_TYPE_DOUBLE = 1;
	public static final int TAP_TYPE_TRIPLE = 2;
	public static final int TAP_TYPE_QUADRUPLE_BEGIN = 0x18;
	public static final int TAP_TYPE_QUADRUPLE_END = 0x19;

	public int mTapType;
	
	public TapEvent(long timestamp, int tapType) {
		super(timestamp);
		mTapType = tapType;
	}
	
	@Override
	public boolean equals(Object o) {
		if (super.equals(o) == false)
			return false;
		
		if (o instanceof TapEvent) {
			TapEvent other = (TapEvent)o;
            if (mTapType == other.mTapType) {
            	return true;
            }
        }
        return false;
	}
	
	@Override
	public int hashCode() {
		return (int) (mTimestamp + mTapType);
	}
}
