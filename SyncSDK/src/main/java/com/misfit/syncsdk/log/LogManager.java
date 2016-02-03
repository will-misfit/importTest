package com.misfit.syncsdk.log;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.misfit.syncsdk.model.BaseResponse;
import com.misfit.syncsdk.network.APIClient;
import com.misfit.syncsdk.utils.ContextUtils;
import com.misfit.syncsdk.utils.LocalFileUtils;
import com.misfit.syncsdk.utils.MLog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

public class LogManager {

    private final static String TAG = "LogManager";


    //FIXME:when close this thread?
    private HandlerThread mHandlerThread;
    private LogHandler mHandler;

    private static LogManager mInstance;

    private LogManager() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        mHandlerThread = new HandlerThread("sync_sdk_log");
        mHandlerThread.start();
        mHandler = new LogHandler(mHandlerThread.getLooper(), gson);
    }

    public static LogManager getInstance() {
        if (mInstance == null) {
            mInstance = new LogManager();
        }
        return mInstance;
    }

    /**
     * should be called in device reboot
     */
    public void uploadAllLogs() {
        //TODO:get all file with session/event prefix
        //TODO:convert to session/event
        //TODO:upload sessions(accept multiple times?)
        //TODO:upload events
    }

    void saveSession(LogSession session) {
        mHandler.saveSession(session);
    }

    void appendEvent(LogEvent event) {
        mHandler.saveEvent(event);
    }

    void uploadLog(String sessionId) {
        mHandler.uploadSession(sessionId);
        mHandler.uploadEvents(sessionId);
    }

    private static class LogHandler extends Handler {
        private final static String TAG = "LogHandler";

        private final static String EVENTS_PREFIX = "events_";
        private final static String SESSION_PREFIX = "session_";
        private final static int SPLIT = ',';
        private final static String SPLITSTR = ",";

        private final static int REQ_SAVE_SESSION = 1;
        private final static int REQ_SAVE_EVENT = 2;
        private final static int REQ_UPLOAD_SESSION = 3;
        private final static int REQ_UPLOAD_EVENTS = 4;
        private final static int REQ_UPLOAD_ALL = 5;

        private Gson mGson;
        APIClient.LogAPI mAPI;

        public LogHandler(Looper looper, Gson gson) {
            super(looper);
            mGson = gson;
            mAPI = APIClient.getInstance().getLogAPI();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String sessionId;
            switch (msg.what) {
                case REQ_SAVE_SESSION:
                    writeSessionToFile((LogSession) msg.obj);
                    break;
                case REQ_SAVE_EVENT:
                    writeEventToFile((LogEvent) msg.obj);
                    break;
                case REQ_UPLOAD_SESSION:
                    sessionId = (String) msg.obj;
                    if (uploadSessionToServer(sessionId) == false) {
                        LocalFileUtils.delete(SESSION_PREFIX + sessionId);
                    }
                    break;
                case REQ_UPLOAD_EVENTS:
                    sessionId = (String) msg.obj;
                    if (uploadEventsToServer(sessionId) == false) {
                        LocalFileUtils.delete(EVENTS_PREFIX + sessionId);
                    }
                    break;
                case REQ_UPLOAD_ALL:
                    //TODO:wait to finish...
                    break;
                default:
                    MLog.d(TAG, "unexpected message, what=" + msg.what);
            }
            MLog.d(TAG, "handle message finished");
        }

        public void saveSession(LogSession session) {
            obtainMessage(REQ_SAVE_SESSION, session).sendToTarget();
        }

        public void saveEvent(LogEvent event) {
            obtainMessage(REQ_SAVE_EVENT, event).sendToTarget();
        }

        public void uploadSession(String sessionId) {
            obtainMessage(REQ_UPLOAD_SESSION, sessionId).sendToTarget();
        }

        public void uploadEvents(String sessionId) {
            obtainMessage(REQ_UPLOAD_EVENTS, sessionId).sendToTarget();
        }

        /**
         * @param sessionId the target's sessionId
         * @return true if upload started, false for else
         */
        private boolean uploadSessionToServer(String sessionId) {
            String fileName = SESSION_PREFIX + sessionId;
            byte[] sessionBytes = LocalFileUtils.read(fileName);
            if (sessionBytes == null || sessionBytes.length == 0) {
                return false;
            }
            String sessionStr = new String(sessionBytes);
            LogSession session;
            try {
                session = mGson.fromJson(sessionStr, LogSession.class);
                mAPI.uploadSession(session).enqueue(new LogUploadCallback(fileName));
                return true;
            } catch (Exception ex) {
                MLog.d(TAG, Log.getStackTraceString(ex.getCause()));
                return false;
            }
        }

        /**
         * @param sessionId the target's sessionId
         * @return true if upload started, false for else
         */
        private boolean uploadEventsToServer(String sessionId) {
            String fileName = EVENTS_PREFIX + sessionId;
            byte[] eventBytes = LocalFileUtils.read(fileName);
            if (eventBytes == null || eventBytes.length == 0) {
                return false;
            }
            String[] eventStrings = new String(eventBytes).split(SPLITSTR);
            if (eventStrings.length == 0) {
                return false;
            }

            List<LogEvent> eventList = new ArrayList<>();
            for (String eventStr : eventStrings) {
                LogEvent event;
                try {
                    event = mGson.fromJson(eventStr, LogEvent.class);
                } catch (Exception ex) {
                    event = null;
                }
                if (event != null) {
                    eventList.add(event);
                } else {
                    MLog.d(TAG, "failed to convert string to LogEvent, string=" + eventStr);
                }
            }
            if (eventList.size() <= 0) {
                MLog.d(TAG, "no log will be uploaded");
                return false;
            }
            mAPI.uploadEvents(sessionId, eventList).enqueue(new LogUploadCallback(fileName));
            return true;
        }

        private void writeEventToFile(LogEvent event) {
            try {
                MLog.d(TAG, String.format("writing event to file, eventId=%s, sessionId=%s", event.id, event.sessionId));
                FileOutputStream stream = ContextUtils.getInstance().getContext().openFileOutput(EVENTS_PREFIX + event.sessionId, Context.MODE_APPEND);
                byte[] eventBytes = mGson.toJson(event).getBytes();
                stream.write(eventBytes);
                stream.write(SPLIT);
                stream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeSessionToFile(LogSession session) {
            try {
                MLog.d(TAG, String.format("writing session to file, sessionId=%s", session.getId()));
                FileOutputStream stream = ContextUtils.getInstance().getContext().openFileOutput(SESSION_PREFIX + session.getId(), Context.MODE_PRIVATE);
                byte[] sessionBytes = mGson.toJson(session).getBytes();
                stream.write(sessionBytes);
                stream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class LogUploadCallback implements Callback<BaseResponse> {

        private final static String TAG = "LogUploadCallback";

        String fileName;

        public LogUploadCallback(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void onResponse(Response<BaseResponse> response) {
            boolean result = LocalFileUtils.delete(fileName);
            if (!result) {
                MLog.d(TAG, "delete file failed, name=" + fileName);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            MLog.d(TAG, "upload failed");
        }
    }
}
