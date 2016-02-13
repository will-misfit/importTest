package com.misfit.syncsdk.callback;

import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.enums.ScanFailedReason;

/**
 * Created by Will-Hou on 1/11/16.
 */
public interface SyncScanCallback {
    void onScanResultFiltered(SyncCommonDevice device, int rssi);

    void onScanFailed(@ScanFailedReason.ScanFailedReasonValue int reason);
}
