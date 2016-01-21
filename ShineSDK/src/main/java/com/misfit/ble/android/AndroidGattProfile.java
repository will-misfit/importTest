package com.misfit.ble.android;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by minh on 8/21/15.
 */
public class AndroidGattProfile {
    private static final String TAG = AndroidGattProfile.class.getName();
    private static AndroidGattProfile sharedInstance;

    public static void setUp(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new AndroidGattProfile(context);
        }
        sharedInstance.context = context;
    }

    public static AndroidGattProfile getSharedInstance() {
        return sharedInstance;
    }

    private Context context;

    private AndroidGattProfile(Context context) {
        super();
        this.context = context;
    }

    /**
     * Connected Devices
     */
    public ArrayList<BluetoothDevice> getConnectedGattDevices() {
        ArrayList<BluetoothDevice> leDevices = new ArrayList<>();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return leDevices;
        }

        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);

        for(BluetoothDevice device : devices) {
            if(device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                leDevices.add(device);
            }
        }
        return leDevices;
    }

    public int getConnectionState(BluetoothDevice bluetoothDevice) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return -1;
        }

        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager.getConnectionState(bluetoothDevice, BluetoothProfile.GATT);
    }
}
