package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.ConnectionParameterManager;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.operator.SyncOperator;
import com.misfit.syncsdk.task.DisconnectTask;
import com.misfit.syncsdk.task.GetConfigurationTask;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.task.PlayAnimationTask;
import com.misfit.syncsdk.task.SetConnectionParameterTask;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;

import java.util.List;

/**
 * Created by Will Hou on 1/13/16.
 */
public class SyncSwarovskiDevice extends SyncCommonDevice {
    public SyncSwarovskiDevice(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.SWAROVSKI_SHINE;
    }

    @Override
    public void startSync(boolean firstSync, SyncSyncCallback syncCallback, SyncOtaCallback otaCallback) {
        if (isRunningOn()) {
            return;
        }
        updateAnimationCallback(syncCallback);

        SyncAndCalculateTask syncAndCalculateTask = new SyncAndCalculateTask();

        TaskSharedData taskSharedData = createTaskSharedData();
        taskSharedData.setSyncSyncCallback(syncCallback);
        taskSharedData.setSyncOtaCallback(otaCallback);

        List<Task> tasks = prepareTasks();
        tasks.add(new PlayAnimationTask());
        tasks.add(new SetConnectionParameterTask(ConnectionParameterManager.defaultParams()));
        tasks.add(new GetConfigurationTask());
        tasks.add(syncAndCalculateTask);
        tasks.add(new OtaTask());
        tasks.add(new DisconnectTask());

        SyncOperator syncOperator = new SyncOperator(taskSharedData, tasks);
        syncAndCalculateTask.setSyncAndCalculationTaskCallback(syncOperator);

        startOperator(syncOperator);
    }
}
