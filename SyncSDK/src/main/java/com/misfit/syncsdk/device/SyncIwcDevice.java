package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.ConnectionParameterManager;
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
 * SyncCommonDevice for IWC(Silveratta)
 */
public class SyncIwcDevice extends SyncCommonDevice {
    public SyncIwcDevice(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.SILVERATTA;
    }

    @Override
    public void startSync(SyncOperationResultCallback resultCallback,
                          ReadDataCallback syncCallback,
                          SyncCalculationCallback calculationCallback,
                          SyncOtaCallback otaCallback,
                          @NonNull SyncSyncParams syncParams) {
        if (isRunning()) {
            resultCallback.onFailed(SyncOperationResult.RUNNING);
            return;
        }

        TaskSharedData taskSharedData = createTaskSharedData();
        taskSharedData.setReadDataCallback(syncCallback);
        taskSharedData.setSyncCalculationCallback(calculationCallback);
        taskSharedData.setSyncOtaCallback(otaCallback);
        taskSharedData.setSyncParams(syncParams);

        List<Task> syncTasks = prepareTasks();
        syncTasks.add(new CheckFirmwareTask());
        syncTasks.add(new PlayAnimationTask());
        syncTasks.add(new SetConnectionParameterTask(ConnectionParameterManager.defaultIwcParams()));
        syncTasks.add(new SyncAndCalculateTask());
        syncTasks.add(new OtaTask());
        syncTasks.add(new GetConfigurationTask());
        syncTasks.add(new SetConfigurationTask());

        SyncOperator syncOperator = new SyncOperator(taskSharedData, syncTasks, resultCallback, this);

        startOperator(syncOperator);
    }

    public boolean supportSettingsElement(SettingsElement element) {
        return element == SettingsElement.BATTERY
            || element == SettingsElement.G_FORCE
            || element == SettingsElement.BUTTON
            || element == SettingsElement.SHOW_DEVICE;
    }

    public boolean isStreamingSupported() {
       return true;
    }
}
