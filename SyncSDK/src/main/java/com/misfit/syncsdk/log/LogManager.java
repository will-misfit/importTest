package com.misfit.syncsdk.log;

/**
 * Created by Will Hou on 1/29/16.
 */
public class LogManager {
    public static LogManager getInstance() {
        return null;
    }

    public void uploadAllLogs() {
        //TODO:get sessions
        //TODO:get events which belong to session
        //TODO:upload the session(accept multiple times?)
        //TODO:upload the events
    }

    void appendEvent(String seesionId, LogEvent event) {
        //TODO:throw to another thread
        //TODO:write event into the file
    }

    void uploadLog(String sessionId) {

    }
}
