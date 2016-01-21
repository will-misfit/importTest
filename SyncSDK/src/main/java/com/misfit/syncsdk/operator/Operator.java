package com.misfit.syncsdk.operator;

import android.support.annotation.NonNull;
import android.util.Log;

import com.misfit.syncsdk.callback.SyncSyncCallback;
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

    private boolean mRunningOn = false;

    private TaskSharedData mTaskSharedData;
    protected TimerTask mCurrTimerTask;
    SyncOperationResultCallback mCallback;

    public Operator(@NonNull TaskSharedData taskSharedData, @NonNull List<Task> taskList) {
        mTaskSharedData = taskSharedData;
        mTaskQueue = taskList;
    }

    public boolean isRunningOn() {
        return mRunningOn;
    }

    public void start() {
        Log.d(TAG, this.getClass().getSimpleName() + " start");
        mRunningOn = true;
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
        mRunningOn = false;
        if (mCurrIndex >= 0 && mCurrIndex < mTaskQueue.size()) {
            mTaskQueue.get(mCurrIndex).stop();
        }
        cleanUp();
    }

    protected void onFlowCompleted() {
        Log.d(TAG, this.getClass().getSimpleName() + " completed");
        mRunningOn = false;
        if (mCallback != null) {
            mCallback.onFinished();
        }
        cleanUp();
    }

    //FIXME: use enum instead of string
    protected void onFlowEnded(String reason) {
        Log.d(TAG, this.getClass().getSimpleName() + " ended by+" + reason);
        mRunningOn = false;
        if (mCallback != null) {
            //FIXME:fix here
            mCallback.onFailed(-1);
        }
        cleanUp();
    }

    private void cleanUp() {
        cancelCurrentTimerTask();
        mTaskSharedData = null;
        mTaskQueue.clear();
    }

    protected void submitLog() {
    }

    @Override
    public void onTaskFinished() {
        gotoNext();
    }

    @Override
    public void onTaskFailed(Task task, int reason) {
        Log.d(TAG, task.getClass().getSimpleName() + "failed, reason=" + reason + ", shouldIgnore=" + task.couldIgnoreResult() + ", shouldEndFlow=" + task.shouldEndFlow());
        if (task.couldIgnoreResult()) {
            Log.d(TAG, "ignore result");
            gotoNext();
        } else if (task.shouldEndFlow()) {
            onFlowEnded("task wants end flow");
        } else {
            retry();
        }
    }

    private void retry() {
        Log.d(TAG, String.format("%s retry, remaining retry = %d", mTaskSharedData.getRemainingRetryCount()));
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
}
