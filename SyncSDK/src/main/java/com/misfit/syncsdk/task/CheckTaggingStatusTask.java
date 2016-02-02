package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.syncsdk.callback.SyncTaggingInputCallback;

/**
 * check the tagging state, if true, then tell the app and wait for the callback
 */
public class CheckTaggingStatusTask extends Task implements SyncTaggingInputCallback {

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        if (mTaskSharedData.getConfigurationSession() == null) {
            taskFailed("configuration is null");
            return;
        }
        byte taggingState = mTaskSharedData.getConfigurationSession().mShineConfiguration.mActivityTaggingState;
        if (taggingState == ShineConfiguration.ACTIVITY_TAGGING_STATE_TAGGED_IN) {
            if (mTaskSharedData.getSyncSyncCallback() == null) {
                taskFailed("SyncSyncCallback is null");
                return;
            }
            mTaskSharedData.getSyncSyncCallback().onDeviceTaggingIn(mTaskSharedData.getDeviceType(), this);
        } else {
            taskSucceed();
        }
    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void cleanup() {

    }

    @Override
    public void onUserInputForTaggingIn(boolean shouldContinueSync) {
        if (mIsFinished) {
            return;
        }
        if (shouldContinueSync) {
            taskSucceed();
        } else {
            taskFailed("user choose not to sync");
        }
    }
}

