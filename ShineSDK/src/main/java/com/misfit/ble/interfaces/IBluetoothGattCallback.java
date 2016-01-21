package com.misfit.ble.interfaces;

import android.bluetooth.BluetoothDevice;

public interface IBluetoothGattCallback {
	
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState);

    public void onServicesDiscovered(int status);

    public void onCharacteristicChanged(IBluetoothGattCharacteristic characteristic);

    public void onCharacteristicRead(IBluetoothGattCharacteristic characteristic, int status);
    
    public void onCharacteristicWrite(IBluetoothGattCharacteristic characteristic, int status);
    
    public void onDescriptorWrite(IBluetoothGattDescriptor descriptor, int status);
    
    public void onDescriptorRead(IBluetoothGattDescriptor descriptor, int status);
    
    public void onCharacteristicSubscriptionStateChanged(IBluetoothGattCharacteristic characteristic, boolean enable);
    
    public void onReadRemoteRssi(int rssi, int status);
}
