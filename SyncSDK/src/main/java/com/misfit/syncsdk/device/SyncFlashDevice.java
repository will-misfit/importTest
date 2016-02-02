package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.model.SettingsElement;
import com.misfit.syncsdk.model.SyncSyncParams;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.operator.SyncOperator;
import com.misfit.syncsdk.task.ActivateTask;
import com.misfit.syncsdk.task.CheckFirmwareTask;
import com.misfit.syncsdk.task.DisconnectTask;
import com.misfit.syncsdk.task.GetConfigurationTask;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.task.PlayAnimationTask;
import com.misfit.syncsdk.task.SetConfigurationTask;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;

import java.util.List;

/**
 * Created by Will Hou on 1/13/16.
 */
public class SyncFlashDevice extends SyncCommonDevice {
    public SyncFlashDevice(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.FLASH;
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

        SyncAndCalculateTask syncAndCalculateTask = new SyncAndCalculateTask();
        List<Task> tasks = prepareTasks();
        tasks.add(new CheckFirmwareTask());
        tasks.add(new PlayAnimationTask());
        if (syncParams.firstSync) {
            tasks.add(new ActivateTask());
        }
        tasks.add(new GetConfigurationTask());
        //TODO:CheckTaggingStatusTask
        tasks.add(syncAndCalculateTask);
        tasks.add(new OtaTask());
        tasks.add(new SetConfigurationTask());
        tasks.add(new DisconnectTask());

        SyncOperator syncOperator = new SyncOperator(taskSharedData, tasks);

        startOperator(syncOperator);
    }

    @Override
    public boolean supportSettingsElement(SettingsElement element) {
        return element == SettingsElement.BATTERY
                || element == SettingsElement.WEARING_POSITION
                || element == SettingsElement.ACTIVITY_TAGGING
                || element == SettingsElement.BUTTON
                || element == SettingsElement.CLOCK
                || element == SettingsElement.SERIAL_NUMBER
                || element == SettingsElement.SHOW_DEVICE;
    }
}
