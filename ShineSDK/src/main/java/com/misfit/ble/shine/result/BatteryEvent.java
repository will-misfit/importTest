package com.misfit.ble.shine.result;


public class BatteryEvent extends Event{
	public int mBaseVoltage;
	public int mLoadVoltage;
	public short mTemperature;
	public short mBatteryLevel;
	
	public BatteryEvent(long timestamp, int baseVoltage, int loadVoltage, short temperature, short batteryLevel) {
		super(timestamp);
		mBaseVoltage = baseVoltage;
		mLoadVoltage = loadVoltage;
		mTemperature = temperature;
		mBatteryLevel = batteryLevel;
	}
}
