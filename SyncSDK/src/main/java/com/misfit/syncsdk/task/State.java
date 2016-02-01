package com.misfit.syncsdk.task;

import com.misfit.syncsdk.TimerManager;

import java.util.TimerTask;

/**
 * Would be use in state pattern
 */
public abstract class State {

    protected TimerTask mCurrTimeoutTask;

    protected void setNewTimeOutTask(TimerTask timerTask, long delayMills){
        cancelCurrentTimeoutTask();
        mCurrTimeoutTask = timerTask;
        TimerManager.getInstance().addTimerTask(mCurrTimeoutTask, delayMills);
    }

    protected void cancelCurrentTimeoutTask() {
        if (mCurrTimeoutTask != null) {
            mCurrTimeoutTask.cancel();
            mCurrTimeoutTask = null;
        }
    }

    abstract void execute();

    abstract void stop();
}
