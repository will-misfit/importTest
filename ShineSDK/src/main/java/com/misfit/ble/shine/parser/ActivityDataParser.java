package com.misfit.ble.shine.parser;

import com.misfit.ble.shine.result.Activity;
import com.misfit.ble.shine.result.BatteryEvent;
import com.misfit.ble.shine.result.OrientationEvent;
import com.misfit.ble.shine.result.SessionEvent;
import com.misfit.ble.shine.result.TapEvent;
import com.misfit.ble.shine.result.TapEventSummary;

import java.util.ArrayList;

public abstract class ActivityDataParser {
	public ArrayList<Activity> mActivities;
	public ArrayList<TapEvent> mTapEvents;
	public ArrayList<TapEventSummary> mTapEventSummarys;
	public ArrayList<OrientationEvent> mOrientationEvents;
	public ArrayList<BatteryEvent> mBatteryEvents;
	public ArrayList<SessionEvent> mSessionEvents;
	public ArrayList<Object> mSwimEntries;
	
	public ActivityDataParser() {
		mActivities = new ArrayList<Activity>();
		mTapEvents = new ArrayList<TapEvent>();
		mTapEventSummarys = new ArrayList<TapEventSummary>();
		mOrientationEvents = new ArrayList<OrientationEvent>();
		mBatteryEvents = new ArrayList<BatteryEvent>();
		mSessionEvents = new ArrayList<SessionEvent>();
		mSwimEntries = new ArrayList<Object>();
	}
	
	public abstract boolean parseRawData(byte[] rawData, int fileFormat, long fileTimestamp);
}
