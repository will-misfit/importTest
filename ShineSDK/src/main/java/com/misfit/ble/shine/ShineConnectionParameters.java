package com.misfit.ble.shine;

import com.misfit.ble.shine.core.Constants;

public class ShineConnectionParameters {
	private double mConnectionInterval;
	private int mConnectionLatency;
	private int mSupervisionTimeout;

	public ShineConnectionParameters(double connectionInterval, int connectionLatency, int supervisionTimeout) {
		mConnectionInterval = Math.max(connectionInterval, Constants.MINIMUM_CONNECTION_INTERVAL);
		mConnectionLatency = connectionLatency;
		mSupervisionTimeout = supervisionTimeout;
	}

	public double getConnectionInterval() {
		return mConnectionInterval;
	}
	
	public int getConnectionLatency() {
		return mConnectionLatency;
	}
	
	public int getSupervisionTimeout() {
		return mSupervisionTimeout;
	}
}
