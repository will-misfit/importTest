package com.misfit.syncsdk.callback;

import com.misfit.syncsdk.model.SdkGraphDay;

/**
 * callback for calculation during sync
 */
public interface SyncCalculationCallback {
    /**
     * query the SdkGraphDay by day
     * */
    SdkGraphDay getSdkGraphDayByDate(String dateOfDateRange);
}
