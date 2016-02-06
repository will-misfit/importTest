package com.misfit.syncsdk.callback;

/**
 * basic callback for operation e.g. sync
 */
public interface SyncOperationResultCallback {
    void onSucceed();

    void onFailed(int reason);
}
