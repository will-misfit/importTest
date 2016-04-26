package com.misfit.syncsdk.callback;

public interface SyncOnTagInStateListener {
    /**
     * Will be called when device is in tagging_in state, SyncSDK will wait for the SyncTaggingInputCallback.
     *
     * @param deviceType    current device type
     * @param inputCallback
     */
    void onDeviceTaggingIn(int deviceType, SyncOnTagInUserInputListener inputCallback);
}
