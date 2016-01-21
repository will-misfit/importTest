package com.misfit.ble.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import java.util.List;
import java.util.UUID;

public interface IBluetoothGattAdapter {
	interface IBluetoothGattScanCallback {
		// interface method of android.bluetooth.LeScanCallback
		void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);

		// interface methods of android.bluetooth.ScanCallback
		void onBatchScanResults(List<ScanResult> results);
		void onScanFailed(int errorCode);
		void onScanResult(int callbackType, ScanResult result);
	}
	
	boolean startScanning(IBluetoothGattScanCallback callback);
	boolean startScanning(UUID[] serviceUuids, IBluetoothGattScanCallback callback);
	void stopScanning(IBluetoothGattScanCallback callback);
}
