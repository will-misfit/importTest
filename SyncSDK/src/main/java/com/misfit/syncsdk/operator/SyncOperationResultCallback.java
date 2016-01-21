package com.misfit.syncsdk.operator;

/**
 * Created by Will-Hou on 1/12/16.
 */
public interface SyncOperationResultCallback {
    void onFinished();

    void onFailed(int reason);
}
