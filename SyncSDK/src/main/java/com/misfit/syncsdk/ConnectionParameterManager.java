package com.misfit.syncsdk;

import com.misfit.ble.shine.ShineConnectionParameters;

/**
 * Created by Will Hou on 1/13/16.
 */
public class ConnectionParameterManager {

    public static ShineConnectionParameters defaultParams() {
        return new ShineConnectionParameters(7.5, 0, 720);
    }

    public static ShineConnectionParameters defaultShine2Params() {
        return new ShineConnectionParameters(15, 0, 720);
    }

    public static ShineConnectionParameters defaultRayParams() {
        return new ShineConnectionParameters(15, 0, 720);
    }

    public static ShineConnectionParameters defaultIwcParams() {
        return new ShineConnectionParameters(15, 0, 720);
    }

    public static ShineConnectionParameters defaultSwarovskiParams() {
        return new ShineConnectionParameters(15, 0, 720);
    }
}
