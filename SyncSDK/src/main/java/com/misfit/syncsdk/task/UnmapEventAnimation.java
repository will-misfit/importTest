/**
 * UnmapEventAnimation.java
 * Sync-SDK-Android
 * Created by TerryZhou on 2/14/16
 */
package com.misfit.syncsdk.task;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.syncsdk.ConnectionManager;

import java.util.Hashtable;

/**
 * @author zhoufu24
 */
public class UnmapEventAnimation extends Task implements ConnectionManager.ConfigCompletedCallback {
    @Override
    public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {

    }

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void cleanup() {

    }
}
