package com.misfit.syncsdk.callback;

import com.misfit.ble.shine.ShineProfile;

/**
 * callback provided to App invoker to listen to BLE connection state change
 */
public interface ConnectionStateCallback {
    void onConnectionStateChanged(ShineProfile.State newState);
}
