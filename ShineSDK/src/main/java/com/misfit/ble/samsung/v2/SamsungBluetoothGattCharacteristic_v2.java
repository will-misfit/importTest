package com.misfit.ble.samsung.v2;

import com.misfit.ble.interfaces.IBluetoothGattCharacteristic;
import com.misfit.ble.interfaces.IBluetoothGattDescriptor;
import com.misfit.ble.interfaces.IBluetoothGattService;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SamsungBluetoothGattCharacteristic_v2 implements IBluetoothGattCharacteristic {
	
	private static HashMap<BluetoothGattCharacteristic, SamsungBluetoothGattCharacteristic_v2> sCachedInstances 
											= new HashMap<BluetoothGattCharacteristic, SamsungBluetoothGattCharacteristic_v2>();
	 
	public static SamsungBluetoothGattCharacteristic_v2 getInstance(BluetoothGattCharacteristic characteristic) {
		SamsungBluetoothGattCharacteristic_v2 object = sCachedInstances.get(characteristic);
		if (object == null) {
			object = new SamsungBluetoothGattCharacteristic_v2(characteristic);
			sCachedInstances.put(characteristic, object);
		}
		return object;
	}
	
	private BluetoothGattCharacteristic mCharacteristic;
	
	private SamsungBluetoothGattCharacteristic_v2(BluetoothGattCharacteristic characteristic) {
		mCharacteristic = characteristic;
	}
	
	@Override
	public Object getRealInstance() {
		return mCharacteristic;
	}
	
	@Override
	public boolean equals(Object o) {
		if (getClass().equals(o.getClass())) {
			SamsungBluetoothGattCharacteristic_v2 other = (SamsungBluetoothGattCharacteristic_v2)o;
			return mCharacteristic.equals(other.mCharacteristic);
		}
		return false;
	}

	@Override
	public IBluetoothGattService getService() {
		BluetoothGattService service = mCharacteristic.getService();
		if (service == null)
			return null;
		
		return SamsungBluetoothGattService_v2.getInstance(service);
	}
	
	@Override
	public String getUUID() {
		return mCharacteristic.getUuid().toString();
	}

	@Override
	public List<IBluetoothGattDescriptor> getDescriptors() {
		@SuppressWarnings("unchecked")
		List<BluetoothGattDescriptor> descriptors = mCharacteristic.getDescriptors();
		if (descriptors == null)
			return null;
		
		List<IBluetoothGattDescriptor> iDescriptors = new ArrayList<IBluetoothGattDescriptor>();
		for (BluetoothGattDescriptor descriptor : descriptors) {
			iDescriptors.add(SamsungBluetoothGattDescriptor_v2.getInstance(descriptor));
		}
		return iDescriptors;
	}

	@Override
	public IBluetoothGattDescriptor getDescriptor(String descriptorUUID) {
		BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(UUID.fromString(descriptorUUID));
		if (descriptor == null)
			return null;
		
		return SamsungBluetoothGattDescriptor_v2.getInstance(descriptor);
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
