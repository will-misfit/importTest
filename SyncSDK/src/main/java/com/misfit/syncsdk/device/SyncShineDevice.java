package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.ConnectionParameterManager;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.model.SettingsElement;
import com.misfit.syncsdk.model.SyncSyncParams;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.operator.SyncOperator;
import com.misfit.syncsdk.task.GetConfigurationTask;
import com.misfit.syncsdk.task.PlayAnimationTask;
import com.misfit.syncsdk.task.SetConfigurationTask;
import com.misfit.syncsdk.task.SetConnectionParameterTask;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;

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
    public void startSync(@NonNull SyncSyncParams syncParams, SyncSyncCallback syncCallback, SyncCalculationCallback calcuCallback, SyncOtaCallback otaCallback) {
        if (isRunningOn()) {
            return;
        }

        TaskSharedData taskSharedData = createTaskSharedData();
        taskSharedData.setSyncCalculationCallback(calcuCallback);
        taskSharedData.setSyncOtaCallback(otaCallback);
        taskSharedData.setSyncParams(syncParams);

        List<Task> syncTasks = prepareTasks();
        syncTasks.add(new PlayAnimationTask());
        syncTasks.add(new SetConnectionParameterTask(ConnectionParameterManager.defaultParams()));
        syncTasks.add(new SyncAndCalculateTask());
        syncTasks.add(new GetConfigurationTask());
        syncTasks.add(new SetConfigurationTask());  //TODO:where to get configuration: from context

        SyncOperator syncOperator = new SyncOperator(taskSharedData, syncTasks);

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
