package com.misfit.ble.samsung.v2;

import com.misfit.ble.interfaces.IBluetoothGattCharacteristic;
import com.misfit.ble.interfaces.IBluetoothGattService;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SamsungBluetoothGattService_v2 implements IBluetoothGattService {

	private static HashMap<BluetoothGattService, SamsungBluetoothGattService_v2> sCachedInstances 
															= new HashMap<BluetoothGattService, SamsungBluetoothGattService_v2>();

	public static SamsungBluetoothGattService_v2 getInstance(BluetoothGattService service) {
		SamsungBluetoothGattService_v2 object = sCachedInstances.get(service);
		if (object == null) {
			object = new SamsungBluetoothGattService_v2(service);
			sCachedInstances.put(service, object);
		}
		return object;
	}

	private BluetoothGattService mService;
	
	private SamsungBluetoothGattService_v2(BluetoothGattService service) {
		mService = service;
	}
	
	@Override
	public Object getRealInstance() {
		return mService;
	}
	
	@Override
	public boolean equals(Object o) {
		if (getClass().equals(o.getClass())) {
			SamsungBluetoothGattService_v2 other = (SamsungBluetoothGattService_v2)o;
			return mService.equals(other.mService);
		}
		return false;
	}
	
	@Override
	public String getUUID() {
		return mService.getUuid().toString();
	}

	@Override
	public List<IBluetoothGattCharacteristic> getCharacteristic() {
		@SuppressWarnings("unchecked")
		List<BluetoothGattCharacteristic> characteristics = mService.getCharacteristics();
		if (characteristics == null)
			return null;
		
		List<IBluetoothGattCharacteristic> iCharacteristics = new ArrayList<IBluetoothGattCharacteristic>();
		for (BluetoothGattCharacteristic characteristic : characteristics) {
			iCharacteristics.add(SamsungBluetoothGattCharacteristic_v2.getInstance(characteristic));
		}
		return iCharacteristics;
	}

	@Override
	public IBluetoothGattCharacteristic getCharacteristic(String characteristicUUID) {
		BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID.fromString(characteristicUUID));
		if (characteristic == null)
			return null;
		
		return SamsungBluetoothGattCharacteristic_v2.getInstance(characteristic);
	}

}
