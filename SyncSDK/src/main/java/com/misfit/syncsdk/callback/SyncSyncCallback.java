package com.misfit.syncsdk.callback;

import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;
import com.misfit.syncsdk.operator.SyncOperationResultCallback;

import java.util.List;

/**
 *
 */
public interface SyncSyncCallback extends SyncOperationResultCallback{

    /**
     * @param syncResultList is per minute Activity data collection from ShineSDK ShineProfile.SyncCallback
     * */
    void onShineProfileSyncReadDataCompleted(List<SyncResult> syncResultList);

    /**
     * @param sdkActivitySessionGroupList is activity session list and sleep session list after calculation
     * */
    void onSyncAndCalculationCompleted(List<SdkActivitySessionGroup> sdkActivitySessionGroupList);
}
