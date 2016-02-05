package com.misfit.syncsdk.callback;

import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;

import java.util.List;

/**
 * a group of callbacks which will be called inside SyncSDK to inform App the sync results
 */
public interface SyncSyncCallback extends OperationResultCallback {

    /**
     * @param syncResultList is per minute Activity data collection from ShineSDK ShineProfile.SyncCallback
     */
    void onShineProfileSyncReadDataCompleted(List<SyncResult> syncResultList);

    /**
     * @param sdkActivitySessionGroupList is activity session list and sleep session list after calculation
     */
    void onSyncAndCalculationCompleted(List<SdkActivitySessionGroup> sdkActivitySessionGroupList);

    /**
     * Will be called when device is in tagging_in state, SyncSDK will wait for the SyncTaggingInputCallback.
     *
     * @param deviceType    current device type
     * @param inputCallback
     */
    void onDeviceTaggingIn(int deviceType, SyncTaggingInputCallback inputCallback);
}
