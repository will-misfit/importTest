package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.SyncOperationResult;
import com.misfit.syncsdk.callback.ReadDataCallback;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.callback.SyncOperationResultCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.model.SettingsElement;
import com.misfit.syncsdk.model.SyncSyncParams;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.operator.SyncOperator;
import com.misfit.syncsdk.task.ActivateTask;
import com.misfit.syncsdk.task.CheckFirmwareTask;
import com.misfit.syncsdk.task.CheckOnTagStatusTaskUserInput;
import com.misfit.syncsdk.task.DisconnectTask;
import com.misfit.syncsdk.task.GetConfigurationTask;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.task.PlayAnimationTask;
import com.misfit.syncsdk.task.SetConfigurationTask;
import com.misfit.syncsdk.task.SwitchTrackerModeTask;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;
import com.misfit.syncsdk.task.UnmapEventAnimation;

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
    public void startSync(@NonNull SyncOperationResultCallback resultCallback, ReadDataCallback syncCallback, SyncCalculationCallback calcuCallback,
                          SyncOtaCallback otaCallback, @NonNull SyncSyncParams syncParams) {
        if (isRunning()) {
            resultCallback.onFailed(SyncOperationResult.RUNNING);
            return;
        }

        TaskSharedData taskSharedData = createTaskSharedData();
        taskSharedData.setReadDataCallback(syncCallback);
        taskSharedData.setSyncCalculationCallback(calcuCallback);
        taskSharedData.setSyncOtaCallback(otaCallback);
        taskSharedData.setSyncParams(syncParams);

        List<Task> tasks = prepareTasks();
        tasks.add(new CheckFirmwareTask());
        tasks.add(new PlayAnimationTask());
        if (syncParams.firstSync) {
            tasks.add(new ActivateTask());
            tasks.add(new SwitchTrackerModeTask());
            tasks.add(new UnmapEventAnimation());
        }
        tasks.add(new GetConfigurationTask());
        tasks.add(new CheckOnTagStatusTaskUserInput());
        tasks.add(new SyncAndCalculateTask());
        tasks.add(new OtaTask());
        tasks.add(new SetConfigurationTask());
        tasks.add(new DisconnectTask());

        SyncOperator syncOperator = new SyncOperator(taskSharedData, tasks, resultCallback, this);

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
