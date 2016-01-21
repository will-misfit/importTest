package com.misfit.ble.shine.result;

public class UserInputEvent {
	public static final int EVENT_TYPE_NO_EVENT = 0;
	public static final int EVENT_TYPE_CONSUMED = 1;
	public static final int EVENT_TYPE_TIMEOUT = 2;
	public static final int EVENT_TYPE_END_OF_ANIMATION = 3;
	public static final int EVENT_TYPE_END_OF_SEQUENCE = 4;
	public static final int EVENT_TYPE_CONNECTION_CLOSE = 5;
	
	public static final int EVENT_TYPE_BUTTON_PRESS = 10;
	public static final int EVENT_TYPE_BUTTON_RELEASE_AFTER_SHORT_PRESS = 11;
	public static final int EVENT_TYPE_BUTTON_LONG_PRESS = 12;
	public static final int EVENT_TYPE_BUTTON_RELEASE_AFTER_LONG_PRESS = 13;

	public static final int EVENT_TYPE_BUTTON_SINGLE_PRESS = 19;
	public static final int EVENT_TYPE_BUTTON_DOUBLE_PRESS = 20;
	public static final int EVENT_TYPE_BUTTON_TRIPLE_PRESS = 21;
	public static final int EVENT_TYPE_BUTTON_DOUBLE_PRESS_AND_HOLD = 22;
	public static final int EVENT_TYPE_BUTTON_TRIPLE_PRESS_AND_HOLD = 23;
	
	public static final int EVENT_TYPE_SINGLE_TAP = 30;
	public static final int EVENT_TYPE_DOUBLE_TAP = 31;
	public static final int EVENT_TYPE_TRIPLE_TAP = 32;
	public static final int EVENT_TYPE_QUADRA_TAP_BEGIN = 24;
	public static final int EVENT_TYPE_QUADRA_TAP_END = 25;

	public static boolean validate(int eventType) {
		return true;//(EVENT_TYPE_PRESS <= eventType && eventType <= EVENT_TYPE_TRIPLE_TAP);
	}
}
