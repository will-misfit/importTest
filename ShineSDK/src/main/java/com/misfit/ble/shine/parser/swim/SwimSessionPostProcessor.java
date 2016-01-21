package com.misfit.ble.shine.parser.swim;

import android.util.Log;

import com.misfit.ble.algorithm.swimlap.LapStatVector;
import com.misfit.ble.algorithm.swimlap.SwimLapPostProcessor;
import com.misfit.ble.algorithm.swimlap.lap_stats_t;
import com.misfit.ble.shine.result.SwimLap;
import com.misfit.ble.shine.result.SwimSession;

import java.util.ArrayList;
import java.util.List;

public class SwimSessionPostProcessor {
	private static final String TAG = SwimSessionPostProcessor.class.getName();
	
    SwimSessionEntry mSessionEntryStart;
    ArrayList<SwimLapEntry> mSessionLapEntries = new ArrayList<SwimLapEntry>();
    
    public boolean hasCompleted() {
    	return (null == mSessionEntryStart);
    }
    
    private SwimSession processSwimSession(double sessionEndTime) {
    	if (null == mSessionEntryStart)
    		return null;
    	
		LapStatVector lapStatsVector = new LapStatVector();
		
		for (SwimLapEntry lapEntry : mSessionLapEntries) {
			lap_stats_t lapStats = new lap_stats_t();
			lapStats.setNStrokes(lapEntry.mStrokes);
			lapStats.setDuration_in_10th_seconds(lapEntry.mDurationInOneTenthSeconds);
			lapStats.setLap_end_in_10th_seconds(lapEntry.mEndTimeSinceSessionStartInOneTenthSeconds);
			lapStats.setSvm(lapEntry.mSvm);
			lapStatsVector.add(lapStats);
		}
		
		long numberOfLaps = SwimLapPostProcessor.refine_lap_count(lapStatsVector);
		
		SwimSession session = new SwimSession();
		session.mStartTime = mSessionEntryStart.mTimestamp;
		session.mEndTime = sessionEndTime;
		session.mNumberOfLaps = numberOfLaps;
		
		ArrayList<SwimLap> swimLaps = new ArrayList<SwimLap>();
		for (int i = 0; i < lapStatsVector.size(); ++i) {
			lap_stats_t lapStats = lapStatsVector.get(i);
			SwimLap swimLap = new SwimLap();
			swimLap.mStrokes = lapStats.getNStrokes();
			swimLap.mDuration = lapStats.getDuration_in_10th_seconds() * 1.0 / 10;
			swimLap.mEndTime = lapStats.getLap_end_in_10th_seconds() * 1.0 / 10 + session.mStartTime;
			swimLap.mSvm = lapStats.getSvm();
			swimLaps.add(swimLap);
		}
		session.mSwimLaps = swimLaps;
		
        return session;
    }

    private void onSessionStarted(SwimSessionEntry sessionStartedEntry) {
    	mSessionEntryStart = sessionStartedEntry;
    	mSessionLapEntries = new ArrayList<SwimLapEntry>();
    }
    
    private void onSessionFinished() {
		if (mSessionLapEntries != null) {
			mSessionLapEntries.clear();
		}
    	mSessionEntryStart = null;
    	mSessionLapEntries = null;
    }
    
    private SwimSession handleSessionEntry(SwimSessionEntry sessionEntry) {
    	SwimSession swimSession = null;
    	
    	switch (sessionEntry.mEntryType) {
    		case SwimSessionEndedByUser:
    		case SwimSessionEndedByFirmware:
			case SwimSessionEndedBySDK:
    			swimSession = processSwimSession(sessionEntry.mTimestamp);
    			onSessionFinished();
    			break;
    			
    		case SwimSessionStartedByUser:
    			onSessionStarted(sessionEntry);
    			break;
    			
    		case SwimSessionIgnore:
    			onSessionFinished();
    			break;
    			
    		default:
    			Log.e(TAG, "Unknown SwimSessionEntryType");
    			break;
    	}
    	
    	return swimSession;
    }
    
    private void handleSwimLapEntry(SwimLapEntry swimLapEntry) {
    	if (null == mSessionEntryStart)
    		return;
    	
    	mSessionLapEntries.add(swimLapEntry);
    }
    
    public ArrayList<SwimSession> processSwimEntries(List<Object> swimEntries, double fileEndTimestamp, boolean isLastFile) {
		if (isLastFile) {
			SwimSessionEntry entry = new SwimSessionEntry();
			entry.mTimestamp = fileEndTimestamp;
			entry.mEntryType = SwimSessionEntry.SwimSessionEntryType.SwimSessionEndedBySDK;

			ArrayList<Object> modifiedSwimEntries = new ArrayList<>(swimEntries);
			modifiedSwimEntries.add(entry);
			swimEntries = modifiedSwimEntries;
		}

		ArrayList<SwimSession> swimSessions = new ArrayList<SwimSession>();
    	
    	for (Object entry : swimEntries) {
    		if (SwimSessionEntry.class.equals(entry.getClass())) {
    			SwimSession swimSession = handleSessionEntry((SwimSessionEntry)entry);
    			if (null != swimSession) {
    				swimSessions.add(swimSession);
    			}
    		}
    		else if (SwimLapEntry.class.equals(entry.getClass())) {
    			handleSwimLapEntry((SwimLapEntry)entry);
    		}
    		else {
    			Log.e(TAG, "Invalid Swim Entry");
    		}
    	}
    	return swimSessions;
    }
}
