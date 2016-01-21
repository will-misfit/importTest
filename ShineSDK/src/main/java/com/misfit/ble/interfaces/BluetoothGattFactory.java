package com.misfit.ble.interfaces;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.misfit.ble.android.AndroidBluetoothGattAdapter;
import com.misfit.ble.android.AndroidBluetoothProfile;
import com.misfit.ble.samsung.v2.SamsungBluetoothGattAdapter_v2;
import com.misfit.ble.samsung.v2.SamsungBluetoothProfile_v2;

public class BluetoothGattFactory {
	
	public static IBluetoothGattProfile buildBluetoothProfile(Context context, BluetoothDevice bluetoothDevice) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null)
			return null;
		
		boolean isSamsungBleSdkv2 = isClassExist("com.samsung.android.sdk.bt.gatt.BluetoothGatt");
		if (isSamsungBleSdkv2) {
			return new SamsungBluetoothProfile_v2(context, bluetoothDevice);
		}
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return new AndroidBluetoothProfile(context, bluetoothDevice);
		}
		
		return null;
	}
	
	public static IBluetoothGattAdapter buildBluetoothAdapter(Context context) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null)
			return null;

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return AndroidBluetoothGattAdapter.getDefaultAdapter();
		} else {
			boolean isSamsungBleSdkv2 = isClassExist("com.samsung.android.sdk.bt.gatt.BluetoothGatt");
			if (isSamsungBleSdkv2) {
				return SamsungBluetoothGattAdapter_v2.getDefaultAdapter(context);
			}
		}

		return null;
	}
	
	public static boolean isClassExist(String className) {
		boolean exist = true;
		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {
			exist = false;
		}
		return exist;
	}
}
