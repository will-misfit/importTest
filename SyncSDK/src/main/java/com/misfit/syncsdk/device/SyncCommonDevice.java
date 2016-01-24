package com.misfit.syncsdk.device;

import android.support.annotation.NonNull;
import android.util.Log;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.callback.SyncAnimationCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.model.SettingsElement;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.operator.Operator;
import com.misfit.syncsdk.task.ConnectTask;
import com.misfit.syncsdk.task.PlayAnimationTask;
import com.misfit.syncsdk.task.ScanTask;
import com.misfit.syncsdk.task.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * class type to send to Misfit flagship app
 */
public class SyncCommonDevice implements DeviceBehavior{
    private final static String TAG = "SyncCommonDevice";
    private String mSerialNumber;
    protected int mDeviceType;

    protected Operator mCurrOperator;

    protected TaskSharedData mTaskSharedData;

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

    public boolean isRunningOn() {
        if (mCurrOperator != null && mTaskSharedData.isTasksRunning()) {
            return true;
        } else {
            return false;
        }
    }

    protected TaskSharedData createTaskSharedData(){
        TaskSharedData sharedData = new TaskSharedData(getSerialNumber(), mDeviceType, this);
        return sharedData;
    }

    public void startSync(boolean firstSync, SyncSyncCallback syncCallback, SyncOtaCallback otaCallback) {
    }

    protected void updateAnimationCallback(SyncSyncCallback syncCallback) {
        if (mTaskSharedData == null) {
            mTaskSharedData = new TaskSharedData(mSerialNumber, mDeviceType, this);
        }
        mTaskSharedData.setSyncSyncCallback(syncCallback);
    }

    protected void updateAnimationCallback(SyncAnimationCallback animationCallback) {
        if (mTaskSharedData == null) {
            mTaskSharedData = new TaskSharedData(mSerialNumber, mDeviceType, this);
        }
        mTaskSharedData.setSyncAnimationCallback(animationCallback);
    }

    protected void startOperator(Operator operator) {
        Log.d(TAG, "start " + operator.getClass().getSimpleName());
        mCurrOperator = operator;
        operator.start();
    }

    // TODO: this needs to be called when entire sync/playAnimation operation completes
    protected void cleanUpCallbacks() {
        if (mTaskSharedData == null) {
            mTaskSharedData.setSyncSyncCallback(null);
            mTaskSharedData.setSyncAnimationCallback(null);
            mTaskSharedData.setConfigurationSession(null);
        }
    }

    public void stopOperation() {
        if (mCurrOperator != null) {
            mCurrOperator.stop();
        }
    }

    public void playAnimation(SyncAnimationCallback animationCallback) {
        if(isRunningOn()){
            Log.d(TAG, "call playAnimation but during operation");
            return;
        }
        updateAnimationCallback(animationCallback);

        List<Task> tasks = prepareTasks();
        tasks.add(new PlayAnimationTask());

        Operator operator = new Operator(mTaskSharedData, tasks);
        startOperator(operator);
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
}
