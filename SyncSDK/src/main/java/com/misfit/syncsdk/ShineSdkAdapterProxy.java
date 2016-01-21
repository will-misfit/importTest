package com.misfit.syncsdk;

import android.content.Context;
import android.util.Log;

import com.misfit.ble.shine.ShineAdapter;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineAdapter.ShineScanCallback;

import java.util.List;

/**
 * proxy of com.misfit.ble.shine.ShineAdapter, part of previous com.misfitwearables.ShineSDKProvider
 */
public class ShineSdkAdapterProxy {
    private final static String TAG = "ShineSdkAdapterProxy";

    protected ShineAdapter mShineAdapter;
    protected ShineAdapter.ShineScanCallback mShineScanCallback;
    protected Context mContext;

    public ShineSdkAdapterProxy(Context context) {
        mShineAdapter = ShineAdapter.getDefaultAdapter(context);
        //FIXME:we don't need to save it!
        mContext = context.getApplicationContext();
    }

    public void startScanning(ShineScanCallback scanCallback) {
        if (scanCallback != null && mShineAdapter != null) {
            mShineScanCallback = scanCallback;
            mShineAdapter.startScanning(mShineScanCallback);
            retrieveConnectedDevices(scanCallback);
        } else {
            Log.d(TAG, "Can't start scanning, mShineAdapter = null || mShineScanCallback = null");
        }
    }

    private void retrieveConnectedDevices(final ShineScanCallback scanCallback) {
        if (scanCallback != null && mShineAdapter != null) {
            Log.d(TAG, "Get connected shines");
            mShineAdapter.getConnectedShines(new ShineAdapter.ShineRetrieveCallback() {
                @Override
                public void onConnectedShinesRetrieved(List<ShineDevice> connectedShines) {
                    Log.d(TAG, connectedShines.size() + " connected shine(s)");
                    for (ShineDevice device : connectedShines) {
                        if (device.getSerialNumber() != null && device.getSerialNumber().length() > 0) {
                            if (scanCallback != null) {
                                scanCallback.onScanResult(device, 0);
                            }
                        } else {
                            connectToReadSerialNumber(device, scanCallback);
                        }
                    }
                }
            });
        } else {
            Log.d(TAG, "can't retrieve connectedShine, mShineAdapter = null || mShineScanCallback = null");
        }
    }

    private void connectToReadSerialNumber(final ShineDevice device, final ShineScanCallback scanCallback) {
        Log.d(TAG, "Connect to read serialNumber. Context: " + mContext + " Device: " + device);

        device.connectProfile(mContext, true, new ShineProfile.ConnectionCallback() {
            @Override
            public void onConnectionStateChanged(ShineProfile shineProfile, ShineProfile.State state) {
                if (state == ShineProfile.State.CONNECTED) {
                    // pqnga: at this step, it is expected that the device has a correct serialNumber
                    Log.d(TAG, "SerialNumber: " + device.getSerialNumber());
                    if (shineProfile != null) {
                        shineProfile.close();
                    }

                    if (device.getSerialNumber() != null
                            && device.getSerialNumber().length() > 0
                            && scanCallback != null) {
                        scanCallback.onScanResult(device, 0);
                    }
                }
            }
        });
    }

    public void stopScanning() {
        if (mShineAdapter != null) {
            mShineAdapter.stopScanning(mShineScanCallback);
        } else {
            Log.d(TAG, "can't stop scanning, mShineAdapter = null");
        }
    }
}
