package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.syncsdk.callback.SyncOnTagInUserInputListener;

/**
 * check the tagging state, if true, then tell the app and wait for the callback
 * TODO: define a LogEventType for this task?
 */
public class CheckOnTagStatusTaskUserInput extends Task implements SyncOnTagInUserInputListener {

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        if (mTaskSharedData.getConfigurationSession() == null
                || mTaskSharedData.getConfigurationSession().mShineConfiguration == null) {
            taskFailed("configuration is null");
            return;
        }
        if (mTaskSharedData.getSyncParams().tagInStateListener == null) {
            taskFailed("SyncOnTagInStateListener is null");
            return;
        }
        byte taggingState = mTaskSharedData.getConfigurationSession().mShineConfiguration.mActivityTaggingState;
        if (taggingState == ShineConfiguration.ACTIVITY_TAGGING_STATE_TAGGED_IN) {
            mTaskSharedData.getSyncParams().tagInStateListener.onDeviceTaggingIn(mTaskSharedData.getDeviceType(), this);
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

