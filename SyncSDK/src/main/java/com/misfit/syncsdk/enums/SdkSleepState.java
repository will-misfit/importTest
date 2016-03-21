package com.misfit.syncsdk.enums;

import com.misfit.cloud.algorithm.models.SleepStateShine;

/**
 * data model to reflect com.misfitwearables.prometheus.common.enums.SleepState
 */
public class SdkSleepState {

    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_WAKE = 1;
    public static final int STATE_SLEEP = 2;
    public static final int STATE_DEEP_SLEEP = 3;

    private SdkSleepState(){}

    public static int convertSleepStateShine2SdkSleepState(SleepStateShine sleepStateShine) {
        int result = STATE_UNKNOWN;
        if (sleepStateShine == SleepStateShine.WAKE_STATE) {
            result = STATE_WAKE;
        } else if (sleepStateShine == SleepStateShine.SLEEP_STATE) {
            result = STATE_SLEEP;
        } else if (sleepStateShine == SleepStateShine.DEEP_STATE) {
            result = STATE_DEEP_SLEEP;
        }
        return result;
    }
}
