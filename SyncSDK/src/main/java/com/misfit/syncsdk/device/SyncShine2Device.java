package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.ConnectionParameterManager;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.SyncOperationResult;
import com.misfit.syncsdk.callback.ReadDataCallback;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.callback.SyncOperationResultCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.callback.UserTokenRequest;
import com.misfit.syncsdk.model.SettingsElement;
import com.misfit.syncsdk.model.SyncSyncParams;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.operator.SyncOperator;
import com.misfit.syncsdk.task.CheckFirmwareTask;
import com.misfit.syncsdk.task.DisconnectTask;
import com.misfit.syncsdk.task.GetConfigurationTask;
import com.misfit.syncsdk.task.OtaTask;
import com.misfit.syncsdk.task.PlayAnimationTask;
import com.misfit.syncsdk.task.SetAlarmTask;
import com.misfit.syncsdk.task.SetConfigurationTask;
import com.misfit.syncsdk.task.SetConnectionParameterTask;
import com.misfit.syncsdk.task.SetInactivityNudgeTask;
import com.misfit.syncsdk.task.SetNotificationTask;
import com.misfit.syncsdk.task.StopAnimationTask;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;

import java.util.List;

/**
 * Created by Will Hou on 1/13/16.
 */
public class SyncShine2Device extends SyncCommonDevice {
    public SyncShine2Device(@NonNull String serialNumber) {
        super(serialNumber);
        mDeviceType = DeviceType.PLUTO;
    }

    @Override
    public void startSync(SyncOperationResultCallback resultCallback,
                          ReadDataCallback syncCallback,
                          SyncCalculationCallback calculationCallback,
                          SyncOtaCallback otaCallback,
                          UserTokenRequest userTokenRequest,
                          @NonNull SyncSyncParams syncParams) {
        if (isRunning()) {
            resultCallback.onFailed(SyncOperationResult.RUNNING);
            return;
        }

        TaskSharedData taskSharedData = createTaskSharedData();
        taskSharedData.setReadDataCallback(syncCallback);
        taskSharedData.setSyncCalculationCallback(calculationCallback);
        taskSharedData.setSyncOtaCallback(otaCallback);
        FirmwareManager.getInstance().setUserTokenRequest(userTokenRequest);
        taskSharedData.setSyncParams(syncParams);

        List<Task> tasks = prepareTasks();
        tasks.add(new CheckFirmwareTask());
        tasks.add(new PlayAnimationTask());
        tasks.add(new StopAnimationTask());
        tasks.add(new SetConnectionParameterTask(ConnectionParameterManager.defaultShine2Params()));
        tasks.add(new SyncAndCalculateTask());
        tasks.add(new OtaTask());
        tasks.add(new GetConfigurationTask());
        tasks.add(new SetConfigurationTask());
        tasks.add(new SetAlarmTask());
        tasks.add(new SetNotificationTask());
        tasks.add(new SetInactivityNudgeTask());
        tasks.add(new DisconnectTask());

        SyncOperator syncOperator = new SyncOperator(taskSharedData, tasks, resultCallback, this);

        startOperator(syncOperator);
    }

    @Override
    public boolean supportSettingsElement(SettingsElement element) {
        return element == SettingsElement.BATTERY
                || element == SettingsElement.WEARING_POSITION
                || element == SettingsElement.MOVE
                || element == SettingsElement.ALARM
                || element == SettingsElement.NOTIFICATION
                || element == SettingsElement.BUTTON
                || element == SettingsElement.CLOCK
                || element == SettingsElement.SERIAL_NUMBER
                || element == SettingsElement.SHOW_DEVICE;
    }
}
