package com.misfit.syncsdk.model;

import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;

/**
 * Created by Will Hou on 2/2/16.
 */
public class SyncSyncParams {

    public boolean firstSync;

    public boolean shouldClearAlarmSettings;
    public AlarmSettings alarmSettings;
    public InactivityNudgeSettings inactivityNudgeSettings;
    public NotificationsSettings notificationsSettings;
}
