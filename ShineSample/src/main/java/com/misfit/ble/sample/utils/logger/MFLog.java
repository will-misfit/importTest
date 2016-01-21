package com.misfit.ble.sample.utils.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Log enhancement，includes Logcat/File/View/Network output，and be able to add new output format via LogNode
 */
public class MFLog {

    public interface LogNode {
        void log(int priority, String tag, String content);
    }

    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static final int DEBUG = android.util.Log.DEBUG;
    public static final int INFO = android.util.Log.INFO;
    public static final int WARN = android.util.Log.WARN;
    public static final int ERROR = android.util.Log.ERROR;
    public static final int WTF = android.util.Log.ASSERT;

    private static final int NUMBER_OF_LOGTYPE = 4;
    private static final int POS_OF_LOGTYPE_VIEW = 2;
    private static final int POS_OF_LOGTYPE_FILE = 1;

    public static final int LOGTYPE_NONE = 0;
    public static final int LOGTYPE_LOGCAT = 1;
    public static final int LOGTYPE_FILE = 1 << 1;
    public static final int LOGTYPE_VIEW = 1 << 2;
    public static final int LOGTYPE_NETWORK = 1 << 3;
    public static final int LOGTYPE_ALL = LOGTYPE_LOGCAT | LOGTYPE_FILE | LOGTYPE_VIEW | LOGTYPE_NETWORK;

    private static int LOG_TYPE = LOGTYPE_ALL;

    private final static LogNode[] logNodes = new LogNode[]{new LogCat(), new FileLogger(), null, new NetLogger()};

    public static void setLogType(int logType) {
        LOG_TYPE = logType;
    }

    public static void e(String tag, String content) {
        if (LOG_TYPE == LOGTYPE_NONE) {
            return;
        }
        log(ERROR, tag, content);
    }

    public static void i(String tag, String content) {
        if (LOG_TYPE == LOGTYPE_NONE) {
            return;
        }
        log(INFO, tag, content);
    }

    public static void d(String tag, String content) {
        if (LOG_TYPE == LOGTYPE_NONE) {
            return;
        }
        log(DEBUG, tag, content);
    }

    public static void v(String tag, String content) {
        if (LOG_TYPE == LOGTYPE_NONE) {
            return;
        }
        log(VERBOSE, tag, content);
    }

    public static void w(String tag, String content) {
        if (LOG_TYPE == LOGTYPE_NONE) {
            return;
        }
        log(WARN, tag, content);
    }

    public static void wtf(String tag, String content) {
        if (LOG_TYPE == LOGTYPE_NONE) {
            return;
        }
        log(WTF, tag, content);
    }

    private static void log(int priority, String tag, String content) {
        for (int pos = 0; pos < NUMBER_OF_LOGTYPE; pos++) {
            if (((1 << pos) & LOG_TYPE) != 0 && logNodes[pos] != null) {
                logNodes[pos].log(priority, tag, content);
            }
        }
    }

    public static void setViewLogger(LogNode view) {
        logNodes[POS_OF_LOGTYPE_VIEW] = view;
    }

    public static String getPriorityStr(int priority) {
        String priorityStr;
        switch (priority) {
            case MFLog.VERBOSE:
                priorityStr = "V";
                break;
            case MFLog.DEBUG:
                priorityStr = "D";
                break;
            case MFLog.INFO:
                priorityStr = "I";
                break;
            case MFLog.WARN:
                priorityStr = "W";
                break;
            case MFLog.ERROR:
                priorityStr = "E";
                break;
            case MFLog.WTF:
                priorityStr = "WTF";
                break;
            default:
                priorityStr = "UNKNOWN";
                break;
        }
        return priorityStr;
    }

    public static void printList(String tag, List<? extends Object> list) {
        if (list == null) {
            MFLog.i(tag, "list is null");
            return;
        }
        StringBuilder builder = new StringBuilder("[");
        for (Object val : list) {
            builder.append(val.toString()).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        MFLog.i(tag, builder.toString());
    }

    public static void printArray(String tag, Object[] list) {
        if (list == null) {
            MFLog.i(tag, "list is null");
            return;
        }
        StringBuilder builder = new StringBuilder("[");
        for (Object val : list) {
            builder.append(val).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        MFLog.i(tag, builder.toString());
    }

    public static void printArray(String tag, byte[] list) {
        if (list == null) {
            MFLog.i(tag, "list is null");
            return;
        }
        StringBuilder builder = new StringBuilder("size:" + list.length + ", [");
        for (byte val : list) {
            builder.append(val).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        MFLog.i(tag, builder.toString());
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public static void setLogFileName(String name) {
        FileLogger fileLogger = (FileLogger) logNodes[POS_OF_LOGTYPE_FILE];
        fileLogger.setCustomLogName(name);
    }
}
