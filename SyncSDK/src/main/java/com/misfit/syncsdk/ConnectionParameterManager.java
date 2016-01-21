package com.misfit.syncsdk;

import com.misfit.ble.shine.ShineConnectionParameters;

/**
 * Created by Will Hou on 1/13/16.
 */
public class ConnectionParameterManager {

    public static ShineConnectionParameters defaultParams() {
        ShineConnectionParameters params = new ShineConnectionParameters(7.5, 0, 720);
        return params;
    }

}
