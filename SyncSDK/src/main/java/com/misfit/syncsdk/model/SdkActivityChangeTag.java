package com.misfit.syncsdk.model;

import com.misfit.cloud.algorithm.models.ActivityChangeTagShine;

/**
 * a data model to reflect ActivityChangeTagShine in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkActivityChangeTag {

	private long mTimestamp = 0;

	private int mMisfitActivityType = SdkActivityType.UNKNOWN_TYPE;

	public long getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(long timeStamp) {
		this.mTimestamp = timeStamp;
	}

	public int getMisfitActivityType() {
		return mMisfitActivityType;
	}

	public void setMisfitActivityType(int misfitActivityType) {
		this.mMisfitActivityType = misfitActivityType;
	}

	public ActivityChangeTagShine convert2ActivityChangeTagShine() {
		ActivityChangeTagShine activityChangeTagShine = new ActivityChangeTagShine();
		activityChangeTagShine.setTimestamp((int) mTimestamp);
		activityChangeTagShine.setType(SdkActivityType.convert2ActivityTypeShine(mMisfitActivityType));
		return activityChangeTagShine;
	}
}
