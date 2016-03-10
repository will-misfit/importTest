package com.misfit.syncsdk.callback;

import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncsdk.model.PostCalculateData;
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
     * @param sdkActivitySessionGroup is activity session list and sleep session list after calculation
     * @return PostCalculateData, some data needs to update by App after sync and calculate
     */
    PostCalculateData onDataCalculateCompleted(SdkActivitySessionGroup sdkActivitySessionGroup);

    /**
     * result of getConfigurationTask needs to send to App invoker
     * */
    void onGetShineConfigurationCompleted(ConfigurationSession configSession);

    /**
     * Called when hardware log was read.
     * @param hwLog
     */
    void onHardwareLogRead(byte[] hwLog);

}
