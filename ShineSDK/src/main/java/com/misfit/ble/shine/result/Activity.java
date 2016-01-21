package com.misfit.ble.shine.result;

public class Activity {
	
	public static final int DEFAULT_VARIANCE = 10000;
	
	public long mStartTimestamp;
	public long mEndTimestamp;
	public int mPoints;
	public int mBipedalCount;
	public int mVariance; 
	
	public Activity(long startTimestamp, long endTimestamp, int bipedalCount, int points, int variance) {
		mStartTimestamp = startTimestamp;
		mEndTimestamp = endTimestamp;
		mBipedalCount = bipedalCount;
		mPoints = points;
		mVariance = variance;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Activity) {
			Activity other = (Activity)o;
            if (mStartTimestamp == other.mStartTimestamp 
            		&& mEndTimestamp == other.mEndTimestamp 
            		&& mPoints == other.mPoints 
            		&& mBipedalCount == other.mBipedalCount
            		&& mVariance == other.mVariance) {
            	return true;
            }
        }
        return false;
	}
	
	@Override
	public int hashCode() {
		return (int) (mStartTimestamp + mEndTimestamp + mPoints + mBipedalCount + mVariance);
	}
}
