package com.misfit.syncsdk.callback;

import com.misfit.syncsdk.device.SyncCommonDevice;

/**
 * Created by Will-Hou on 1/11/16.
 */
public interface SyncScanCallback {
    public void onScanResultFiltered(SyncCommonDevice device, int rssi);
}
