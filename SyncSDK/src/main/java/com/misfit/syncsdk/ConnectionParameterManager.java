package com.misfit.syncsdk;

import com.misfit.ble.shine.ShineConnectionParameters;

/**
 * the connection parameters are presented as [ConnectionInterval, ConnectionLatency, SupervisionTimeout]
 */
public class ConnectionParameterManager {

    public static final ShineConnectionParameters DefaultParams
        = new ShineConnectionParameters(7.5, 0, 720);

    public static final ShineConnectionParameters DefaultShine2Params
        = new ShineConnectionParameters(15, 0, 720);

    public static final ShineConnectionParameters DefaultRayParams
        = new ShineConnectionParameters(15, 0, 720);

    public static final ShineConnectionParameters DefaultIWCParams
        = new ShineConnectionParameters(15, 0, 720);

    public static final ShineConnectionParameters DefaultSwarovskiParams
        = new ShineConnectionParameters(15, 0, 720);

    public static final ShineConnectionParameters SlowConnectionParams
        = new ShineConnectionParameters(200, 4, 6 * 1000);

    public static boolean paramsEquals(ShineConnectionParameters lhp, ShineConnectionParameters rhp) {
        if (lhp == null || rhp == null) {
            return false;
        }

        return lhp.getConnectionInterval() == rhp.getConnectionInterval()
            && lhp.getConnectionLatency()  == rhp.getConnectionLatency()
            && lhp.getSupervisionTimeout() == rhp.getSupervisionTimeout();
    }
}
