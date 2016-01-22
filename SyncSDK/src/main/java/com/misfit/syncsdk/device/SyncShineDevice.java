package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.ConnectionParameterManager;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.operator.SyncOperator;
import com.misfit.syncsdk.task.GetConfigurationTask;
import com.misfit.syncsdk.task.PlayAnimationTask;
import com.misfit.syncsdk.task.SetConfigurationTask;
import com.misfit.syncsdk.task.SetConnectionParameterTask;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;
import com.misfit.syncsdk.model.SettingsElement;

import java.util.List;

/**
 * SyncCommonDevice child class for Shine device
 */
public class SyncShineDevice extends SyncCommonDevice {
    public SyncShineDevice(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.SHINE;
    }

    @Override
    public void startSync(boolean firstSync, SyncSyncCallback syncCallback, SyncOtaCallback otaCallback) {
        if (isRunningOn()) {
            return;
        }

        updateAnimationCallback(syncCallback);

        SyncAndCalculateTask syncAndCalculateTask = new SyncAndCalculateTask();
        List<Task> syncTasks = prepareTasks();
        syncTasks.add(new PlayAnimationTask());
        syncTasks.add(new SetConnectionParameterTask(ConnectionParameterManager.defaultParams()));
        syncTasks.add(new GetConfigurationTask());
        syncTasks.add(syncAndCalculateTask);
        syncTasks.add(new SetConfigurationTask());  //TODO:where to get configuration: from context

        SyncOperator syncOperator = new SyncOperator(mTaskSharedData, syncTasks);
        syncAndCalculateTask.setSyncAndCalculationTaskCallback(syncOperator);

        startOperator(syncOperator);
    }

    @Override
    public boolean supportSettingsElement(SettingsElement element) {
        return element == SettingsElement.BATTERY
                || element == SettingsElement.WEARING_POSITION
                || element == SettingsElement.ACTIVITY_TAGGING
                || element == SettingsElement.SLEEP_TAGGING
                || element == SettingsElement.SWIMMING
                || element == SettingsElement.CLOCK
                || element == SettingsElement.SERIAL_NUMBER
                || element == SettingsElement.SHOW_DEVICE;
    }
}
