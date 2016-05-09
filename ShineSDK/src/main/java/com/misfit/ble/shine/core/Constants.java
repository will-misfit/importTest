package com.misfit.ble.shine.core;


import java.util.Locale;
import java.util.UUID;

public class Constants {
	// Misfit Service
    public static final String MFSERVICE_UUID                         				= "3dda0001-957f-7d4a-34a6-74696673696d";
    public static final String MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID 	= "3dda0002-957f-7d4a-34a6-74696673696d";
    public static final String MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID 	= "3dda0003-957f-7d4a-34a6-74696673696d";
    public static final String MFSERVICE_FILE_TRANSFER_DATA_CHARACTERISTIC_UUID    	= "3dda0004-957f-7d4a-34a6-74696673696d";
	public static final String MFSERVICE_UUID_WITHOUT_SEPERATOR_UPPERCASED = MFSERVICE_UUID.replace("-", "").toUpperCase(Locale.US);

	public static final UUID[] SCAN_SERVICES_UUIDs = new UUID[] {UUID.fromString(Constants.MFSERVICE_UUID)};

    /*! Standard device information: http://developer.bluetooth.org/gatt/services/Pages/ServiceViewer.aspx?u=org.bluetooth.service.device_information.xml */
    public static final String DISERVICE_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String DISERVICE_FIRMWARE_REVISION_CHARACTERISTIC_UUID = "00002a26-0000-1000-8000-00805f9b34fb";
    public static final String DISERVICE_SERIAL_NUMBER_CHARACTERISTIC_UUID = "00002a25-0000-1000-8000-00805f9b34fb";
    public static final String DISERVICE_MODEL_NUMBER_CHARACTERISTIC_UUID = "00002a24-0000-1000-8000-00805f9b34fb";
    
    public static final String FACTORY_SERIAL_NUMBER = "9876543210";
    
    // Device Configuration Operation Code
 	public static final byte DEVICE_CONFIG_OPERATION_GET = 1;
 	public static final byte DEVICE_CONFIG_OPERATION_SET = 2;
 	public static final byte DEVICE_CONFIG_OPERATION_RESPONSE = 3;
 	
 	// Device Configuration Error Code
 	public static final byte DEVICE_CONFIG_RESPONSE_ERROR_INVALID_ATTRIBUTE_VALUE_LENGTH = 0x0d;
 	public static final byte DEVICE_CONFIG_RESPONSE_ERROR_CHARACTERISTIC_NOT_CONFIGURED_FOR_NOTIFICATIONS = (byte)0xfd;
 	public static final byte DEVICE_CONFIG_RESPONSE_ERROR_OPERATION_IN_PROGRESS = (byte)0xfe;
 	public static final byte DEVICE_CONFIG_RESPONSE_ERROR_OUT_OF_RANGE = 0x0f;
 	
 	public static final byte DEVICE_CONFIG_RESPONSE_ERROR_AUTHENTICATION_REQUIRED = (byte)0x80;
 	
 	// Device Configuration Parameter ID
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_DATA_FORMAT = 0x01;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_SENSOR_CONFIGURATION = 0x02;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_ALGORITHM = 0x03;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_TIME = 0x04;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_GOAL = 0x05;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_ACTIVITY_POINT = 0x06;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_DEVICE_SETTINGS_IN_OUT = 0x07;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_BATTERY_LEVEL = 0x08;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_CONNECTION_PARAMETER_SET = 0x09;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_CONNECTION_PARAMETER_GET = 0x0a;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_EVENT_MAPPING_CONFIGURATION = 0x0b;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_STREAMING_CONFIGURATION = 0x0c;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_DEBUG_INFO_GET = (byte)0xf0;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_DIAGNOSTIC_FUNCTIONS = (byte)0xf1;
 	public static final byte DEVICE_CONFIG_PARAMETER_ID_HARDWARE_TEST = (byte)0xf2;
 	
 	// Device Configuration Diagnostic Functions
 	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_DISPLAY_PAIR_ANIMATION = 0x05;
 	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_DISPLAY_SYNC_ANIMATION = 0x06;
 	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_STOP_ANIMATION = 0x07;
 	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_OTA_ENTER = 0x08;
 	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_OTA_ENTER_RESPONSE = 0x09;
 	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_OTA_RESET = 0x0a;
 	// Venus
 	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_ACTIVATE   = 0x0b;
 	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_DEACTIVATE = 0x0c;
	// Pluto
	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_PLAY_VIBRATION = 0x10;
	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_PLAY_SOUND = 0x11;
	public static final byte DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_PLAY_LED_ANIMATION = 0x12;
 	
 	// Device Settings Id
 	public static final byte DEVICE_SETTING_ID_SERIAL_NUMBER = 0x01;
 	public static final byte DEVICE_SETTING_ID_CLOCK_STATE = 0x02;
 	public static final byte DEVICE_SETTING_ID_TRIPLE_TAP_ENABLE = 0x03;
 	public static final byte DEVICE_SETTING_ID_DOUBLE_TAP_ANIMATION_FAST_FORDWARD_ENABLE = 0x04;
 	public static final byte DEVICE_SETTING_ID_ACTIVITY_TAGGING_ENABLE = 0x05;
 	public static final byte DEVICE_SETTING_ID_BLE_ADVERTISING_INTERVAL = 0x06;
 	public static final byte DEVICE_SETTING_ID_SERIAL_NUMBER_EXTENDED = 0x07;
 	public static final byte DEVICE_SETTING_ID_EXTRA_ADVERTISING_DATA_STATE = 0x08;
 	public static final byte DEVICE_SETTING_ID_ACTIVITY_TAGGING_STATE = 0x09;
	public static final byte DEVICE_SETTING_ID_DEVICE_MODE = 0x0a;
	public static final byte DEVICE_SETTING_ID_CLEAR_ACTIVITY_LOG = 0x0b;
	public static final byte DEVICE_SETTING_ID_SET_DEVICE_LINK_STATE = 0x0c;
	public static final byte DEVICE_SETTING_ID_GENERATING_PATTERN_FILES = 0x0d;
	public static final byte DEVICE_SETTING_ID_ALARMS = 0x0e;
	public static final byte DEVICE_SETTING_ID_BLE_NOTIFICATIONS = 0x0f;

	public class LEDControl {
		public final static byte PARAMETER_ID = 0x0e;
		public final static byte COMMAND_START_SPECIFIED_ANIMATION = 0x04;
		public final static byte COMMAND_ANIMATION_COLOR = 0x05;
	}

	public class VibeControl {
		public final static byte PARAMETER_ID = 0x0f;
		public final static byte COMMAND_PLAY_SPECIFIED_VIBRATION = 0x05;
	}

	public class LapCounting {
        public static final byte PARAMETER_ID_LAP_COUNTING = 0x03;
        public static final byte COMMAND_ID_LAP_COUNTING = 0x06;
        public static final byte SETTING_ID_LAP_COUNTING = 0x02;

        public static final byte PARAMETER_ID_MODE = 0x0b;
        public static final byte COMMAND_ID_MODE = 0x40;
        public static final byte SETTING_ID_MODE = 0x0c;
        public static final byte EXTRA_MODE = 0x10;

    }

	// Algorithm Settings Id (Pluto)
	public static final byte ALGORITHM_SETTING_ID_SET_ACE = 0x01;
	public static final byte ALGORITHM_SETTING_ID_CONFIG_SMARTLOCK_AND_ONBODY = 0x02;
	public static final byte ALGORITHM_SETTING_ID_INACTIVITY_NUDGE = 0x03;
	public static final byte ALGORITHM_SETTING_ID_GOAL_HIT_NOTIFICATION = 0x04;

	// Command Id (Pluto)
	public static final byte COMMAND_ID_INACTIVITY_NUDGE_SETUP = 0x01;

	public static final byte COMMAND_ID_GOAL_HIT_NOTIFICATION_SETUP = 0x01;

	public static final byte COMMAND_ID_CLEAR_ALL_ALARMS = 0x01;
	public static final byte COMMAND_ID_SETUP_SINGLE_ALARM_TIME = 0x02;
	public static final byte COMMAND_ID_SETUP_ALARM_PARAMETERS = 0x05;

	public static final byte COMMAND_ID_SETUP_TEXT_CALL_NOTIFICATIONS = 0x01;
	public static final byte COMMAND_ID_DISABLE_ALL_NOTIFICATIONS = 0x02;
	public static final byte COMMAND_ID_SETUP_CALL_TEXT_NOTIFICATIONS_TIME_WINDOW = 0x03;
	public static final byte COMMAND_ID_SEND_CALL_NOTIFICATION = 0x04;
	public static final byte COMMAND_ID_SEND_TEXT_NOTIFICATION = 0x05;
	public static final byte COMMAND_ID_STOP_NOTIFICATION = 0x06;

 	// Serial Number Extended Settings Operation Id
 	public static final byte SERIAL_NUMBER_EXTENDED_OPERATION_ID_SET = (byte)0xa6;
 	public static final byte SERIAL_NUMBER_EXTENDED_OPERATION_ID_CHANGE = (byte)0xa7;
 	public static final byte SERIAL_NUMBER_EXTENDED_OPERATION_ID_LOCK = (byte)0xa8;
 	public static final byte SERIAL_NUMBER_EXTENDED_OPERATION_ID_UNLOCK = (byte)0xa9;
 	public static final byte SERIAL_NUMBER_EXTENDED_OPERATION_ID_CHANGE_AND_LOCK = (byte)0xaa;
 	
 	/*!
 	 * 	Event Mapping Settings
 	 *  Refer to the spec: https://misfit.jira.com/wiki/display/HAR/Misfit+Event+Mapping
 	 */
  	public static final byte EVENT_MAPPING_START_ANIMATION = 0x01;
  	public static final byte EVENT_MAPPING_START_AND_REPEAT_ANIMATION = 0x02;
  	public static final byte EVENT_MAPPING_MAP_POSITIVE_AND_NEGATIVE_ANIMATION = 0x17;
  	public static final byte EVENT_MAPPING_MAP_POSITIVE_AND_NEGATIVE_TIMEOUT_ANIMATION = 0x27;
  	public static final byte EVENT_MAPPING_MAP_POSITIVE_AND_NEGATIVE_AND_TIMEOUT_ANIMATION_WITH_REPEAT = 0x28;
  	public static final byte EVENT_MAPPING_UNMAP_ALL = 0x30;
	public static final byte EVENT_MAPPING_UNMAP_EVENT = 0x31;
  	public static final byte EVENT_MAPPING_UNPAUSE_EVENTS_AND_RESET_SEQUENCE_FLAG = (byte)0xe3;

  	
 	// Streaming Settings List
 	public static final byte STREAMING_SETTING_ID_NUMBER_OF_MAPPED_EVENT_PACKETS = (byte)0xe0;
 	public static final byte STREAMING_SETTING_ID_CONNECTION_HEARTBEAT_INTERVAL = (byte)0xf0;

 	// File Control
 	public static final byte FILE_CONTROL_OPERATION_GET = 0x01;
	public static final byte FILE_CONTROL_OPERATION_GET_RESPONSE = 0x02;
	public static final byte FILE_CONTROL_OPERATION_ERASE = 0x03;
	public static final byte FILE_CONTROL_OPERATION_ERASE_RESPONSE = 0x04;
	public static final byte FILE_CONTROL_OPERATION_LIST = 0x05;
	public static final byte FILE_CONTROL_OPERATION_LIST_RESPONSE = 0x06;
	public static final byte FILE_CONTROL_OPERATION_ABORT_REQUEST_RESPONSE = 0x07;
	public static final byte FILE_CONTROL_OPERATION_EOF_RESPONSE = 0x08;
	public static final byte FILE_CONTROL_OPERATION_SIZE = 0x09;
	public static final byte FILE_CONTROL_OPERATION_SIZE_RESPONSE = 0x0a;
	public static final byte FILE_CONTROL_OPERATION_OTA_PUT = 0x0b;
	public static final byte FILE_CONTROL_OPERATION_OTA_PUT_RESPONSE = 0x0c;
	public static final byte FILE_CONTROL_OPERATION_OTA_VERIFY_FILE = 0x0d;
	public static final byte FILE_CONTROL_OPERATION_OTA_VERIFY_FILE_RESPONSE = 0x0e;
	public static final byte FILE_CONTROL_OPERATION_OTA_PUT_EOF = 0x0f;
	public static final byte FILE_CONTROL_OPERATION_OTA_VERIFY_SEGMENT = 0x10;
	public static final byte FILE_CONTROL_OPERATION_OTA_VERIFY_SEGMENT_RESPONSE = 0x11;
	public static final byte FILE_CONTROL_OPERATION_OTA_ERASE = 0x12;
	public static final byte FILE_CONTROL_OPERATION_OTA_ERASE_RESPONSE = 0x13;
	public static final byte FILE_CONTROL_OPERATION_OTA_GET_WRITTEN_SIZE = 0x14;
	public static final byte FILE_CONTROL_OPERATION_OTA_GET_WRITTEN_SIZE_RESPONSE = 0x15;
	
	// File Control - Error
	public static final byte FILE_CONTROL_RESPONSE_SUCCESS = 0x00;
	public static final byte FILE_CONTROL_RESPONSE_INVALID_OPERATION = 0x01;
	public static final byte FILE_CONTROL_RESPONSE_INVALID_FILE_HANDLE = 0x02;
	public static final byte FILE_CONTROL_RESPONSE_INVALID_OPERATION_DATA = 0x03;
	public static final byte FILE_CONTROL_RESPONSE_OPERATION_IN_PROGRESS = 0x04;
	public static final byte FILE_CONTROL_RESPONSE_VERIFICATION_FAILURE = 0x05;
	
	// Connection Parameter - Error
	public static final byte CONNECTION_PARAMETER_RESPONSE_SUCCESS = 0x00;
	public static final byte CONNECTION_PARAMETER_RESPONSE_FAILURE = 0x01;
	
	// File Transfer
	public static final byte FILE_TRANSFER_SEQUENCE_MASK = 0x3f;
	public static final byte FILE_TRANSFER_EOF_MASK = (byte)0x80;
	
	// General 
	public static final byte RESPONSE_SUCCESS = 0x00;
	public static final byte RESPONSE_ERROR = 0x01;
	public static final byte RESPONSE_MISMATCHED = 0x02;
	public static final byte RESPONSE_WRONG_CRC = 0x03;
	
	// OTA
	public static final long OTA_SEGMENT_SIZE = 2 * 1024;
	public static final long OTA_ERASE_END_OFFSET = 4;
	
	// Activity File
	public static final int ACTIVITY_FILE_HEADER_LENGTH_LEGACY = 16;
	public static final int ACTIVITY_FILE_CRC_LENGTH = 4;
	public static final int ACTIVITY_FILE_HEADER_LENGTH = 20;
	public static final int ACTIVITY_FILE_SPECIAL_FIELD_LENGTH = 2;
	
	// File Handle
	public static final short FILE_HANDLE_ACTIVITY_FILE = 0x0100;
	public static final int FILE_LENGTH_ACTIVITY_FILE = 0xffff;
	
	public static final short FILE_HANDLE_HARDWARE_LOG = 0x0010;
	public static final int FILE_LENGTH_HARDWARE_LOG = 0x0800;
	
	public static final short FILE_HANDLE_OTA = 0x5A5B;
	
	// File Streaming
	public static final short FILE_HANDLE_STREAMING_EVENTS_WITH_APP_ID = (short)0x8EF1;
	public static final short FILE_HANDLE_STREAMING_EVENTS = (short)0x8EF0; // alternative: 0x8E0F
    public static final short FILE_HANDLE_STREAMING_EVENTS_LEGACY = (short)0x8e00;

	public static final int FILE_LENGTH_STREAMING_EVENTS = 0;
	public static final int HEARTBEAT_PACKET_LENGTH = 4;
	public static final int HEARTBEAT_WITH_APPID_PACKET_LENGTH = 6;
	
	// File Streaming Packet Type
	public static final byte STREAMING_USER_INPUT_EVENTS_SEQUENCE_MASK = (byte)0xff;
	
	public static final byte STREAMING_PACKET_TYPE_USER_INPUT_EVENTS = (byte)0xe0;
	public static final byte STREAMING_PACKET_TYPE_CONNECTION_HEARTBEAT_LEGACY = (byte)0xff;
	public static final byte STREAMING_PACKET_TYPE_CONNECTION_HEARTBEAT = (byte)0xfe;
	
	// Connection Interval
	public static final float DEFAULT_CONNECTION_INTERVAL = 48.75f;
	public static final float MINIMUM_CONNECTION_INTERVAL = 7.5f;
	public static final float CONNECTION_INTERVAL_UNIT = 1.25f;
	public static final float CONNECTION_INTERVAL_STEP = 3 * CONNECTION_INTERVAL_UNIT;
	public static final int SUPERVISION_TIMEOUT_UNIT = 10;
	
	// File Parser
	public static final short MIN_SPECIAL_ENTRY_CODE = 200;
	public static final short IGNORE_PREV_ENTRY_CODE = 253;


	// Bolt Constant
	public static final byte MP_DC_ID_BTN_SETTINGS = 0x0d;

	public static final byte BTN_BOLT_CONTROL = 0x07;

	public static final byte BOLT_GROUP_ID = 0x01;
	public static final byte BOLT_PASSCODE = 0x02;

	// BluetoothGatt constants copied from https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/master/stack/include/gatt_api.h
	public static final int GATT_SUCCESS   = 0x00;
	public static final int GATT_ERROR     = 0x85;  // 133
	public static final int GATT_AUTH_FAIL = 0x89;  // 137
	public static final int GATT_CONN_TERMINATE_PEER_USER = 0x13; // 19

	// BluetoothGatt connect management parameters
	public static final int INTERVAL_BEFORE_CONNECT = 3000;  // in micro second
	public static final int CONNECT_CALLBACK_TIMEOUT = 8000;    // in micro second
	public static final int DISCONNECT_CALLBACK_TIMEOUT = 500;  // in micro second
	public static final int EXTERNAL_CONNECT_TIMEOUT = 44000;   // currently Misfit app set 30 sec for connect timeout, so SDK promises to stop all internal attempts before it ticks
	public static final int DUMMY_FILE_LIST_TIMEOUT = 1000;   // in milliseconds

	// Bluetooth scanning retry
	public static final int INTERNAL_BEFORE_RESTART_SCAN = 1000;

}
