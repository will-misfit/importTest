package com.misfit.syncsdk.task;

import android.util.Log;

import com.misfit.syncsdk.enums.FailedReason;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;
import com.misfit.syncsdk.log.LogSession;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.utils.MLog;

import java.util.TimerTask;

/**
 * base class for all the detailed task in operation
 */
public abstract class Task {
    private final static String TAG = "Task";

    public interface TaskResultCallback {
        void onTaskFinished();

        void onTaskFailed(Task task, int reason);
    }

    protected TaskResultCallback mTaskResultCallback;

    // subclass need to define its own remaining retry count
    protected int mRemainingRetry = 0;

    protected TaskSharedData mTaskSharedData;
    protected boolean mIsFinished = false;

    protected LogEvent mLogEvent;
    protected LogSession mLogSession;

    // Timer to monitor whether the Task execution timeout
    protected TimerTask mCurrTimerTask;

    public boolean shouldEndFlow() {
        return false;
    }

    public void start(TaskSharedData sharedData) {
        mTaskSharedData = sharedData;
        mIsFinished = false;
        Log.d(TAG, this.getClass().getSimpleName() + " start");
        mLogSession = mTaskSharedData.getLogSession();
        prepare();
        execute();
    }

    public void stop() {
        mIsFinished = true;
        onStop();
        cleanup();
    }

    protected void retry() {
        mRemainingRetry--;
        Log.d(TAG, this.getClass().getSimpleName() + " retry, remaining retry=" + mRemainingRetry);
        if (mRemainingRetry <= 0) {
            taskFailed("retry out");
        } else {
            cleanup();
            start(mTaskSharedData);
        }
    }

    protected boolean canbeIgnored(){
        return false;
    }

    protected void retryAndIgnored() {
        mRemainingRetry--;
        Log.d(TAG, this.getClass().getSimpleName() + " retry, remaining retry=" + mRemainingRetry);
        if (mRemainingRetry <= 0) {
            taskIgnored("retry out");
        } else {
            cleanup();
            start(mTaskSharedData);
        }
    }

    protected void taskFailed(String reason) {
        mIsFinished = true;
        cleanup();
        Log.d(TAG, this.getClass().getSimpleName() + "ended by " + reason);
        if (mTaskResultCallback != null) {
            //FIXME:use enum
            mTaskResultCallback.onTaskFailed(this, -1);
        }
    }

    protected void taskSucceed() {
        mIsFinished = true;
        cleanup();
        Log.d(TAG, this.getClass().getSimpleName() + " completed");
        if (mTaskResultCallback != null) {
            mTaskResultCallback.onTaskFinished();
        }
    }

    protected void taskIgnored(String reason) {
        mIsFinished = true;
        cleanup();
        Log.d(TAG, this.getClass().getSimpleName() + " was skipped by" + reason);
        if (mTaskResultCallback != null) {
            mTaskResultCallback.onTaskFinished();
        }
    }

    /**
     * methods to be called in start()
     */
    protected abstract void prepare();

    protected abstract void execute();

    protected abstract void onStop();

    /**
     * to be called in succeed/ failed/ stop/ retry
     */
    protected abstract void cleanup();

    // default TimerTask to monitor in each Task subclass, no retry
    protected TimerTask createTimeoutTask() {
        return new TimerTask() {
            @Override
            public void run() {
                MLog.d(TAG, "Task instance timeout timer ticks!");
                mTaskSharedData.setFailureReasonInLogSession(FailedReason.TIMEOUT);
                taskFailed("timeout");
            }
        };
    }

    public void setCallback(TaskResultCallback mCallback) {
        this.mTaskResultCallback = mCallback;
    }

    protected void cancelCurrentTimerTask() {
        if (mCurrTimerTask != null) {
            mCurrTimerTask.cancel();
            mCurrTimerTask = null;
        }
    }

}
