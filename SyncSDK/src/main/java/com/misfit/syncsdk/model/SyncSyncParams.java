package com.misfit.syncsdk.model;

import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.syncsdk.callback.SyncOnTagInStateListener;

/**
 * a model class composed of some customized settings of device, e.g. Shine2
 */
// FIXME: as SyncSDK hide ShineSDK to App, do we need to expose ShineSDK class as parameter to App?
// Answer: the SyncSDK is the ShineSDK, they are the same.
public class SyncSyncParams {

    public boolean firstSync;

    public boolean shouldClearAlarmSettings;

    public AlarmSettings alarmSettings;

    public InactivityNudgeSettings inactivityNudgeSettings;

    public NotificationsSettings notificationsSettings;

    public SyncOnTagInStateListener tagInStateListener;
}
