package com.misfit.ble.sample.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MLog {

    // initially this value is from BuildConfig.DEBUG. But within SyncDemo,
    // SyncSDK build type is RELEASE, so set it true to display the log on UI
    private final static boolean OPEN_LOG = true;

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
            Log.d(tag, msg);
            notifyLogNodes(Log.DEBUG, tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (OPEN_LOG) {
            Log.w(tag, msg);
            notifyLogNodes(Log.WARN, tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (OPEN_LOG) {
            Log.i(tag, msg);
            notifyLogNodes(Log.INFO, tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (OPEN_LOG) {
            Log.e(tag, msg);
            notifyLogNodes(Log.ERROR, tag, msg);
        }
    }

    public static String getArrayString(Object[] array) {
        if (array == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder("[");
        for (Object item : array) {
            builder.append(item)
                    .append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }

    public static String getListString(List<Object> list) {
        if (list == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder("[");
        for (Object item : list) {
            builder.append(item)
                    .append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
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
}
