package com.misfit.ble.shine;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.util.Log;

import com.misfit.ble.encryption.TextEncryption;
import com.misfit.ble.shine.storage.InternalStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by minh on 7/17/15.
 */
public class ShineDeviceFactory {
    private static final String TAG = ShineDeviceFactory.class.getSimpleName();
    private static final String DEVICES_CACHE_FILENAME = "com.misfit.ble.devices";

    private static HashMap<String, ShineDevice> sDevicesCache = new HashMap<>();

    static {
        loadDevicesCache();
    }

    public static ShineDevice getShineDevice(BluetoothDevice bluetoothDevice, String serialNumber) {
        if (bluetoothDevice == null || TextUtils.isEmpty(serialNumber))
            return null;

        ShineDevice device = getCachedDevice(bluetoothDevice.getAddress());
        if (device == null) {
            device = new ShineDevice(bluetoothDevice, serialNumber);
            sDevicesCache.put(bluetoothDevice.getAddress(), device);
        }
        return device;
    }

    public static ShineDevice getShineDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null)
            return null;

        ShineDevice device = getCachedDevice(bluetoothDevice.getAddress());
        if (device == null) {
            device = new ShineDevice(bluetoothDevice);
            sDevicesCache.put(bluetoothDevice.getAddress(), device);
        }
        return device;
    }

    public static ShineDevice getShineDevice(String macAddress, String serialNumber) {
        if (macAddress == null)
            return null;

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "BluetoothAdapter.getDefaultAdapter is null");
            return null;
        }

        BluetoothDevice bluetoothDevice = adapter.getRemoteDevice(macAddress);
        if (serialNumber == null) {
            return getShineDevice(bluetoothDevice);
        } else {
            return getShineDevice(bluetoothDevice, serialNumber);
        }
    }

    /*package*/ static ShineDevice getCachedDevice(String macAddress) {
        return sDevicesCache.get(macAddress);
    }

    /*package*/ static void saveDevicesCache() {
        JSONArray array = new JSONArray();
        for (String macAddress : sDevicesCache.keySet()) {
            JSONObject json = new JSONObject();
            try {
                json.put(ShineDevice.MAC_ADDRESS_KEY, macAddress);
                json.put(ShineDevice.SERIAL_NUMBER_KEY, sDevicesCache.get(macAddress).getSerialNumber());
            } catch (Exception e) {
                e.printStackTrace();
            }
            array.put(json);
        }

        String content = array.toString();
        String encrypted = TextEncryption.encrypt(content);
        InternalStorage.saveTextToCacheFile(encrypted, DEVICES_CACHE_FILENAME);
    }

    private static void loadDevicesCache() {
        String encrypted = InternalStorage.readTextFromCacheFile(DEVICES_CACHE_FILENAME);
        if (encrypted == null)
            return;

        String decrypted = TextEncryption.decrypt(encrypted);
        if (decrypted == null)
            return;

        try {
            JSONArray array = new JSONArray(decrypted);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String macAddress = obj.getString(ShineDevice.MAC_ADDRESS_KEY);
                String serialNumber = obj.getString(ShineDevice.SERIAL_NUMBER_KEY);

                // Load devicesCache
                getShineDevice(macAddress, serialNumber);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
