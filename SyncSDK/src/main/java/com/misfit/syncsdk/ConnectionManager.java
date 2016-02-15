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

    public interface ConnectionStateCallback {
        void onConnectionStateChanged(ShineProfile.State newState);
    }

    public interface ConfigCompletedCallback {
        void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data);
    }

    Map<String, ShineDevice> mShineDeviceCache;
    Map<String, ShineSdkProfileProxy> mShineProfileProxyCache;

    // actually, each item of the HashMap includes both of ConnectionStateCallback and ConfigCompletedCallback
    Map<String, BleSdkProfileCallbackWrapper> mConnectionCallbackWrappers;

    private static ConnectionManager sharedInstance;

    private ConnectionManager() {
        mShineDeviceCache = new HashMap<>();
        mShineProfileProxyCache = new HashMap<>();
        mConnectionCallbackWrappers = new HashMap<>();
    }

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
        mShineProfileProxyCache.put(serialNumber, null);
        BleSdkProfileCallbackWrapper callbackWrapper = mConnectionCallbackWrappers.get(serialNumber);
        if (callbackWrapper != null) {
            callbackWrapper.release();
        }
    }

    public ShineSdkProfileProxy createShineProfileProxy(String serialNumber) {
        //TODO:should transfer itself or serialNumber in callback
        BleSdkProfileCallbackWrapper callbackWrapper = new BleSdkProfileCallbackWrapper();
        ShineSdkProfileProxy shineSdkProfileProxy = new ShineSdkProfileProxy(callbackWrapper, callbackWrapper);
        saveShineProfileProxy(serialNumber, shineSdkProfileProxy);
        //FIXME: maybe Profile*n vs callbackWrapper*1
        mConnectionCallbackWrappers.put(serialNumber, callbackWrapper);
        return shineSdkProfileProxy;
    }

    public void subscribeConfigCompleted(String serialNumber, ConfigCompletedCallback configCompletedCallback) {
        BleSdkProfileCallbackWrapper callbackWrapper = mConnectionCallbackWrappers.get(serialNumber);
        if (callbackWrapper == null) {
            Log.d(TAG, "no callback wrapper, serialNumber=" + serialNumber);
            return;
        }
        callbackWrapper.subscribeConfigCompleted(configCompletedCallback);
    }

    public void unsubscribeConfigCompleted(String serialNumber, ConfigCompletedCallback configCompletedCallback) {
        BleSdkProfileCallbackWrapper callbackWrapper = mConnectionCallbackWrappers.get(serialNumber);
        if (callbackWrapper == null) {
            Log.d(TAG, "no callback wrapper, serialNumber=" + serialNumber);
            return;
        }
        callbackWrapper.unsubscribeConfigCompleted(configCompletedCallback);
    }

    public void subscribeConnectionStateChanged(String serialNumber, ConnectionStateCallback connectionStateCallback) {
        BleSdkProfileCallbackWrapper callbackWrapper = mConnectionCallbackWrappers.get(serialNumber);
        if (callbackWrapper == null) {
            Log.d(TAG, "no callback wrapper, serialNumber=" + serialNumber);
            return;
        }
        callbackWrapper.subscribeConnectionStateChanged(connectionStateCallback);
    }

    public void unsubscribeConnectionStateChanged(String serialNumber, ConnectionStateCallback connectionStateCallback) {
        BleSdkProfileCallbackWrapper callbackWrapper = mConnectionCallbackWrappers.get(serialNumber);
        if (callbackWrapper == null) {
            Log.d(TAG, "no callback wrapper, serialNumber=" + serialNumber);
            return;
        }
        callbackWrapper.unsubscribeConnectionStateChanged(connectionStateCallback);
    }

    static class BleSdkProfileCallbackWrapper implements ShineProfile.ConfigurationCallback, ShineProfile.ConnectionCallback {

        List<ConnectionStateCallback> mConnectionStateCallbacks;
        List<ConfigCompletedCallback> mConfigCompletedCallbacks;

        public BleSdkProfileCallbackWrapper() {
            mConfigCompletedCallbacks = new ArrayList<>();
            mConnectionStateCallbacks = new ArrayList<>();
        }

        public void subscribeConfigCompleted(ConfigCompletedCallback configCompletedCallback) {
            if (mConfigCompletedCallbacks.contains(configCompletedCallback)) {
                return;
            }
            mConfigCompletedCallbacks.add(configCompletedCallback);
        }

        public void unsubscribeConfigCompleted(ConfigCompletedCallback configCompletedCallback) {
            mConfigCompletedCallbacks.remove(configCompletedCallback);
        }

        public void subscribeConnectionStateChanged(ConnectionStateCallback connectionStateCallback) {
            if (mConnectionStateCallbacks.contains(connectionStateCallback)) {
                return;
            }
            mConnectionStateCallbacks.add(connectionStateCallback);
        }

        public void unsubscribeConnectionStateChanged(ConnectionStateCallback connectionStateCallback) {
            if (mConnectionStateCallbacks != null) {
                mConnectionStateCallbacks.remove(connectionStateCallback);
            }
        }

        @Override
        public void onConfigCompleted(ActionID actionID, ShineProfile.ActionResult resultCode, Hashtable<ShineProperty, Object> data) {
            if (mConfigCompletedCallbacks.isEmpty()) {
                return;
            }
            for (ConfigCompletedCallback callback : mConfigCompletedCallbacks) {
                callback.onConfigCompleted(actionID, resultCode, data);
            }
        }

        @Override
        public void onConnectionStateChanged(ShineProfile shineProfile, ShineProfile.State newState) {
            if (mConnectionStateCallbacks.isEmpty()) {
                return;
            }
            for (ConnectionStateCallback callback : mConnectionStateCallbacks) {
                callback.onConnectionStateChanged(newState);
            }
        }

        public void release() {
            mConfigCompletedCallbacks.clear();
            mConnectionStateCallbacks.clear();
        }
    }
}
