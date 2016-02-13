/**
 * ScanFailedReason.java
 * Sync-SDK-Android
 * Created by TerryZhou on 2/13/16
 */
package com.misfit.syncsdk.enums;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author zhoufu24
 */
public class ScanFailedReason {
    @IntDef({NO_BLUETOOTH, INTERNAL_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScanFailedReasonValue {
    }

    // User is not using standalone.
    public static final int NO_BLUETOOTH = 0;

    // User is using standalone.
    public static final int INTERNAL_ERROR = 1;
}
