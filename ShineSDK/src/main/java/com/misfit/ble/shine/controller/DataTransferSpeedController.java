package com.misfit.ble.shine.controller;

import android.content.SharedPreferences;

import com.misfit.ble.sdk.GlobalVars;
import com.misfit.ble.shine.storage.Preferences;

public class DataTransferSpeedController {
	private static final float MAX_NUMBER_OF_PACKETS_PER_INTERVAL = 4f;
	private static final float MIN_NUMBER_OF_PACKETS_PER_INTERVAL = 1f;
	private static final float NUMBER_OF_PACKETS_UNIT = 0.25f;
	
	// Final data transfer speed for currentSDKVersion
	private static final String NUMBER_OF_PACKETS_PER_INTERVAL_UPPER_BOUND_KEY = "numberOfPacketsPerIntervalUpperBound" + "_" + GlobalVars.getSystemAPILevel() + "_" + GlobalVars.getSDKVersion();
	private static final String NUMBER_OF_PACKETS_PER_INTERVAL_LOWER_BOUND_KEY = "numberOfPacketsPerIntervalLowerBound" + "_" + GlobalVars.getSystemAPILevel() + "_" + GlobalVars.getSDKVersion();
	
	private static SharedPreferences getPreferences() {
		return Preferences.getSharedPreferences("com.misfitwearables.ble.shine.controller.DataTransferSpeedController");
	}
	
	private static float getNumberOfPacketsPerIntervalUpperBound() {
		return getPreferences().getFloat(NUMBER_OF_PACKETS_PER_INTERVAL_UPPER_BOUND_KEY, MAX_NUMBER_OF_PACKETS_PER_INTERVAL);
	}
	
	private static float getNumberOfPacketsPerIntervalLowerBound() {
		return getPreferences().getFloat(NUMBER_OF_PACKETS_PER_INTERVAL_LOWER_BOUND_KEY, MIN_NUMBER_OF_PACKETS_PER_INTERVAL);
	}
	
	private static void setNumberOfPacketsPerInterval(float upperBound, float lowerBound) {
		upperBound = Math.min(upperBound, MAX_NUMBER_OF_PACKETS_PER_INTERVAL);
		lowerBound = Math.max(lowerBound, MIN_NUMBER_OF_PACKETS_PER_INTERVAL);
		
		SharedPreferences.Editor preferencesEditor = getPreferences().edit();
		preferencesEditor.putFloat(NUMBER_OF_PACKETS_PER_INTERVAL_UPPER_BOUND_KEY, upperBound);
		preferencesEditor.putFloat(NUMBER_OF_PACKETS_PER_INTERVAL_LOWER_BOUND_KEY, lowerBound);
		preferencesEditor.commit();
	}
	
	private static float getNumberOfPacketsPerInterval() {
		float upperBound = getNumberOfPacketsPerIntervalUpperBound();
		float lowerBound = getNumberOfPacketsPerIntervalLowerBound();
		return Math.max((upperBound + lowerBound) / 2, MIN_NUMBER_OF_PACKETS_PER_INTERVAL);
	}
	
	public static float getInterpacketDelay(float connectionInterval) {
		return connectionInterval / getNumberOfPacketsPerInterval();
	}
	
	public static void onDataTransferFailedDueToTransferSpeed() {
		// lower upperBound to slow down data transfer speed
		float newUpperBound = getNumberOfPacketsPerInterval();
		float newLowerBound = getNumberOfPacketsPerIntervalLowerBound();
		if (newUpperBound - newLowerBound < 0.001f) {
			// the saturated speed is still too fast, release lowerBound to go even slower. 
			newLowerBound = MIN_NUMBER_OF_PACKETS_PER_INTERVAL;
		} else if (newUpperBound - newLowerBound < NUMBER_OF_PACKETS_UNIT) {
			newUpperBound = newLowerBound;
		}
		setNumberOfPacketsPerInterval(newUpperBound, newLowerBound);
	}
	
	public static void onDataTransferSucceeded() {
		// push lowerBound to speed up data transfer speed
		float newUpperBound = getNumberOfPacketsPerIntervalUpperBound();
		float newLowerBound = getNumberOfPacketsPerInterval();
		if (newUpperBound - newLowerBound < NUMBER_OF_PACKETS_UNIT) {
			return;
		}
		setNumberOfPacketsPerInterval(getNumberOfPacketsPerIntervalUpperBound(), getNumberOfPacketsPerInterval());
	}

}
