package com.misfit.syncsdk.operator;

import android.support.annotation.NonNull;
import android.util.Log;

import com.misfit.syncsdk.callback.SyncOperationResultCallback;
import com.misfit.syncsdk.log.LogManager;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.task.Task;

import java.util.List;
import java.util.TimerTask;

/**
 * to iterate internal task list to execute all the tasks
 */
public class Operator implements Task.TaskResultCallback {
    private final static String TAG = "Operator";

    public List<Task> mTaskQueue;

    private int mCurrIndex;

    private TaskSharedData mTaskSharedData;

    protected TimerTask mCurrTimerTask;

    SyncOperationResultCallback mResultCallback;

    private OperatorReleaseCallback mReleaseCallback;

    public Operator(@NonNull TaskSharedData taskSharedData,
                    @NonNull List<Task> taskList,
                    @NonNull SyncOperationResultCallback resultCallback,
                    OperatorReleaseCallback releaseCallback) {
        mTaskSharedData = taskSharedData;
        mTaskQueue = taskList;
        mReleaseCallback = releaseCallback;
        mResultCallback = resultCallback;
    }

    public TaskSharedData getTaskSharedData() {
        return mTaskSharedData;
    }

    public void start() {
        Log.d(TAG, this.getClass().getSimpleName() + " start");
        mCurrIndex = -1;
        prepare();
        gotoNext();
    }

    protected void gotoNext() {
        Log.d(TAG, this.getClass().getSimpleName() + " goto next task");
        mCurrIndex++;
        if (mCurrIndex >= mTaskQueue.size()) {
            //should callback
            onFlowCompleted();
            return;
        }

        Task task = mTaskQueue.get(mCurrIndex);
        task.setCallback(this);
        task.start(mTaskSharedData);
    }

    public void stop() {
        Log.d(TAG, this.getClass().getSimpleName() + " stop");
        if (mCurrIndex >= 0 && mCurrIndex < mTaskQueue.size()) {
            mTaskQueue.get(mCurrIndex).stop();
        }
        cleanUpAndRelease();
    }

    protected void onFlowCompleted() {
        Log.d(TAG, this.getClass().getSimpleName() + " completed");
        if (mResultCallback != null) {
            mResultCallback.onSucceed();
        }
        cleanUpAndRelease();
    }

    //FIXME: use enum instead of string
    protected void onFlowEnded(String reason) {
        Log.d(TAG, this.getClass().getSimpleName() + " ended by " + reason);
        if (mResultCallback != null) {
            //FIXME:fix here
            mResultCallback.onFailed(-1);
        }
        cleanUpAndRelease();
    }

    /**
     * clean up internal resources, and invoke callback to release this Operator instance
     */
    private void cleanUpAndRelease() {
        cancelCurrentTimerTask();
        mTaskQueue.clear();
        LogManager.getInstance().uploadAllLog();
        if (mReleaseCallback != null) {
            mReleaseCallback.onOperatorRelease();
        }
        mTaskSharedData.cleanUp();
        mTaskSharedData = null;
    }

    @Override
    public void onTaskFinished() {
        gotoNext();
    }

    @Override
    public void onTaskFailed(Task task, int reason) {
        Log.d(TAG, task.getClass().getSimpleName() + " failed, reason=" + reason);
        if (task.shouldEndFlow()) {
            onFlowEnded("task wants end flow");
        } else {
            retry();
        }
    }

    private void retry() {
        Log.d(TAG, String.format("remaining retry = %d", mTaskSharedData.getRemainingRetryCount()));
        if (mTaskSharedData.consumeRetryCount() < 0) {
            onFlowEnded("retry out");
        } else {
            start();
        }
    }

    protected void prepare() {
    }

    protected void cancelCurrentTimerTask() {
        if (mCurrTimerTask != null) {
            mCurrTimerTask.cancel();
        }
    }

    public interface OperatorReleaseCallback {
        void onOperatorRelease();
    }
}
