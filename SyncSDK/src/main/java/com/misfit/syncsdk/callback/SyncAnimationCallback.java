package com.misfit.syncsdk.callback;

import com.misfit.syncsdk.device.SyncCommonDevice;

/**
 * animation callback
 */
public interface SyncAnimationCallback {
    public void onScanResultFiltered(SyncCommonDevice device, int rssi);
}
