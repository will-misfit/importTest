package com.misfit.syncsdk.log;

public enum LogEventType {
    /* constant Log Event ID */
    Default(0),
    StartScanning(1),
    ScannedDevice(2),
    StopScanning(3),

    Connect(4),
    GetCachedDevice(5),
    GetConnectedDevice(6),

    DiscoverCharacteristics(7),  // for iOS
    Prepare(8),            // for iOS
    HandShake(9),

    SwitchTrackerMode(10),
    MapEventAnimation(11),
    UnmapEventAnimation(12),

    SetConnectionParameter(13),
    GetConnectionParameter(14),
    PlayAnimation(15),
    StopAnimation(16),
    SetConfiguration(17),
    GetConfiguration(18),

    FileList(19),

    GetActivity(20),
    GetHardwareLog(21),

    Disconnect(22),
    Calculate(23),

    StartFileStreaming(24),
    StopFileStreaming(25),

    SetAlarm(26),
    SetInactiveNudges(27),
    SetCallTextNotification(28),
    SetGoalMetNotification(29),

    Ota(30),
    PlaySyncAnimation(31),
    Activation(32),
    StopRunningOperation(33),
    SetFileStreamingParameters(34),
    ClearAlarmSetting(35),
    SetCleanUp(36),

    CheckFirmware(37),
    StartCallNotification(38),
    StartTextNotification(39),
    DisableAllCallTextNotification(40),
    StopNotification(41),

    UnexpectedConnectionState(100);

    private int mId;

    LogEventType(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }
}
