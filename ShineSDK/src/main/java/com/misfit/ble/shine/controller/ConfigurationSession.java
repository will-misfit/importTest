package com.misfit.ble.shine.controller;

import com.misfit.ble.shine.ShineConfiguration;


public abstract class ConfigurationSession {
	public long mTimestamp;
	public int mPartialSecond;
	public short mTimeZoneOffset;
	public ShineConfiguration mShineConfiguration;

	public ConfigurationSession() {
	}
}
