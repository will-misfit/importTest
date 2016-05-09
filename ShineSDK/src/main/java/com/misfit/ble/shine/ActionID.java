package com.misfit.ble.shine;

/**
 * Created by Quoc-Hung Le on 9/4/15.
 */

/**
 * Action ID Enumeration
 */
public enum ActionID {
	IDLE,

	/* Basic */
	ANIMATE,
	STOP_ANIMATING,
	ACTIVATE,
	GET_ACTIVATION_STATE,
	GET_CONFIGURATION,
	SET_CONFIGURATION,
	SYNC,
	OTA,
	CHANGE_SERIAL_NUMBER,
	GET_CONNECTION_PARAMETERS,
	SET_CONNECTION_PARAMETERS,
	GET_FLASH_BUTTON_MODE,
	SET_FLASH_BUTTON_MODE,
	GET_LAP_COUNTING_STATUS,
	SET_LAP_COUNTING_LICENSE_INFO,
	SET_LAP_COUNTING_MODE,

	/* Streaming */
	STREAM_USER_INPUT_EVENTS,
	GET_STREAMING_CONFIGURATION,
	SET_STREAMING_CONFIGURATION,
	SET_EXTRA_ADV_DATA_STATE,
	GET_EXTRA_ADV_DATA_STATE,
	READ_REMOTE_RSSI,
	START_BUTTON_ANIMATION,
	MAP_EVENT_ANIMATION,
	UNMAP_ALL_EVENT_ANIMATION,
	EVENT_MAPPING_SYSTEM_CONTROL,

	/* Pluto */
	SET_INACTIVITY_NUDGE,
	GET_INACTIVITY_NUDGE,
	SET_GOAL_HIT_NOTIFICATION,
	GET_GOAL_HIT_NOTIFICATION,
	SET_SINGLE_ALARM_TIME,
	GET_SINGLE_ALARM_TIME,
	CLEAR_ALL_ALARMS,
	SET_CALL_TEXT_NOTIFICATIONS,
	GET_CALL_TEXT_NOTIFICATIONS,
	DISABLE_ALL_CALL_TEXT_NOTIFICATIONS,
	SEND_CALL_NOTIFICATION,
	SEND_TEXT_NOTIFICATION,
	STOP_NOTIFICATION,
	PLAY_VIBRATION,
	PLAY_SOUND,
	PLAY_LED_ANIMATION,
	START_SPECIFIED_NOTIFICATION,
	START_SPECIFIED_ANIMATION,
	START_SPECIFIED_VIBRATION,

	/* Flash Link */
	SET_CUSTOM_MODE,
	UNMAP_EVENT,
	UNMAP_ALL_EVENTS,
	
	ADD_GROUP_ID,
	GET_GROUP_ID,
	SET_PASSCODE,
	GET_PASSCODE
}
