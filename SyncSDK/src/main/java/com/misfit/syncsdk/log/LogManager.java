package com.misfit.syncsdk.log;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.misfit.syncsdk.model.BaseResponse;
import com.misfit.syncsdk.network.APIClient;
import com.misfit.syncsdk.utils.CollectionUtils;
import com.misfit.syncsdk.utils.LocalFileUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.SdkConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Callback;
import retrofit2.Response;

public class LogManager {

    private final static String TAG = "LogManager";
    private static final String LOG_FOLDER = "com.misfit.syncsdk.log";

    //FIXME:when close this thread?
    private HandlerThread mHandlerThread;
    private LogHandler mHandler;

    private Gson mGson;
    APIClient.LogAPI mAPI;

    private static LogManager mInstance;

    private LogManager() {
        mGson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        mAPI = APIClient.getInstance().getLogAPI();
        mHandlerThread = new HandlerThread("sync_sdk_log");
        mHandlerThread.start();
        mHandler = new LogHandler(mHandlerThread.getLooper());
    }

    public static LogManager getInstance() {
        if (mInstance == null) {
            mInstance = new LogManager();
        }
        return mInstance;
    }

    public void saveSession(LogSession session) {
        mHandler.saveSession(session);
    }

    /**
     * LogEvent is saved(written) in local file
     * */
    public void appendEvent(LogEvent event) {
        mHandler.saveEvent(event);
    }

    private void uploadLog(String sessionId) {
        mHandler.uploadSession(sessionId);
        mHandler.uploadEvents(sessionId);
    }

    /**
     * when to upload all log files, it will search all local log files, and upload them one by one
     * */
    public void uploadAllLog() {
        File[] logFiles = LocalFileUtils.getFiles(LOG_FOLDER);
        if (logFiles == null) {
            return;
        }

        Set<String> sessionIdSet = new HashSet<>();
        for (File file : logFiles) {
            String sessionId = parseSessionId(file.getName());
            if (sessionIdSet.contains(sessionId)) {
                continue;
            }
            sessionIdSet.add(sessionId);
            uploadLog(sessionId);
        }
    }

    private class LogHandler extends Handler {
        private final static String TAG = "LogHandler";

        private final static int REQ_SAVE_SESSION = 1;
        private final static int REQ_SAVE_EVENT = 2;
        private final static int REQ_UPLOAD_SESSION = 3;
        private final static int REQ_UPLOAD_EVENTS = 4;

        public LogHandler(Looper looper) {
            super(looper);
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
                    uploadSessionToServer(sessionId);
                    break;
                case REQ_UPLOAD_EVENTS:
                    sessionId = (String) msg.obj;
                    uploadEventsToServer(sessionId);
                    break;
                default:
                    MLog.d(TAG, "unexpected message, what=" + msg.what);
            }
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
    }

    /**
     * write one LogSession to one file
     * since there are several moments to write LogSession to file, its solution is to
     * override content in file instead of append
     * */
    private void writeSessionToFile(LogSession session) {
        BufferedWriter bufferedWriter = null;
        try {
            MLog.d(TAG, String.format("writing session to file, sessionId = %s", session.getId()));
            String fileName = SdkConstants.SESSION_PREFIX + session.getId();
            FileOutputStream stream = LocalFileUtils.openFileOutput(LOG_FOLDER, fileName, Context.MODE_PRIVATE, false);
            if (stream == null) {
                return;
            }

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(stream));
            String sessionJsonStr = mGson.toJson(session);
            bufferedWriter.write(new String(sessionJsonStr));
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * write one LogEvent to file in one line
     * */
    private void writeEventToFile(LogEvent event) {
        BufferedWriter bufferedWriter = null;
        try {
            Log.d(TAG, String.format("writing event to file, eventId = %s, sessionId = %s", event.id, event.sessionId));
            String fileName = getLogFileName(SdkConstants.EVENTS_PREFIX, event.sessionId);
            FileOutputStream stream = LocalFileUtils.openFileOutput(LOG_FOLDER, fileName,
                Context.MODE_APPEND | Context.MODE_PRIVATE);
            if (stream == null) {
                return;
            }

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(stream));
            byte[] eventBytes = mGson.toJson(event).getBytes();

            bufferedWriter.newLine();
            bufferedWriter.write(new String(eventBytes));
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param sessionId the target's sessionId
     * @return true if upload started, false for else
     */
    private boolean uploadSessionToServer(String sessionId) {
        String fileName = getLogFileName(SdkConstants.SESSION_PREFIX, sessionId);
        byte[] sessionBytes = LocalFileUtils.read(LOG_FOLDER, fileName);
        if (sessionBytes == null || sessionBytes.length == 0) {
            MLog.d(TAG, String.format("session_%s file does not exist or contain nothing", sessionId));
            return false;
        }

        String sessionStr = new String(sessionBytes);
        LogSession session;
        try {
            session = mGson.fromJson(sessionStr, LogSession.class);
            mAPI.uploadSession(session).enqueue(new UploadLogCallback(fileName));
        } catch (JsonSyntaxException ex) {
            MLog.d(TAG, Log.getStackTraceString(ex.getCause()));
            return false;
        }
        return true;
    }

    /**
     * @param sessionId the target's sessionId
     * @return true if upload started, false for else
     */
    private boolean uploadEventsToServer(String sessionId) {
        String fileName = getLogFileName(SdkConstants.EVENTS_PREFIX, sessionId);
        List<String> eventStrings = readEventsJsonFromFile(fileName);
        if (CollectionUtils.isEmpty(eventStrings)) {
            MLog.d(TAG, String.format("events_%s file does not exist or contains nothing", sessionId));
            return false;
        }

        List<LogEvent> eventList = new ArrayList<>();
        for (String eventStr : eventStrings) {
            try {
                LogEvent event = mGson.fromJson(eventStr, LogEvent.class);
                eventList.add(event);
            } catch (JsonSyntaxException ex) {
                MLog.d(TAG, "failed to convert string to LogEvent, string = " + eventStr);
            }
        }

        mAPI.uploadEvents(sessionId, eventList).enqueue(new UploadLogCallback(fileName));
        return true;
    }

    /**
     * retrofit2 response listener for LogSession and LogEvent(s)
     * */
    private static class UploadLogCallback implements Callback<BaseResponse> {

        private final static String TAG = "LogUploadCallback";
        private String mFileName;

        public UploadLogCallback(String fileName) {
            mFileName = fileName;
        }

        @Override
        public void onResponse(Response<BaseResponse> response) {
            boolean result = LocalFileUtils.delete(LOG_FOLDER, mFileName);
            if (!result) {
                MLog.d(TAG, "delete file failed, name = " + mFileName);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            MLog.d(TAG, "upload failed");
        }
    }

    private static String getLogFileName(String prefix, String sessionId) {
        return prefix + sessionId;
    }

    /**
     * from a file full of events, read them line by line
     * Note: result does not contain null/empty String
     * */
    private static List<String> readEventsJsonFromFile(String fileName) {
        List<String> jsonList = new ArrayList<>();

        BufferedReader bufferedReader = null;
        try {
            FileInputStream inStream = LocalFileUtils.openFileInput(LOG_FOLDER, fileName);
            if (inStream == null) {
                return jsonList;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inStream));
            while(bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (!TextUtils.isEmpty(line)) {
                    jsonList.add(line);
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            MLog.d(TAG, e.getMessage());
        } catch (IOException e) {
            MLog.d(TAG, e.getStackTrace().toString());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonList;
    }

    /**
     * log files are named like session_[sessionId] or events_[sessionId]
     * */
    private static String parseSessionId(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        final String splitStr = "_";
        int pos = fileName.lastIndexOf(splitStr);
        if (pos == -1) {
            return null;
        }
        return fileName.substring(pos + 1, fileName.length());
    }
}
