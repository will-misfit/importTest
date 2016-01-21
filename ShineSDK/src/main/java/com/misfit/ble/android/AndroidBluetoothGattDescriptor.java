package com.misfit.ble.android;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;

import com.misfit.ble.interfaces.IBluetoothGattCharacteristic;
import com.misfit.ble.interfaces.IBluetoothGattDescriptor;

import java.util.HashMap;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AndroidBluetoothGattDescriptor implements IBluetoothGattDescriptor {

	private static HashMap<BluetoothGattDescriptor, AndroidBluetoothGattDescriptor> sCachedInstances 
											= new HashMap<BluetoothGattDescriptor, AndroidBluetoothGattDescriptor>();

	public static AndroidBluetoothGattDescriptor getInstance(BluetoothGattDescriptor descriptor) {
		AndroidBluetoothGattDescriptor object = sCachedInstances.get(descriptor);
		if (object == null) {
			object = new AndroidBluetoothGattDescriptor(descriptor);
			sCachedInstances.put(descriptor, object);
		}
		return object;
	}

	private BluetoothGattDescriptor mDescriptor;

	private AndroidBluetoothGattDescriptor(BluetoothGattDescriptor descriptor) {
		mDescriptor = descriptor;
	}

	@Override
	public Object getRealInstance() {
		return mDescriptor;
	}

	@Override
	public boolean equals(Object o) {
		if (getClass().equals(o.getClass())) {
			AndroidBluetoothGattDescriptor other = (AndroidBluetoothGattDescriptor)o;
			return mDescriptor.equals(other.mDescriptor);
		}
		return false;
	}

	@Override
	public IBluetoothGattCharacteristic getCharacteristic() {
		BluetoothGattCharacteristic characteristic = mDescriptor.getCharacteristic();
		if (characteristic == null)
			return null;
		
		return AndroidBluetoothGattCharacteristic.getInstance(characteristic);
	}

	@Override
	public String getUUID() {
		return mDescriptor.getUuid().toString();
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
