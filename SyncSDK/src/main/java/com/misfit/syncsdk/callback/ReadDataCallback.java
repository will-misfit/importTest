package com.misfit.syncsdk.callback;

import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;

import java.util.List;

/**
 * a group of callbacks which will be called inside SyncSDK to inform App the sync results
 */
public interface ReadDataCallback {

    /**
     * @param syncResultList is per minute Activity data collection from ShineSDK ShineProfile.SyncCallback
     */
    void onRawDataReadCompleted(List<SyncResult> syncResultList);

    /**
     * @param sdkActivitySessionGroupList is activity session list and sleep session list after calculation
     */
    void onDataCalculateCompleted(List<SdkActivitySessionGroup> sdkActivitySessionGroupList);

}
