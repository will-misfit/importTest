package com.misfit.syncsdk.callback;

/**
 * Created by Will-Hou on 1/11/16.
 */
public interface SyncOtaCallback {
    void onOtaProgress(float progress);

    void onOtaCompleted();

    boolean isForceOta(boolean hasNewFirmware);
}
