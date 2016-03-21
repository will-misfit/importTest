package com.misfit.syncsdk.callback;

import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.enums.ScanFailedReason;

/**
 * callback of scan used by SyncSDK to notify App invoker
 */
public interface SyncScanCallback {
    void onScanResultFiltered(SyncCommonDevice device, int rssi);

    void onScanFailed(@ScanFailedReason.ScanFailedReasonValue int reason);
}
