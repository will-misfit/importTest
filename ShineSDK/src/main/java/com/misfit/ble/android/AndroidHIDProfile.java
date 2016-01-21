package com.misfit.ble.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * Created by minh on 7/16/15.
 */
public class AndroidHIDProfile {
    private static final String TAG = AndroidHIDProfile.class.getName();
    private static AndroidHIDProfile sharedInstance;

    public static void setUp(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new AndroidHIDProfile(context);
        }
        sharedInstance.context = context;
    }

    public static AndroidHIDProfile getSharedInstance() {
        return sharedInstance;
    }

    public interface RetrieveConnectedHIDCallback  {
        void onConnectedHIDRetrieved(List<BluetoothDevice> connectedHIDs);
    }

    public interface HIDConnectionCallback {
        void onHIDConnectionStateChanged(BluetoothDevice device, int state);
    }

    private BluetoothProfile bluetoothHIDProxy;
    private Context context;

    private AndroidHIDProfile(Context context) {
        super();
        this.context = context;
        setUpHIDProxy();
        monitorHIDConnection();
    }

    @Override
    protected void finalize() throws Throwable {
        cleanUpHIDProxy();
        stopMonitorHIDConnection();
        super.finalize();
    }

    private BluetoothProfile.ServiceListener profileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(TAG, "BluetoothProfile - onServiceConnected - profile: " + profile);
            bluetoothHIDProxy = proxy;
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.d(TAG, "BluetoothProfile - onServiceDisconnected - profile: " + profile);
            bluetoothHIDProxy = null;
        }
    };

    private void setUpHIDProxy() {
        int profileConnectionState = BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(4 /*BluetoothProfile.INPUT_DEVICE */);
        boolean result = BluetoothAdapter.getDefaultAdapter().getProfileProxy(this.context, profileServiceListener, 4);
        Log.d(TAG, "setUpHIDProxy - profileConnectionState: " + profileConnectionState + ", getProxy: " + result);
    }

    private void cleanUpHIDProxy() {
        BluetoothAdapter.getDefaultAdapter().closeProfileProxy(4, bluetoothHIDProxy);
    }

    private void monitorHIDConnection() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
        this.context.registerReceiver(mHIDConnectionListener, intentFilter);
    }

    private void stopMonitorHIDConnection() {
        this.context.unregisterReceiver(mHIDConnectionListener);
    }

    private BroadcastReceiver mHIDConnectionListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int prev = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, BluetoothProfile.STATE_DISCONNECTED);
            int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (state) {
                case BluetoothProfile.STATE_CONNECTING:
                    Log.i(TAG, "HID Connecting " + device.getAddress() + ", prev: " + prev);
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "HID Connected " + device.getAddress() + " prev: " + prev);
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    Log.i(TAG, "HID Disconnecting " + device.getAddress() + " , prev: " + prev);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "HID Disconnected " + device.getAddress() + " , prev: " + prev);
                    break;
            }

            HIDConnectionCallback callback = mConnectionCallbacks.get(device.getAddress());
            if (callback != null) {
                callback.onHIDConnectionStateChanged(device, state);
            }
        }
    };

    /**
     * Connected Devices
     */
    private int mNumberOfAttempts;

    public void getConnectedHIDDevices(RetrieveConnectedHIDCallback callback) {
        mNumberOfAttempts = 5;
        getConnectedHIDDevicesLoop(callback);
    }

    private void getConnectedHIDDevicesLoop(final RetrieveConnectedHIDCallback callback) {
        if (null == bluetoothHIDProxy) {
            if (mNumberOfAttempts > 0) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getConnectedHIDDevicesLoop(callback);
                    }
                }, 500);
                --mNumberOfAttempts;
            }
            return;
        }
        callback.onConnectedHIDRetrieved(bluetoothHIDProxy.getConnectedDevices());
    }

    /**
     * Connection Monitoring
     */
    private HashMap<String, HIDConnectionCallback> mConnectionCallbacks = new HashMap<>();

    public void registerHIDConnectionCallback(BluetoothDevice bluetoothDevice, HIDConnectionCallback callback) {
        mConnectionCallbacks.put(bluetoothDevice.getAddress(), callback);
    }

    /**
     * Connection
     */
    public boolean connect(BluetoothDevice bluetoothDevice) {
        if (null == bluetoothDevice) {
            Log.w(TAG, "No device specified. FIND it first!!!");
            return false;
        }

        if (null == bluetoothHIDProxy) {
            Log.e(TAG, "BluetoothHIDProxy is empty. Error in proxy setup?");
            return false;
        }

        Log.i(TAG, "CONNECT USING HID PROXY");
        boolean result;
        try {
            Method localMethod;
            localMethod = bluetoothHIDProxy.getClass().getMethod("connect", BluetoothDevice.class);
            if (localMethod == null) {
                Log.e(TAG, "localMethod NOT found???");
                return false;
            }

            result = (Boolean) localMethod.invoke(bluetoothHIDProxy, bluetoothDevice);
            Log.d(TAG, "HID Proxy Connect - success?: " + result);
        } catch (Exception localException) {
            Log.e(TAG, "HID Proxy Connect - got exception!");
            localException.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean disconnect(BluetoothDevice bluetoothDevice) {
        if (null == bluetoothDevice) {
            Log.w(TAG, "No device specified. FIND it first!!!");
            return false;
        }

        if (null == bluetoothHIDProxy) {
            Log.w(TAG, "BluetoothHIDProxy is empty. Error in proxy setup?");
            return false;
        }

        Log.d(TAG, "DISCONNECT USING HID PROXY");
        boolean result;
        try {
            Method localMethod;
            localMethod = bluetoothHIDProxy.getClass().getMethod("disconnect", BluetoothDevice.class);
            if (localMethod == null) {
                Log.e(TAG, "localMethod NOT found???");
                return false;
            }

            result = (Boolean) localMethod.invoke(bluetoothHIDProxy, bluetoothDevice);
            Log.d(TAG, "HID Proxy Disconnect - success?: " + result);
        } catch (Exception localException) {
            Log.e(TAG, "HID Proxy Disconnect - got exception!");
            localException.printStackTrace();
            result = false;
        }
        return result;
    }
}
