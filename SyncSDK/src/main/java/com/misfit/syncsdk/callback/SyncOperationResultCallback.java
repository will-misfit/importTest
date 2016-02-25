package com.misfit.syncsdk.callback;

/**
 * basic callback for SDK operation result to tell App invoker, e.g. startSync() final result
 */
public interface SyncOperationResultCallback {
    void onSucceed();

    void onFailed(int reason);
}
