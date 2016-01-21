package com.misfit.syncsdk.callback;

import com.misfit.syncsdk.OtaType;

/**
 * Created by Will-Hou on 1/11/16.
 */
public interface SyncOtaCallback {
    void onOtaProgress(float progress);

    void onOtaCompleted();

    //TODO: need a discussion
    int getOtaSuggestion(boolean hasNewFirmware);
}
