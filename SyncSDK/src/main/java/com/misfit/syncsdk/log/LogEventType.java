package com.misfit.syncsdk.log;

/**
 * Log event type names and ID defined in document
 */
public class LogEventType {

    /* constant Log Event ID */
    public static final int DEFAULT        = 0;
    public static final int START_SCANNING = 1;
    public static final int SCANNED_DEVICE = 2;
    public static final int STOP_SCANNING  = 3;

    public static final int CONNECT        = 4;
    public static final int GET_CACHED_DEVICE = 5;
    public static final int GET_CONNECTED_DEVICE = 6;
    public static final int DISCOVER_CHARACTERISTICS = 7;  // for iOS
    public static final int PREPARE        = 8;            // for iOS
    public static final int HANDSHAKE      = 9;

    public static final int SWITCH_TRACKER_MODE = 10;
    public static final int MAP_EVENT_ANIMATION = 11;
    public static final int UNMAP_EVENT_ANIMATION = 12;

    public static final int SET_CONNECTION_PARAMETER = 13;
    public static final int GET_CONNECTION_PARAMETER = 14;
    public static final int PLAY_ANIMATION = 15;
    public static final int STOP_ANIMATION = 16;
    public static final int SET_CONFIGURATION = 17;
    public static final int GET_CONFIGURATION = 18;

    public static final int FILE_LIST = 19;
    public static final int GET_ACTIVITY = 20;
    public static final int GET_HARDWARE_LOG = 21;

    public static final int DISCONNECT = 22;
    public static final int CALCULATE  = 23;

    public static final int START_FILE_STREAMING = 24;
    public static final int STOP_FILE_STREAMING  = 25;

    public static final int SET_ALARM = 26;
    public static final int SET_INACTIVE_NUDGES = 27;
    public static final int SET_CALL_TEXT_NOTIFICATION = 28;
    public static final int SET_GOAL_MET_NOTIFICATION = 29;

    public static final int OTA = 30;
    public static final int PLAY_SYNC_ANIMATION = 31;
    public static final int ACTIVATION = 32;
    public static final int STOP_RUNNING_OPERATION = 33;
    public static final int SET_FILE_STREAMING_PARAMETERS = 34;
    public static final int CLEAR_ALARM_SETTING = 35;
    public static final int SET_CLEAN_UP = 36;

    public static final int CHECK_FIRMWARE = 37;
    public static final int START_CALL_NOTIFICATION = 38;
    public static final int START_TEXT_NOTIFICATION = 39;
    public static final int DISABLE_ALL_CALL_TEXT_NOTIFICATION = 40;
    public static final int STOP_NOTIFICATION = 41;


    /* constant Log Event names */
    public static final String[] LogEventNames = {
        "Default",             // 0
        "StartScanning",       // 1
        "ScannedDevice",       // 2
        "StopScanning",        // 3

        "Connect",             // 4
        "GetCachedDevice",     // 5
        "GetConnectedDevice",  // 6
        "DiscoverCharacteristics", // 7
        "Prepare",             // 8
        "Handshake",           // 9

        "SwitchTrackerMode",   // 10
        "MapEventAnimation",   // 11
        "UnmapEventAnimation", // 12

        "SetConnectionParameter", // 13
        "GetConnectionParameter", // 14
        "PlayAnimation",          // 15
        "StopAnimation",          // 16
        "SetConfiguration",       // 17
        "GetConfiguration",       // 18

        "FileList",               // 19
        "GetActivity",            // 20
        "GetHardwareLog",         // 21

        "DISCONNECT",             // 22
        "Calculate",              // 23

        "StartFileStreaming",     // 24
        "StopFileStreaming",      // 25

        "SetAlarm",               // 26
        "SetInactiveNudges",      // 27
        "SetCallTextNotification",// 28
        "SetGoalMetNotification", // 29

        "OTA",                    // 30
        "PlaySyncAnimation",      // 31
        "Activation",             // 32
        "StopRunningOperation",   // 33
        "SetFileStreamingParameters",  // 34
        "ClearAlarmSetting",      // 35
        "SetCleanUp",             // 36

        "CheckFirmware",          // 37
        "StartCallNotification",  // 38
        "StartTextNotification",  // 39
        "DisableAllCallTextNotification", // 40
        "StopNotification"        // 41
    };
}
