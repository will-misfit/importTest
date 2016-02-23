package com.misfit.syncsdk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * model class to hold activity session, sleep sessions and graph items after sync calculation
 */
public class SdkActivitySessionGroup {

    public List<SdkActivitySession> activitySessionList;
    public List<SdkSleepSession> sleepSessionList;
    public List<SdkGraphItem> graphItemList;

    public SdkActivitySessionGroup() {
        activitySessionList = new ArrayList<>();
        sleepSessionList = new ArrayList<>();
        graphItemList = new ArrayList<>();
    }
}
