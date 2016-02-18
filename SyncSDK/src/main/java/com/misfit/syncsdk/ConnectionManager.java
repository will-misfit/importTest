package com.misfit.syncsdk;

import android.support.annotation.NonNull;
import android.util.Log;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


/**
 * class to manage the ShineDevice/ShineProfile/ShineSDK callbacks which are dependent on device
 */
public class ConnectionManager {

    private final static String TAG = "ConnectionManager";

    Map<String, ShineDevice> mShineDeviceCache = new HashMap<>();
    Map<String, ShineSdkProfileProxy> mShineProfileProxyCache = new HashMap<>();

    private static ConnectionManager sharedInstance;

    public static ConnectionManager getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new ConnectionManager();
        }
        return sharedInstance;
    }

    public ShineDevice getShineDevice(String serialNumber) {
        return mShineDeviceCache.get(serialNumber);
    }

    public ShineSdkProfileProxy getShineSDKProfileProxy(String serialNumber) {
        return mShineProfileProxyCache.get(serialNumber);
    }

    public void saveShineDevice(String serialNumber, @NonNull ShineDevice device) {
        mShineDeviceCache.put(serialNumber, device);
    }

    public void saveShineProfileProxy(String serialNumber, @NonNull ShineSdkProfileProxy profileProxy) {
        mShineProfileProxyCache.put(serialNumber, profileProxy);
    }

    public void releaseShineProfileProxy(String serialNumber) {
        ShineSdkProfileProxy proxy = getShineSDKProfileProxy(serialNumber);
        if (proxy != null) {
            proxy.releaseCallbacks();
            mShineProfileProxyCache.remove(serialNumber);
        }
    }

    public ShineSdkProfileProxy createShineProfileProxy(String serialNumber) {
        //TODO:should transfer itself or serialNumber in callback
        ShineSdkProfileProxy shineSdkProfileProxy = new ShineSdkProfileProxy();
        saveShineProfileProxy(serialNumber, shineSdkProfileProxy);
        return shineSdkProfileProxy;
    }
}
