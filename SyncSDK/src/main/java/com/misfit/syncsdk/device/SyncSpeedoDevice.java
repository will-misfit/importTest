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
import com.misfit.syncsdk.task.CheckFirmwareTask;
import com.misfit.syncsdk.task.GetConfigurationTask;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.task.PlayAnimationTask;
import com.misfit.syncsdk.task.SetConfigurationTask;
import com.misfit.syncsdk.task.SetConnectionParameterTask;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;

import java.util.List;

/**
 * SyncCommonDevice subclass for Speedo Shine
 * NOTE: currently, its operation is totally the same with SyncShineDevice
 */
public class SyncSpeedoDevice extends SyncCommonDevice {

    public SyncSpeedoDevice(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.SPEEDO_SHINE;
    }

    /**
     * currently SyncCommonDevice supports only one Operator running
     */
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

        List<Task> syncTasks = prepareTasks();
        syncTasks.add(new CheckFirmwareTask());
        syncTasks.add(new PlayAnimationTask());
        syncTasks.add(new SetConnectionParameterTask(ConnectionParameterManager.DefaultParams));
        syncTasks.add(new SyncAndCalculateTask());
        syncTasks.add(new OtaTask());
        syncTasks.add(new GetConfigurationTask());
        syncTasks.add(new SetConfigurationTask());

        SyncOperator syncOperator = new SyncOperator(taskSharedData, syncTasks, resultCallback, this);

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
