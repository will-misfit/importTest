package com.misfit.syncsdk.enums;

/**
 * BLE_XXX failedReason needs to be sent upwards from ShineSDK. if ShineSDK does not update,
 * all of the BLE_XXX failedReason cannot be written in LogSession currently
 * */
public final class FailedReason {
    public final static int DEFAULT = 1;
    public final static int DISCONNECTED_UNEXPECTEDLY = 2;
    public final static int INTERRUPTED_BY_USER = 3;
    public final static int SYNC_FAIL = 4;
    public final static int NO_DEVICE_WITH_SERIAL_NUMBER_FOUND = 5;
    public final static int NO_DEVICE_WITH_EXPECTED_TYPE_FOUND = 6; // if add it in LogSession, it has to be in MisfitScanner
    public final static int TIMEOUT = 7;
    public final static int BLUETOOTH_POWERED_OFF = 8;

    //    Internal Failure
    public final static int BLE_CONNECT_FAILURE = 100;
    public final static int BLE_DISCONNECT_FAILURE = 101;
    public final static int BLE_READ_SERIAL_NUMBER_FAILURE = 102;
    public final static int BLE_READ_MODEL_NUMBER_FAILURE = 103;
    public final static int BLE_READ_FIRMWARE_VERSION_FAILURE = 104;
    public final static int BLE_ACTIVATE_REQUEST_FAILURE = 105;
    public final static int BLE_DISPLAY_PAIR_ANIMATION_REQUEST_FAILURE = 106;
    public final static int BLE_DISPLAY_SYNC_ANIMATION_REQUEST_FAILURE = 107;
    public final static int BLE_STOP_ANIMATION_REQUEST_FAILURE = 108;
    public final static int BLE_GET_ACTIVITY_POINT_REQUEST_FAILURE = 109;
    public final static int BLE_GET_ACTIVITY_TAGGING_STATE_REQUEST_FAILURE = 110;
    public final static int BLE_GET_BATTERY_REQUEST_FAILURE = 111;
    public final static int BLE_GET_CLOCK_STATE_REQUEST_FAILURE = 112;
    public final static int BLE_GET_GOAL_REQUEST_FAILURE = 113;
    public final static int BLE_GET_TIME_REQUEST_FAILURE = 114;
    public final static int BLE_GET_TRIPLE_TAP_ENA_BLE_REQUEST_FAILURE = 115;
    public final static int BLE_SET_ACTIVITY_POINT_REQUEST_FAILURE = 116;
    public final static int BLE_SET_ACTIVITY_TAGGING_STATE_REQUEST_FAILURE = 117;
    public final static int BLE_SET_CLOCK_STATE_REQUEST_FAILURE = 118;
    public final static int BLE_SET_EXTRA_ADVERTISING_DATA_STATE_REQUEST_FAILURE = 119;
    public final static int BLE_SET_GOAL_REQUEST_FAILURE = 120;
    public final static int BLE_SET_TIME_REQUEST_FAILURE = 121;
    public final static int BLE_SET_TRIPLE_TAP_ENA_BLE_REQUEST_FAILURE = 122;
    public final static int BLE_GET_CONNECTION_PARAMETERS_REQUEST_FAILURE = 123;
    public final static int BLE_SET_CONNECTION_PARAMETERS_REQUEST_FAILURE = 124;
    public final static int BLE_FILE_STREAMING_REQUEST_FAILURE = 125;
    public final static int BLE_GET_FLASH_BUTTON_MODE_REQUEST_FAILURE = 126;
    public final static int BLE_MAP_BUTTON_EVENT_ANIMATION_REQUEST_FAILURE = 127;
    public final static int BLE_PLAY_BUTTON_EVENT_RESPONSE_ANIMATION_REQUEST_FAILURE = 128;
    public final static int BLE_SET_FILE_STREAM_HEARTBEAT_INTERVAL_REQUEST_FAILURE = 129;
    public final static int BLE_SET_FILE_STREAM_NUMBER_OF_PACKETS_PER_EVENT_REQUEST_FAILURE = 130;
    public final static int BLE_SET_FLASH_BUTTON_MODE_REQUEST_FAILURE = 131;
    public final static int BLE_STOP_FILE_STREAMING_REQUEST_FAILURE = 132;
    public final static int BLE_OTA_ENTER_REQUEST_FAILURE = 133;
    public final static int BLE_OTA_ERASE_REQUEST_FAILURE = 134;
    public final static int BLE_OTA_GET_SIZE_WRITTEN_REQUEST_FAILURE = 135;
    public final static int BLE_OTA_PUT_REQUEST_FAILURE = 136;
    public final static int BLE_OTA_RESET_REQUEST_FAILURE = 137;
    public final static int BLE_OTA_VERIFY_FILE_REQUEST_FAILURE = 138;
    public final static int BLE_OTA_VERIFY_SEGMENT_REQUEST_FAILURE = 139;
    public final static int BLE_CLEAR_ALL_ALARMS_REQUEST_FAILURE = 140;
    public final static int BLE_DISA_BLE_ALL_BLE_NOTIFICATIONS_REQUEST_FAILURE = 141;
    public final static int BLE_GET_ALARM_PARAMETERS_REQUEST_FAILURE = 142;
    public final static int BLE_GET_BLE_NOTIFICATION_REQUEST_FAILURE = 143;
    public final static int BLE_GET_BLE_NOTIFICATION_TIME_WINDOW_REQUEST_FAILURE = 144;
    public final static int BLE_GET_GOAL_HIT_NOTIFICATION_REQUEST_FAILURE = 145;
    public final static int BLE_GET_INACTIVITY_NUDGE_REQUEST_FAILURE = 146;
    public final static int BLE_GET_SINGLE_ALARM_REQUEST_FAILURE = 147;
    public final static int BLE_PLAY_LED_REQUEST_FAILURE = 148;
    public final static int BLE_PLAY_SOUND_REQUEST_FAILURE = 149;
    public final static int BLE_PLAY_VIBRATION_REQUEST_FAILURE = 150;
    public final static int BLE_SET_ALARM_PARAMETERS_REQUEST_FAILURE = 151;
    public final static int BLE_SETUP_BLE_NOTIFICATION_REQUEST_FAILURE = 152;
    public final static int BLE_SETUP_BLE_NOTIFICATION_TIME_WINDOW_REQUEST_FAILURE = 153;
    public final static int BLE_SETUP_GOAL_HIT_NOTIFICATION_REQUEST_FAILURE = 154;
    public final static int BLE_SETUP_INACTIVITY_NUDGE_REQUEST_FAILURE = 155;
    public final static int BLE_SETUP_SINGLE_ALARM_REQUEST_FAILURE = 156;
    public final static int BLE_FILE_ABORT_REQUEST_FAILURE = 157;
    public final static int BLE_FILE_ERASE_ACTIVITY_REQUEST_FAILURE = 158;
    public final static int BLE_FILE_ERASE_HARDWARE_LOG_REQUEST_FAILURE = 159;
    public final static int BLE_FILE_ERASE_REQUEST_FAILURE = 160;
    public final static int BLE_FILE_GET_ACTIVITY_REQUEST_FAILURE = 161;
    public final static int BLE_FILE_GET_HARDWARE_LOG_REQUEST_FAILURE = 162;
    public final static int BLE_FILE_GET_REQUEST_FAILURE = 163;
    public final static int BLE_FILE_LIST_REQUEST_FAILURE = 164;

    //    Internal timeout
    public final static int BLE_CONNECT_TIMEOUT = 200;
    public final static int BLE_DISCONNECT_TIMEOUT = 201;
    public final static int BLE_READ_SERIAL_NUMBER_TIMEOUT = 202;
    public final static int BLE_READ_MODEL_NUMBER_TIMEOUT = 203;
    public final static int BLE_READ_FIRMWARE_VERSION_TIMEOUT = 204;
    public final static int BLE_ACTIVATE_REQUEST_TIMEOUT = 205;
    public final static int BLE_DISPLAY_PAIR_ANIMATION_REQUEST_TIMEOUT = 206;
    public final static int BLE_DISPLAY_SYNC_ANIMATION_REQUEST_TIMEOUT = 207;
    public final static int BLE_STOP_ANIMATION_REQUEST_TIMEOUT = 208;
    public final static int BLE_GET_ACTIVITY_POINT_REQUEST_TIMEOUT = 209;
    public final static int BLE_GET_ACTIVITY_TAGGING_STATE_REQUEST_TIMEOUT = 210;
    public final static int BLE_GET_BATTERY_REQUEST_TIMEOUT = 211;
    public final static int BLE_GET_CLOCK_STATE_REQUEST_TIMEOUT = 212;
    public final static int BLE_GET_GOAL_REQUEST_TIMEOUT = 213;
    public final static int BLE_GET_TIME_REQUEST_TIMEOUT = 214;
    public final static int BLE_GET_TRIPLE_TAP_ENA_BLE_REQUEST_TIMEOUT = 215;
    public final static int BLE_SET_ACTIVITY_POINT_REQUEST_TIMEOUT = 216;
    public final static int BLE_SET_ACTIVITY_TAGGING_STATE_REQUEST_TIMEOUT = 217;
    public final static int BLE_SET_CLOCK_STATE_REQUEST_TIMEOUT = 218;
    public final static int BLE_SET_EXTRA_ADVERTISING_DATA_STATE_REQUEST_TIMEOUT = 219;
    public final static int BLE_SET_GOAL_REQUEST_TIMEOUT = 220;
    public final static int BLE_SET_TIME_REQUEST_TIMEOUT = 221;
    public final static int BLE_SET_TRIPLE_TAP_ENA_BLE_REQUEST_TIMEOUT = 222;
    public final static int BLE_GET_CONNECTION_PARAMETERS_REQUEST_TIMEOUT = 223;
    public final static int BLE_SET_CONNECTION_PARAMETERS_REQUEST_TIMEOUT = 224;
    public final static int BLE_FILE_STREAMING_REQUEST_TIMEOUT = 225;
    public final static int BLE_GET_FLASH_BUTTON_MODE_REQUEST_TIMEOUT = 226;
    public final static int BLE_MAP_BUTTON_EVENT_ANIMATION_REQUEST_TIMEOUT = 227;
    public final static int BLE_PLAY_BUTTON_EVENT_RESPONSE_ANIMATION_REQUEST_TIMEOUT = 228;
    public final static int BLE_SET_FILE_STREAM_HEARTBEAT_INTERVAL_REQUEST_TIMEOUT = 229;
    public final static int BLE_SET_FILE_STREAM_NUMBER_OF_PACKETS_PER_EVENT_REQUEST_TIMEOUT = 230;
    public final static int BLE_SET_FLASH_BUTTON_MODE_REQUEST_TIMEOUT = 231;
    public final static int BLE_STOP_FILE_STREAMING_REQUEST_TIMEOUT = 232;
    public final static int BLE_OTA_ENTER_REQUEST_TIMEOUT = 233;
    public final static int BLE_OTA_ERASE_REQUEST_TIMEOUT = 234;
    public final static int BLE_OTA_GET_SIZE_WRITTEN_REQUEST_TIMEOUT = 235;
    public final static int BLE_OTA_PUT_REQUEST_TIMEOUT = 236;
    public final static int BLE_OTA_RESET_REQUEST_TIMEOUT = 237;
    public final static int BLE_OTA_VERIFY_FILE_REQUEST_TIMEOUT = 238;
    public final static int BLE_OTA_VERIFY_SEGMENT_REQUEST_TIMEOUT = 239;
    public final static int BLE_CLEAR_ALL_ALARMS_REQUEST_TIMEOUT = 240;
    public final static int BLE_DISA_BLE_ALL_BLE_NOTIFICATIONS_REQUEST_TIMEOUT = 241;
    public final static int BLE_GET_ALARM_PARAMETERS_REQUEST_TIMEOUT = 242;
    public final static int BLE_GET_BLE_NOTIFICATION_REQUEST_TIMEOUT = 243;
    public final static int BLE_GET_BLE_NOTIFICATION_TIME_WINDOW_REQUEST_TIMEOUT = 244;
    public final static int BLE_GET_GOAL_HIT_NOTIFICATION_REQUEST_TIMEOUT = 245;
    public final static int BLE_GET_INACTIVITY_NUDGE_REQUEST_TIMEOUT = 246;
    public final static int BLE_GET_SINGLE_ALARM_REQUEST_TIMEOUT = 247;
    public final static int BLE_PLAY_LED_REQUEST_TIMEOUT = 248;
    public final static int BLE_PLAY_SOUND_REQUEST_TIMEOUT = 249;
    public final static int BLE_PLAY_VIBRATION_REQUEST_TIMEOUT = 250;
    public final static int BLE_SET_ALARM_PARAMETERS_REQUEST_TIMEOUT = 251;
    public final static int BLE_SETUP_BLE_NOTIFICATION_REQUEST_TIMEOUT = 252;
    public final static int BLE_SETUP_BLE_NOTIFICATION_TIME_WINDOW_REQUEST_TIMEOUT = 253;
    public final static int BLE_SETUP_GOAL_HIT_NOTIFICATION_REQUEST_TIMEOUT = 254;
    public final static int BLE_SETUP_INACTIVITY_NUDGE_REQUEST_TIMEOUT = 255;
    public final static int BLE_SETUP_SINGLE_ALARM_REQUEST_TIMEOUT = 256;
    public final static int BLE_FILE_ABORT_REQUEST_TIMEOUT = 257;
    public final static int BLE_FILE_ERASE_ACTIVITY_REQUEST_TIMEOUT = 258;
    public final static int BLE_FILE_ERASE_HARDWARE_LOG_REQUEST_TIMEOUT = 259;
    public final static int BLE_FILE_ERASE_REQUEST_TIMEOUT = 260;
    public final static int BLE_FILE_GET_ACTIVITY_REQUEST_TIMEOUT = 261;
    public final static int BLE_FILE_GET_HARDWARE_LOG_REQUEST_TIMEOUT = 262;
    public final static int BLE_FILE_GET_REQUEST_TIMEOUT = 263;
    public final static int BLE_FILE_LIST_REQUEST_TIMEOUT = 264;


    //    Internal invalid parameter
    public final static int BLE_SET_ACTIVITY_POINT_REQUEST_INVALID_PARAMETER = 300;
    public final static int BLE_SET_ACTIVITY_TAGGING_STATE_REQUEST_INVALID_PARAMETER = 301;
    public final static int BLE_SET_CLOCK_STATE_REQUEST_INVALID_PARAMETER = 302;
    public final static int BLE_SET_EXTRA_ADVERTISING_DATA_STATE_REQUEST_INVALID_PARAMETER = 303;
    public final static int BLE_SET_GOAL_REQUEST_INVALID_PARAMETER = 304;
    public final static int BLE_SET_TRIPLE_TAP_ENA_BLE_REQUEST_INVALID_PARAMETER = 305;
    public final static int BLE_SET_CONNECTION_PARAMETERS_REQUEST_INVALID_PARAMETER = 306;
    public final static int BLE_SET_FILE_STREAM_HEARTBEAT_INTERVAL_REQUEST_INVALID_PARAMETER = 307;
    public final static int BLE_SET_FILE_STREAM_NUMBER_OF_PACKETS_PER_EVENT_REQUEST_INVALID_PARAMETER = 308;
    public final static int BLE_SET_FLASH_BUTTON_MODE_REQUEST_INVALID_PARAMETER = 309;
    public final static int BLE_SET_ALARM_PARAMETERS_REQUEST_INVALID_PARAMETER = 310;
    public final static int BLE_SETUP_BLE_NOTIFICATION_REQUEST_INVALID_PARAMETER = 311;
    public final static int BLE_SETUP_BLE_NOTIFICATION_TIME_WINDOW_REQUEST_INVALID_PARAMETER = 312;
    public final static int BLE_SETUP_GOAL_HIT_NOTIFICATION_REQUEST_INVALID_PARAMETER = 313;
    public final static int BLE_SETUP_INACTIVITY_NUDGE_REQUEST_INVALID_PARAMETER = 314;
    public final static int BLE_SETUP_SINGLE_ALARM_REQUEST_INVALID_PARAMETER = 315;

    //    Firmware error
    public final static int FIRMWARE_CHECKSUM_MISMATCH = 400;
    public final static int FIRMWARE_DATA_NULL = 401;
    public final static int FIRMWARE_SAVE_FAILURE = 402;

    //    Network error
    public final static int NETWORK_FAILURE = 500;
    public final static int NETWORK_UN_EXPECTED_DATA_TYPE = 501;

    //    Log error
    public final static int NETWORK_SESSION_REQUEST_FAILURE = 600;
    public final static int NETWORK_ACTIVITY_DATA_REQUEST_FAILURE = 601;
}