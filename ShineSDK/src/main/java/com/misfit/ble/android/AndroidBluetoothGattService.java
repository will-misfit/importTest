package com.misfit.ble.android;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;

import com.misfit.ble.interfaces.IBluetoothGattCharacteristic;
import com.misfit.ble.interfaces.IBluetoothGattService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AndroidBluetoothGattService implements IBluetoothGattService {

	private static HashMap<BluetoothGattService, AndroidBluetoothGattService> sCachedInstances 
													= new HashMap<BluetoothGattService, AndroidBluetoothGattService>();

	public static AndroidBluetoothGattService getInstance(BluetoothGattService service) {
		AndroidBluetoothGattService object = sCachedInstances.get(service);
		if (object == null) {
			object = new AndroidBluetoothGattService(service);
			sCachedInstances.put(service, object);
		}
		return object;
	}

	private BluetoothGattService mService;

	private AndroidBluetoothGattService(BluetoothGattService service) {
		mService = service;
	}

	@Override
	public Object getRealInstance() {
		return mService;
	}

	@Override
	public boolean equals(Object o) {
		if (getClass().equals(o.getClass())) {
			AndroidBluetoothGattService other = (AndroidBluetoothGattService)o;
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
		List<BluetoothGattCharacteristic> characteristics = mService.getCharacteristics();
		
		List<IBluetoothGattCharacteristic> iCharacteristics = new ArrayList<IBluetoothGattCharacteristic>();
		for (BluetoothGattCharacteristic characteristic : characteristics) {
			iCharacteristics.add(AndroidBluetoothGattCharacteristic.getInstance(characteristic));
		}
		return iCharacteristics;
	}

	@Override
	public IBluetoothGattCharacteristic getCharacteristic(String characteristicUUID) {
		BluetoothGattCharacteristic characteristic = mService.getCharacteristic(UUID.fromString(characteristicUUID));
		if (characteristic == null)
			return null;
		
		return AndroidBluetoothGattCharacteristic.getInstance(characteristic);
	}
}
