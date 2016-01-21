package com.misfit.ble.shine;

import org.json.JSONException;
import org.json.JSONObject;

public class ShineEventAnimationMapping {
	public static final short ANIMATION_SUCCESS = 11;
	public static final short ANIMATION_ERROR = 12;
	public static final short ANIMATION_DOUBLE_RECEIVED = 13;
	public static final short ANIMATION_TRIPLE_RECEIVED = 14;
	public static final short ANIMATION_DOUBLE_SUCCESS = 15;
	public static final short ANIMATION_TRIPLE_SUCCESS = 16;
	
//	public static final short ACTION_ACTIVE = 1 << 0;
//	public static final short ACTION_CONNECTED_DEFAULT = 1 << 1;
//	public static final short ACTION_UNCONNECTED_DEFAULT = 1 << 2;
//	public static final short ACTION_TIMEOUT_ANIMATION = 1 << 3;
	
	private short mButtonEventType;
	
	private short mActiveAndConnectedDefaultAnimationCode;
	private short mActiveAndConnectedDefaultAnimationRepeat;
	
	private short mUnconnectedDefaultAnimationCode;
	private short mUnconnectedDefaultAnimationRepeat;
	
	private short mTimeoutAnimationCode;
	private short mTimeoutAnimationRepeat;

	public ShineEventAnimationMapping(
			short buttonEventType,
			short activeAndConnectedDefaultAnimation, short activeAndConnectedDefaultAnimationRepeat,
			short unconnectedDefaultAnimation, short unconnectedDefaultAnimationRepeat,
			short timeoutAnimation, short timeoutAnimationRepeat) {
		mButtonEventType = buttonEventType;
		
		mActiveAndConnectedDefaultAnimationCode = activeAndConnectedDefaultAnimation;
		mActiveAndConnectedDefaultAnimationRepeat = activeAndConnectedDefaultAnimationRepeat;
		
		mUnconnectedDefaultAnimationCode = unconnectedDefaultAnimation;
		mUnconnectedDefaultAnimationRepeat = unconnectedDefaultAnimationRepeat;
		
		mTimeoutAnimationCode = timeoutAnimation;
		mTimeoutAnimationRepeat = timeoutAnimationRepeat;
	}
	
	public short getButtonEventType() {
		return mButtonEventType;
	}
	
	public short getActiveAndConnectedDefaultAnimationCode() {
		return mActiveAndConnectedDefaultAnimationCode;
	}
	
	public short getActiveAndConnectedDefaultAnimationRepeat() {
		return mActiveAndConnectedDefaultAnimationRepeat;
	}
	
	public short getUnconnectedDefaultAnimationCode() {
		return mUnconnectedDefaultAnimationCode;
	}
	
	public short getUnconnectedDefaultAnimationRepeat() {
		return mUnconnectedDefaultAnimationRepeat;
	}
	
	public short getTimeoutAnimationCode() {
		return mTimeoutAnimationCode;
	}

	public short getTimeoutAnimationRepeat() {
		return mTimeoutAnimationRepeat;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("buttonEventType", this.mButtonEventType);
			
			json.put("activeAnimationType", this.mActiveAndConnectedDefaultAnimationCode);
			json.put("activeAnimationRepeat", this.mActiveAndConnectedDefaultAnimationRepeat);

			json.put("unconnectedAnimationType", this.mUnconnectedDefaultAnimationCode);
			json.put("unconnectedAnimationRepeat", this.mUnconnectedDefaultAnimationRepeat);

			json.put("timeoutAnimationType", this.mTimeoutAnimationCode);
			json.put("timeoutAnimationRepeat", this.mTimeoutAnimationRepeat);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
