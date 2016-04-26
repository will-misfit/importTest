package com.misfit.ble.shine.result;

import android.annotation.SuppressLint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SyncResult {
	public ArrayList<Activity> mActivities;
	public ArrayList<TapEventSummary> mTapEventSummarys;
	public ArrayList<SessionEvent> mSessionEvents;
	public ArrayList<SwimSession> mSwimSessions;
	
	public SyncResult() {
		mActivities = new ArrayList<>();
		mTapEventSummarys = new ArrayList<>();
		mSessionEvents = new ArrayList<>();
		mSwimSessions = new ArrayList<>();
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	public String toString() {
		long startTimestamp = mActivities.size() > 0 ? mActivities.get(0).mStartTimestamp : 0;
		return String.format("nActivities = %d, nTapEventSummarys = %d, nSessionEvents = %d, nSwimSession = %d, startTimestamp = %d",
						mActivities.size(), mTapEventSummarys.size(), mSessionEvents.size(), mSwimSessions.size(), startTimestamp);
	}

	/**
	 *
	 * @return -1 if no activity
     */
	public long getHeadStartTime(){
		if(mActivities == null || mActivities.isEmpty()){
			return -1;
		}
		return mActivities.get(0).mStartTimestamp;
	}

	/**
	 *
	 * @return -1 if no activity
	 */
	public long getTailEndTime(){
		if(mActivities == null || mActivities.isEmpty()){
			return -1;
		}
		return mActivities.get(mActivities.size()-1).mEndTimestamp;
	}

	/**
	 * get total minutes of the per minute data collection
	 * */
	public long getTotalMinutes(){
		if(mActivities == null || mActivities.isEmpty()){
			return -1;
		}
		return mActivities.size();
	}

	public void correctSyncDataTimestamp(long delta) {
	    for (Activity activity : mActivities) {
	        activity.mStartTimestamp += delta;
	        activity.mEndTimestamp += delta;
	    }
	    
	    for (TapEventSummary tapEventsSummary : mTapEventSummarys) {
	        tapEventsSummary.mTimestamp += delta;
	    }
	    
	    for (SessionEvent event : mSessionEvents) {
	        event.mTimestamp += delta;
	    }
	    
	    for (SwimSession swimSession : mSwimSessions) {
	        for (SwimLap swimLap : swimSession.mSwimLaps) {
	            swimLap.mEndTime += delta;
	        }
	        swimSession.mStartTime += delta;
	        swimSession.mEndTime += delta;
	    }
	}

	public static SyncResult mergeSyncData(List<SyncResult> syncResults) {
		ArrayList<Activity> activities = new ArrayList<>();
		ArrayList<TapEventSummary> tapEventSummaries = new ArrayList<>();
		ArrayList<SessionEvent> sessionEvents = new ArrayList<>();
		ArrayList<SwimSession> swimSessions = new ArrayList<>();
	    
	    for (SyncResult syncResult : syncResults) {
	    	activities.addAll(syncResult.mActivities);
	    	tapEventSummaries.addAll(syncResult.mTapEventSummarys);
	    	sessionEvents.addAll(syncResult.mSessionEvents);
	    	swimSessions.addAll(syncResult.mSwimSessions);
	    }
	    
	    SyncResult mergedSyncData = new SyncResult();
	    mergedSyncData.mActivities = activities;
	    mergedSyncData.mTapEventSummarys = tapEventSummaries;
	    mergedSyncData.mSessionEvents = sessionEvents;
	    mergedSyncData.mSwimSessions = swimSessions;
	    return mergedSyncData;
	}
}
