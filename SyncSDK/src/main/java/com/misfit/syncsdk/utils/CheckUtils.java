package com.misfit.syncsdk.utils;

import java.util.List;

/**
 *
 */
public class CheckUtils {

    public static boolean isCollectionEmpty(List<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isStringEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
