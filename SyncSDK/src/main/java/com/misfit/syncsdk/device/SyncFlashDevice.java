package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.ConnectionParameterManager;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.SyncOperationResult;
import com.misfit.syncsdk.callback.ConnectionStateCallback;
import com.misfit.syncsdk.callback.ReadDataCallback;
import com.misfit.syncsdk.callback.SyncOperationResultCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.model.SettingsElement;
import com.misfit.syncsdk.model.SyncParams;
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
import com.misfit.syncsdk.task.SetConnectionParameterTask;
import com.misfit.syncsdk.task.StartUserInputStreamingTask;
import com.misfit.syncsdk.task.SwitchTrackerModeTask;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;
import com.misfit.syncsdk.task.UnmapEventAnimationTask;

import java.util.List;

/**
 * SyncCommonDevice for Flash
 *
 * as SyncCommonDevice instance is created before model name is known in connect task,
 * SyncFlashDevice supports Device type update to FlashLink after connect
 */
public class SyncFlashDevice extends SyncCommonDevice {
    public SyncFlashDevice(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.FLASH;
    }

    @Override
    public void startSync(SyncOperationResultCallback resultCallback,
                          ReadDataCallback syncCallback,
                          SyncOtaCallback otaCallback,
                          ConnectionStateCallback postSyncConnectionStateCallback,
                          @NonNull SyncParams syncParams) {
        if (isRunning()) {
            resultCallback.onFailed(SyncOperationResult.RUNNING);
            return;
        }
        setPostSyncConnectionStateCallback(postSyncConnectionStateCallback);

        TaskSharedData taskSharedData = createTaskSharedData();
        taskSharedData.setReadDataCallback(syncCallback);
        taskSharedData.setSyncOtaCallback(otaCallback);
        taskSharedData.setSyncParams(syncParams);

        List<Task> tasks = prepareTasks();
        tasks.add(new CheckFirmwareTask());
        tasks.add(new PlayAnimationTask());
        if (syncParams.firstSync) {
            tasks.add(new ActivateTask());
        }
        tasks.add(new SwitchTrackerModeTask());   // only works for Flash Button
        tasks.add(new UnmapEventAnimationTask()); // only works for Flash Button

        tasks.add(new GetConfigurationTask());
        tasks.add(new CheckOnTagStatusTaskUserInput());
        tasks.add(new SetConnectionParameterTask(ConnectionParameterManager.DefaultParams));
        tasks.add(new SyncAndCalculateTask());
        tasks.add(new OtaTask());
        tasks.add(new GetConfigurationTask());
        tasks.add(new SetConfigurationTask());

        tasks.add(new SetConnectionParameterTask(ConnectionParameterManager.SlowConnectionParams)); // only works for Flash Button
        tasks.add(new StartUserInputStreamingTask());  // only works for Flash Button
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
