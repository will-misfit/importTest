package com.misfit.ble.shine.result;

import android.annotation.SuppressLint;

import java.util.List;

public class SwimSession {
	public double mStartTime;
	public double mEndTime;
	public long mNumberOfLaps;
	public List<SwimLap> mSwimLaps;
	
	@SuppressLint("DefaultLocale")
	@Override
	public String toString() {
		return String.format("startTime = %.3f, endTime = %.3f, numberOfLaps = %d, swimLaps = " + mSwimLaps, mStartTime, mEndTime, mNumberOfLaps);
	}
}
