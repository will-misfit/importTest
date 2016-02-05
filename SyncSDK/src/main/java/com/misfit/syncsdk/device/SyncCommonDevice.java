package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;
import android.util.Log;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.callback.SyncAnimationCallback;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.model.SettingsElement;
import com.misfit.syncsdk.model.SyncSyncParams;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.operator.Operator;
import com.misfit.syncsdk.task.ConnectTask;
import com.misfit.syncsdk.task.ScanTask;
import com.misfit.syncsdk.task.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * class type to send to Misfit flagship app
 */
public class SyncCommonDevice implements DeviceBehavior, Operator.OperatorReleaseCallback{
    private final static String TAG = "SyncCommonDevice";

    protected String mSerialNumber;
    protected int mDeviceType;
    protected Operator mCurrOperator;

    protected SyncCommonDevice(@NonNull String serialNumber) {
        mSerialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    protected List<Task> prepareTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new ScanTask());
        tasks.add(new ConnectTask());
        return tasks;
    }

    protected TaskSharedData createTaskSharedData() {
        TaskSharedData taskSharedData = new TaskSharedData(getSerialNumber(), mDeviceType);
        taskSharedData.setDeviceBehavior(this);
        return taskSharedData;
    }


    public boolean isRunningOn() {
        return mCurrOperator != null;
    }

    public void startSync(SyncSyncCallback syncCallback, SyncCalculationCallback calculationCallback,
                          SyncOtaCallback otaCallback, @NonNull SyncSyncParams syncParams) {
    }

    protected void startOperator(Operator operator) {
        Log.d(TAG, "start " + operator.getClass().getSimpleName());
        mCurrOperator = operator;
        operator.start();
    }

    public void stopOperation() {
        if (mCurrOperator != null) {
            mCurrOperator.stop();
        }
    }

    //will not be public until it is completed
    /* public */ void playAnimation(SyncAnimationCallback animationCallback) {
    }

    //will not be public until it is completed
    /* public */ void startUserInputStreaming(ShineProfile.StreamingCallback callback) {
    }

    //will not be public until it is completed
    /* public */ void stopUserInputStreaming() {
    }

//    public void sendNotification(NotificationType notificationType, NotificationCallback callback) {
//    }

    //will not be public until it is completed
    /* public */ void stopNotification() {
    }

    // opt to override in subclass
    public boolean isStreamingSupported() {
        return false;
    }

    // opt to override in subclass
    public boolean supportSettingsElement(SettingsElement element) {
        return false;
    }

    public void onOperatorRelease() {
        mCurrOperator = null;
    }
}
