package com.misfit.ble.samsung.v2;

import com.misfit.ble.interfaces.IBluetoothGattCharacteristic;
import com.misfit.ble.interfaces.IBluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;

import java.util.HashMap;

public class SamsungBluetoothGattDescriptor_v2 implements IBluetoothGattDescriptor {
	
	private static HashMap<BluetoothGattDescriptor, SamsungBluetoothGattDescriptor_v2> sCachedInstances 
															= new HashMap<BluetoothGattDescriptor, SamsungBluetoothGattDescriptor_v2>();

	public static SamsungBluetoothGattDescriptor_v2 getInstance(BluetoothGattDescriptor descriptor) {
		SamsungBluetoothGattDescriptor_v2 object = sCachedInstances.get(descriptor);
		if (object == null) {
			object = new SamsungBluetoothGattDescriptor_v2(descriptor);
			sCachedInstances.put(descriptor, object);
		}
		return object;
	}

	private BluetoothGattDescriptor mDescriptor;
	
	private SamsungBluetoothGattDescriptor_v2(BluetoothGattDescriptor descriptor) {
		mDescriptor = descriptor;
	}
	
	@Override
	public Object getRealInstance() {
		return mDescriptor;
	}
	
	@Override
	public boolean equals(Object o) {
		if (getClass().equals(o.getClass())) {
			SamsungBluetoothGattDescriptor_v2 other = (SamsungBluetoothGattDescriptor_v2)o;
			return mDescriptor.equals(other.mDescriptor);
		}
		return false;
	}
	
	@Override
	public String getUUID() {
		return mDescriptor.getUuid().toString();
	}
	
	@Override
	public IBluetoothGattCharacteristic getCharacteristic() {
		BluetoothGattCharacteristic characteristic = mDescriptor.getCharacteristic();
		if (characteristic == null)
			return null;
		
		return SamsungBluetoothGattCharacteristic_v2.getInstance(characteristic);
	}

	@Override
	public boolean setValue(byte[] bytes) {
		return mDescriptor.setValue(bytes);
	}

	@Override
	public byte[] getValue() {
		return mDescriptor.getValue();
	}
	
}
