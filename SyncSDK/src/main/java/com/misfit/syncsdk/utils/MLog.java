package com.misfit.syncsdk.utils;

import android.util.Log;

import com.misfit.ble.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MLog {

    // initially this value is from BuildConfig.DEBUG. But within SyncDemo,
    // SyncSDK build type is RELEASE, so set it true to display the log on UI
    private final static boolean OPEN_LOG = BuildConfig.DEBUG;
    private final static LogNode mLogCat;

    static {
        mLogCat = new LogCat();
        registerLogNode(mLogCat);
    }

    public interface LogNode {
        void printLog(int priority, String tag, String msg);
    }

    private static List<LogNode> logNodes = new ArrayList<>();

    public static void registerLogNode(LogNode node) {
        if (logNodes.contains(node)) {
            return;
        }
        logNodes.add(node);
    }

    public static void unregisterLogNode(LogNode node) {
        logNodes.remove(node);
    }

    private static void notifyLogNodes(int priority, String tag, String msg) {
        for (LogNode node : logNodes) {
            node.printLog(priority, tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (OPEN_LOG) {
            notifyLogNodes(Log.DEBUG, tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (OPEN_LOG) {
            notifyLogNodes(Log.WARN, tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (OPEN_LOG) {
            notifyLogNodes(Log.INFO, tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (OPEN_LOG) {
            notifyLogNodes(Log.ERROR, tag, msg);
        }
    }

    public static String getMapString(Map<Object, Object> map) {
        if (map == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder("[");
        for (Map.Entry<Object, Object> item : map.entrySet()) {
            builder.append(item.getKey())
                    .append("=")
                    .append(item.getValue());
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }

    public static class LogCat implements LogNode {

        private final static int LOG_CAT_MAX_LENGTH = 3000;

        @Override
        public void printLog(int priority, String tag, String msg) {
            if (msg == null) {
                print(priority, tag, msg);
                return;
            }
            while (msg.length() > LOG_CAT_MAX_LENGTH) {
                String printMsg = msg.substring(0, LOG_CAT_MAX_LENGTH);
                print(priority, tag, printMsg);
                msg = msg.substring(LOG_CAT_MAX_LENGTH);
            }
            print(priority, tag, msg);
        }

        private void print(int priority, String tag, String msg) {
            switch (priority) {
                case Log.ASSERT:
                    Log.d(tag, msg);
                    break;
                case Log.VERBOSE:
                    Log.v(tag, msg);
                    break;
                case Log.INFO:
                    Log.i(tag, msg);
                    break;
                case Log.WARN:
                    Log.w(tag, msg);
                    break;
                case Log.ERROR:
                    Log.e(tag, msg);
                    break;
                case Log.DEBUG:
                    Log.d(tag, msg);
                    break;
                default:
                    Log.e(tag, msg);
                    break;
            }
        }
    }
}
