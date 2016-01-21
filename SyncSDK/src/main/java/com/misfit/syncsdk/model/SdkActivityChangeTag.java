package com.misfit.syncsdk.model;

import com.misfit.cloud.algorithm.models.ActivityChangeTagShine;

/**
 * a data model to reflect ActivityChangeTagShine in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkActivityChangeTag {

	private long mTimeStamp = 0;

	private int mMisfitActivityType = SdkActivityType.UNKNOWN_TYPE;

	public long getTimeStamp() {
		return mTimeStamp;
	}

	public void setmTimeStamp(long mTimeStamp) {
		this.mTimeStamp = mTimeStamp;
	}

	public int getMisfitActivityType() {
		return mMisfitActivityType;
	}

	public void setmMisfitActivityType(int mMisfitActivityType) {
		this.mMisfitActivityType = mMisfitActivityType;
	}

	public ActivityChangeTagShine convert2ActivityChangeTagShine() {
		ActivityChangeTagShine activityChangeTagShine = new ActivityChangeTagShine();
		activityChangeTagShine.setTimestamp((int) mTimeStamp);
		activityChangeTagShine.setType(SdkActivityType.convert2ActivityTypeShine(mMisfitActivityType));
		return activityChangeTagShine;
	}
}
