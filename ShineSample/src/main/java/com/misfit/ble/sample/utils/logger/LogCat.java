package com.misfit.ble.sample.utils.logger;

/**
 * Logcat方式输出
 * <br>
 * author: houxg
 * <br>
 * create on 2015/4/13
 */
public class LogCat implements MFLog.LogNode {
    @Override
    public void log(int priority, String tag, String content) {
        switch (priority) {
            case MFLog.DEBUG:
                android.util.Log.d(tag, content);
                break;
            case MFLog.VERBOSE:
                android.util.Log.v(tag, content);
                break;
            case MFLog.INFO:
                android.util.Log.i(tag, content);
                break;
            case MFLog.WARN:
                android.util.Log.w(tag, content);
                break;
            case MFLog.ERROR:
                android.util.Log.e(tag, content);
                break;
            case MFLog.WTF:
                android.util.Log.wtf(tag, content);
                break;
            default:
                android.util.Log.wtf(tag, "default?" + content);
                break;
        }
    }
}
