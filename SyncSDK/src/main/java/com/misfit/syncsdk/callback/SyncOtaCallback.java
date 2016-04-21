package com.misfit.syncsdk.callback;

/**
 * callback exposed to App for OTA
 */
public interface SyncOtaCallback {
    /**
     * invoked when the entire OTA operation completes, including OTA and reconnect in a while later
     * */
    void onEntireOtaCompleted();

    /**
     * confirmed by App whether necessary to OTA although need to wait
     * */
    boolean isForceOta(boolean hasNewFirmware);
}
