package com.misfit.syncsdk;

/**
 * Created by Will Hou on 1/15/16.
 */
public class OtaType {
    public final static int FORCE_OTA = 0;
    public final static int NEED_OTA = 1;
    public final static int NO_NEED_TO_OTA = 2;

    private int mVal;

    OtaType(int val) {
        mVal = val;
    }
}
