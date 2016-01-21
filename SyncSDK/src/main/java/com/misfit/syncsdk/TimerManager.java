package com.misfit.syncsdk;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public class TimerManager {

    private Timer mTimer = new Timer("SyncSDKTimer");

    private static TimerManager sharedInstance;

    private TimerManager() {
    }

    public static TimerManager getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new TimerManager();
        }
        return sharedInstance;
    }

    public void addTimerTask(TimerTask timerTask, long delayMilSecond) {
        if (timerTask != null) {
            mTimer.schedule(timerTask, delayMilSecond);
        }
    }
}
