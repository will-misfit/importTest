package com.misfit.syncsdk.model;

import java.util.List;

/**
 * Model class to present sleep session
 */
public class SdkSleepSession {

	private int mTimestamp;

	private int mBookmarkTime;

    private int mRealStartTime;

	private int mRealEndTime;

	private int mEditedStartTime;

	private int mEditedEndTime;

    private boolean mIsAutoDetected;

	private int mNormalizedSleepQuality;

	private int mSleepSecs;

    private int mDeepSleepSecs;

	private int mSleepDuration;

	private int mAwakeDuration;

	private int mLightSleepDuration;

	private List<long[]> mSleepStateChanges;

    private int mRealAlarmTime;

	private int mAlarmTime;

	private boolean mIsDeleted;


	public void setTimestamp(int mTimestamp) {
		this.mTimestamp = mTimestamp;
	}

	public void setBookmarkTime(int mBookmarkTime) {
		this.mBookmarkTime = mBookmarkTime;
	}

	public void setRealStartTime(int mRealStartTime) {
		this.mRealStartTime = mRealStartTime;
	}

	public void setRealEndTime(int mRealEndTime) {
		this.mRealEndTime = mRealEndTime;
	}

	public void setEditedStartTime(int mEditedStartTime) {
		this.mEditedStartTime = mEditedStartTime;
	}

	public void setEditedEndTime(int mEditedEndTime) {
		this.mEditedEndTime = mEditedEndTime;
	}

	public void setIsAutoDetected(boolean mIsAutoDetected) {
		this.mIsAutoDetected = mIsAutoDetected;
	}

	public void setNormalizedSleepQuality(int mNormalizedSleepQuality) {
		this.mNormalizedSleepQuality = mNormalizedSleepQuality;
	}

	public void setSleepSecs(int mSleepSecs) {
		this.mSleepSecs = mSleepSecs;
	}

	public void setDeepSleepSecs(int mDeepSleepSecs) {
		this.mDeepSleepSecs = mDeepSleepSecs;
	}

	public void setSleepDuration(int mSleepDuration) {
		this.mSleepDuration = mSleepDuration;
	}

	public void setAwakeDuration(int mAwakeDuration) {
		this.mAwakeDuration = mAwakeDuration;
	}

	public void setLightSleepDuration(int mLightSleepDuration) {
		this.mLightSleepDuration = mLightSleepDuration;
	}

	public void setSleepStateChanges(List<long[]> mSleepStateChanges) {
		this.mSleepStateChanges = mSleepStateChanges;
	}

	public void setRealAlarmTime(int mRealAlarmTime) {
		this.mRealAlarmTime = mRealAlarmTime;
	}

	public void setAlarmTime(int mAlarmTime) {
		this.mAlarmTime = mAlarmTime;
	}

	public void setIsDeleted(boolean mIsDeleted) {
		this.mIsDeleted = mIsDeleted;
	}

	public int getTimestamp() {
		return mTimestamp;
	}

	public int getBookmarkTime() {
		return mBookmarkTime;
	}

	public int getRealStartTime() {
		return mRealStartTime;
	}

	public int getRealEndTime() {
		return mRealEndTime;
	}

	public int getEditedStartTime() {
		return mEditedStartTime;
	}

	public int getEditedEndTime() {
		return mEditedEndTime;
	}

	public boolean isAutoDetected() {
		return mIsAutoDetected;
	}

	public int getNormalizedSleepQuality() {
		return mNormalizedSleepQuality;
	}

	public int getSleepSecs() {
		return mSleepSecs;
	}

	public int getDeepSleepSecs() {
		return mDeepSleepSecs;
	}

	public int getSleepDuration() {
		return mSleepDuration;
	}

	public int getAwakeDuration() {
		return mAwakeDuration;
	}

	public int getLightSleepDuration() {
		return mLightSleepDuration;
	}

	public List<long[]> getSleepStateChanges() {
		return mSleepStateChanges;
	}

	public int getRealAlarmTime() {
		return mRealAlarmTime;
	}

	public int getAlarmTime() {
		return mAlarmTime;
	}

	public boolean isDeleted() {
		return mIsDeleted;
	}

}
