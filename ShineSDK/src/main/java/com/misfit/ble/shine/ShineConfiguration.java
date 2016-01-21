package com.misfit.ble.shine;


public class ShineConfiguration {
	public static final byte CLOCK_STATE_DISABLE = 0;
	public static final byte CLOCK_STATE_ENABLE = 1;
	public static final byte CLOCK_STATE_SHOW_CLOCK_FIRST = 2;
	
	public static final byte TRIPLE_TAP_STATE_DISABLE = 0;
	public static final byte TRIPLE_TAP_STATE_ENABLE = 1;
	
	public static final byte ACTIVITY_TAGGING_STATE_TAGGED_OUT = 0;
	public static final byte ACTIVITY_TAGGING_STATE_TAGGED_IN = 1;
	
	public static final int DEFAULT_CLOCK_STATE = -1;
	public static final int DEFAULT_TRIPLE_TAP_STATE = -1;
	public static final int DEFAULT_ACTIVITY_TAGGING_STATE = -1;
	public static final long DEFAULT_ACTIVITY_POINT = -1;
	public static final long DEFAULT_GOAL_VALUE = -1;
	public static final short DEFAULT_BATTERY_LEVEL = 200;
	
	public byte mClockState = DEFAULT_CLOCK_STATE;
	public byte mTripleTapState = DEFAULT_TRIPLE_TAP_STATE;
	public byte mActivityTaggingState = DEFAULT_ACTIVITY_TAGGING_STATE;
	public long mActivityPoint = DEFAULT_ACTIVITY_POINT;
	public long mGoalValue = DEFAULT_GOAL_VALUE;
	public short mBatteryLevel = DEFAULT_BATTERY_LEVEL;
	
	public ShineConfiguration clone() {
		ShineConfiguration shineConfiguration = new ShineConfiguration();
		shineConfiguration.mClockState = mClockState;
		shineConfiguration.mTripleTapState = mTripleTapState;
		shineConfiguration.mActivityTaggingState = mActivityTaggingState;
		shineConfiguration.mActivityPoint = mActivityPoint;
		shineConfiguration.mGoalValue = mGoalValue;
		shineConfiguration.mBatteryLevel = mBatteryLevel;
		return shineConfiguration;
	}
}
