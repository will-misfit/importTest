package com.misfit.syncsdk.model;

import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.callback.SyncOnTagInStateListener;

import java.util.List;

/**
 * a model class composed of
 * 1. some customized settings of devices
 * 2. some callback for specified devices
 */
public class SyncParams {

    public boolean firstSync;

    // last successful sync timestamp in sec
    // TODO:whether dependent on serial number is TBD
    public long lastSyncTime;

    // the priority of shouldClearAlarmSettings is higher than alarmSettings
    public boolean shouldClearAlarmSettings;

    public AlarmSettings alarmSettings;

    public InactivityNudgeSettings inactivityNudgeSettings;

    public NotificationsSettings notificationsSettings;

    public GoalHitNotificationSettings goalHitNotificationSettings;

    public SyncOnTagInStateListener tagInStateListener;

    public String userId;

    public String appVersion;

    /**
     * StreamingCallback is unnecessary for each device type, so it is better to be in SyncParams
     * than in public API(startSync() ) as parameter
     * */
    public ShineProfile.StreamingCallback streamingCallback;

    /**
     * Settings table rows from App database since last successful sync time
     * 1.the items should be listed in ascending order of timestamp
     * 2. the tail item should be the current Settings
     */
    public List<SdkResourceSettings> settingsChangeListSinceLastSync;

    /**
     * current user profile
     * */
    public SdkProfile userProfile;

    /**
     * ShineConfiguration from App invoker to update in Device
     * */
    public ShineConfiguration shineConfiguration;
}
