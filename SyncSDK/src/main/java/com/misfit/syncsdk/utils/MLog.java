package com.misfit.syncsdk.utils;

import android.util.Log;

import com.misfit.syncsdk.BuildConfig;

import java.util.List;
import java.util.Map;

/**
 * Created by Will Hou on 1/29/16.
 */
public class MLog {
    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
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
