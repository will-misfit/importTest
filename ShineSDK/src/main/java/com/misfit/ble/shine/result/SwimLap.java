package com.misfit.ble.shine.result;

import android.annotation.SuppressLint;

public class SwimLap {
	public long mStrokes;
	public double mDuration;
	public double mEndTime;
	public long mSvm;
	
	@SuppressLint("DefaultLocale")
	@Override
	public String toString() {
		return String.format("{stroke = %d, duration = %.3f, endTime = %.3f, svm = %d}", mStrokes, mDuration, mEndTime, mSvm);
	}
}
