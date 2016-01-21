package com.misfit.ble.android;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;

import com.misfit.ble.interfaces.IBluetoothGattAdapter;
import com.misfit.ble.sdk.GlobalVars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AndroidBluetoothGattAdapter implements IBluetoothGattAdapter{
	/*
	 * Nested Class to wrap BluetoothAdapter.LeScanCallback
	 */
	private static class LEScanCallbackWrapper implements BluetoothAdapter.LeScanCallback {

		private static HashMap<IBluetoothGattScanCallback, LEScanCallbackWrapper> mCache = new HashMap<>();

		private static LEScanCallbackWrapper getInstance(IBluetoothGattScanCallback callback) {
			LEScanCallbackWrapper wrapper = mCache.get(callback);
			if (wrapper == null) {
				wrapper = new LEScanCallbackWrapper();
				wrapper.mCallback = callback;

				mCache.put(callback, wrapper);
			}
			return wrapper;
		}

		private static void flushCachedInstance(IBluetoothGattScanCallback callback) {
			mCache.remove(callback);
		}

		private IBluetoothGattScanCallback mCallback;
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			mCallback.onLeScan(device, rssi, scanRecord);
		}
	}
	
	/**
	 * Nested class to wrap android.bluetooth.le.ScanCallback
	 * */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static class ScanCallbackWrapper extends ScanCallback {
		private static HashMap<IBluetoothGattScanCallback, ScanCallbackWrapper> mCache = new HashMap<>();

		private static ScanCallbackWrapper getInstance(IBluetoothGattScanCallback callback) {
			ScanCallbackWrapper wrapper = mCache.get(callback);
			if (wrapper == null) {
				wrapper = new ScanCallbackWrapper();
				wrapper.mCallback = callback;
				mCache.put(callback, wrapper);
			}
			return wrapper;
		}

		private IBluetoothGattScanCallback mCallback;

		// interface methods of android.bluetooth.BlueLeScanner.ScanCallback
		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			mCallback.onBatchScanResults(results);
		}

		@Override
		public void onScanFailed(int errorCode) {
			mCallback.onScanFailed(errorCode);
		}

		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			mCallback.onScanResult(callbackType, result);
		}

		private static void flushCachedInstance(IBluetoothGattScanCallback callback) {
			mCache.remove(callback);
		}
	}

	/*
	 * Class Level
	 */
	private static AndroidBluetoothGattAdapter sDefaultAdapter = null;

	public static AndroidBluetoothGattAdapter getDefaultAdapter() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// BluetoothAdapter.getDefaultAdapter() returns null if Bluetooth is not supported on h/w
		if (bluetoothAdapter == null)
			return null;
		
		if (sDefaultAdapter == null) {
			sDefaultAdapter = new AndroidBluetoothGattAdapter(); 
		}
		return sDefaultAdapter;
	}
	
	/*
	 * Instance Level
	 */
	private BluetoothAdapter mBluetoothAdapter;
	private ScanSettings mScanSettings;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private AndroidBluetoothGattAdapter() {
		mBluetoothAdapter = getSystemBluetoothAdapter();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(); // scan interval is 1/10 of LOW_POWER mode
		}
	}

	/**
	 * to retrieve the BluetoothAdapter representing the local Bluetooth adapter
	 * for Android version >= JELLY_BEAN_MR2(API 18), getSystemService(Class) with BLUETOOTH_SERVICE
	 */
	private BluetoothAdapter getSystemBluetoothAdapter() {
		Context context = GlobalVars.getApplicationContext();
		if (context == null) {
			return BluetoothAdapter.getDefaultAdapter(); // return null only if h/w does not support Bluetooth
		} else {
			BluetoothManager btManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
			return btManager.getAdapter();
		}
	}

	@Override
	public boolean startScanning(IBluetoothGattScanCallback callback) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return mBluetoothAdapter.startLeScan(LEScanCallbackWrapper.getInstance(callback));
		} else {
			List<ScanFilter> filters = new ArrayList<>();
			//ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(Constants.MFSERVICE_UUID_WITHOUT_SEPERATOR_UPPERCASED)).build();
			mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, mScanSettings, ScanCallbackWrapper.getInstance(callback));
			return true;
		}
	}

	@Override
	public boolean startScanning(UUID[] serviceUuids, IBluetoothGattScanCallback callback) {
		return mBluetoothAdapter.startLeScan(serviceUuids, LEScanCallbackWrapper.getInstance(callback));
	}

	@Override
	public void stopScanning(IBluetoothGattScanCallback callback) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			mBluetoothAdapter.stopLeScan(LEScanCallbackWrapper.getInstance(callback));
			LEScanCallbackWrapper.flushCachedInstance(callback);
		} else {
			mBluetoothAdapter.getBluetoothLeScanner().stopScan(ScanCallbackWrapper.getInstance(callback));
			ScanCallbackWrapper.flushCachedInstance(callback);
		}
	}
}
