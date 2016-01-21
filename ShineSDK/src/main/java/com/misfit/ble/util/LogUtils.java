package com.misfit.ble.util;

/**
 * Some utilities for Log
 * <p>Created by Quoc-Hung Le on 8/31/2015.
 */
public class LogUtils {

    private static final int MAX_LOG_TAG_LENGTH = 23;
    private static final String LOG_PREFIX = "mf_";

    public static String makeTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX.length()) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX.length() - 1);
        }

        return LOG_PREFIX + str;
    }

    public static String makeTag(Class cls) {
        return makeTag(cls.getSimpleName());
    }
}
