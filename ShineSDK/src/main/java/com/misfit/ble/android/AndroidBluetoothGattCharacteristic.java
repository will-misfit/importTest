package com.misfit.ble.android;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;

import com.misfit.ble.interfaces.IBluetoothGattCharacteristic;
import com.misfit.ble.interfaces.IBluetoothGattDescriptor;
import com.misfit.ble.interfaces.IBluetoothGattService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AndroidBluetoothGattCharacteristic implements IBluetoothGattCharacteristic {

	// FIXME: manage all the mappings between underlying-wrapper classes in a central place called "Mapper" 
	// Mapper associates with BluetoothProfile in a 1-1 relationship. 
	// So that we can clear all these mapping on BluetoothProfile.close()  
	private static HashMap<BluetoothGattCharacteristic, AndroidBluetoothGattCharacteristic> sCachedInstances 
										= new HashMap<BluetoothGattCharacteristic, AndroidBluetoothGattCharacteristic>();

	public static AndroidBluetoothGattCharacteristic getInstance(BluetoothGattCharacteristic characteristic) {
		AndroidBluetoothGattCharacteristic object = sCachedInstances.get(characteristic);
		if (object == null) {
			object = new AndroidBluetoothGattCharacteristic(characteristic);
			sCachedInstances.put(characteristic, object);
		}
		return object;
	}

	private BluetoothGattCharacteristic mCharacteristic;

	private AndroidBluetoothGattCharacteristic(BluetoothGattCharacteristic characteristic) {
		mCharacteristic = characteristic;
	}

	@Override
	public Object getRealInstance() {
		return mCharacteristic;
	}

	@Override
	public boolean equals(Object o) {
		if (getClass().equals(o.getClass())) {
			AndroidBluetoothGattCharacteristic other = (AndroidBluetoothGattCharacteristic)o;
			return mCharacteristic.equals(other.mCharacteristic);
		}
		return false;
	}

	@Override
	public IBluetoothGattService getService() {
		BluetoothGattService service = mCharacteristic.getService();
		if (service == null)
			return null;
		
		return AndroidBluetoothGattService.getInstance(service);
	}

	@Override
	public List<IBluetoothGattDescriptor> getDescriptors() {
		List<BluetoothGattDescriptor> descriptors = mCharacteristic.getDescriptors();
		
		List<IBluetoothGattDescriptor> iDescriptors = new ArrayList<IBluetoothGattDescriptor>();
		for (BluetoothGattDescriptor descriptor : descriptors) {
			iDescriptors.add(AndroidBluetoothGattDescriptor.getInstance(descriptor));
		}
		return iDescriptors;
	}

	@Override
	public IBluetoothGattDescriptor getDescriptor(String descriptorUUID) {
		BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(UUID.fromString(descriptorUUID));
		if (descriptor == null)
			return null;
		
		return AndroidBluetoothGattDescriptor.getInstance(descriptor);
	}

	@Override
	public String getUUID() {
		return mCharacteristic.getUuid().toString();
	}

	@Override
	public boolean setValue(byte[] bytes) {
		return mCharacteristic.setValue(bytes);
	}

	@Override
	public byte[] getValue() {
		return mCharacteristic.getValue();
	}

	@Override
	public String getStringValue(int offset) {
		return mCharacteristic.getStringValue(offset);
	}

	@Override
	public void setWriteType(int writeType) {
		mCharacteristic.setWriteType(writeType);
	}
	
	@Override
	public int getWriteType() {
		return mCharacteristic.getWriteType();
	}
	
	@Override
	public int getProperties() {
		return mCharacteristic.getProperties();
	}
}
