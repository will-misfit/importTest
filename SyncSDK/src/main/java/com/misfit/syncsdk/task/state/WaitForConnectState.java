package com.misfit.syncsdk.task.state;

import com.misfit.syncsdk.task.OtaTask;

import java.util.TimerTask;

/**
 * wait for a while before reconnect
 * */
public class WaitForConnectState extends State {
    private final static int DELAY_BEFORE_CONNECT = 5 * 1000; // in milli sec

    private OtaTask otaTask;

    public WaitForConnectState(OtaTask otaTask) {
        this.otaTask = otaTask;
    }

    @Override
    public void execute() {
        setNewTimeOutTask(new TimerTask() {
            @Override
            public void run() {
                otaTask.gotoState(new ReconnectState(otaTask));
            }
        }, DELAY_BEFORE_CONNECT);
    }

    @Override
    public void stop() {
        cancelCurrentTimeoutTask();
    }
}
