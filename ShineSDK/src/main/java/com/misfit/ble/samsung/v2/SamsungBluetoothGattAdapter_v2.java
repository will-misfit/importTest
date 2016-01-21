package com.misfit.ble.samsung.v2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.misfit.ble.interfaces.IBluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class SamsungBluetoothGattAdapter_v2 implements IBluetoothGattAdapter {
	
	/*
	 * Class Level
	 */
	private static SamsungBluetoothGattAdapter_v2 sDefaultAdapter = null;
	public static SamsungBluetoothGattAdapter_v2 getDefaultAdapter(Context context) {
		if (context == null)
			return null;
		
		if (sDefaultAdapter == null) {
			sDefaultAdapter = new SamsungBluetoothGattAdapter_v2(context);
		}
		return sDefaultAdapter;
	}
	
	/*
	 * Instance Level
	 */
	private boolean isTryingToStartScanning = false;
	
	private IBluetoothGattScanCallback mMisfitGattScanCallback;
	private BluetoothGatt mBluetoothGatt = null;
	
	private SamsungBluetoothGattAdapter_v2(Context context) {
		BluetoothGattAdapter.getProfileProxy(context, mProfileServiceListener, BluetoothGattAdapter.GATT);
	}
	
	private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
		@Override
		public void onAppRegistered(int status) {};
		
		@Override
		public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
			mMisfitGattScanCallback.onLeScan(device, rssi, scanRecord);
		};
	};
	
	/**
     * Profile Listener
     */
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @SuppressLint("NewApi")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothGattAdapter.GATT) {
                mBluetoothGatt = (BluetoothGatt) proxy;
                mBluetoothGatt.registerApp(mBluetoothGattCallback);;
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothGattAdapter.GATT) {
            	mBluetoothGatt = null;
            }
        }
    };
	
	public void close() {
		if (mBluetoothGatt == null)
    		return;
		
		mBluetoothGatt.unregisterApp();
		BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT, mBluetoothGatt);
	}

	@Override
	public boolean startScanning(IBluetoothGattScanCallback callback) {
		isTryingToStartScanning = true;
		
		// Wait for BluetoothGattCallback.onAppRegistered at most 3 seconds
		return startScanning(callback, 3);
	}

	@Override
	public boolean startScanning(UUID[] serviceUuids, IBluetoothGattScanCallback callback) {
		return startScanning(callback);
	}
	
	private boolean startScanning(final IBluetoothGattScanCallback callback, final int retriesLeft) {
		if (isTryingToStartScanning == false)
			return false;

		boolean result = true;
		if (mBluetoothGatt == null) {
			new Timer().schedule(new TimerTask() {	
				@Override
				public void run() {
					startScanning(callback, retriesLeft - 1);
				}
			}, 1000);
		} else {
			mMisfitGattScanCallback = callback;
			result = mBluetoothGatt.startScan();
		}
		return result;
	}
	
	@Override
	public void stopScanning(IBluetoothGattScanCallback callback) {
		isTryingToStartScanning = false;
		
		if (mBluetoothGatt != null) {
			mBluetoothGatt.stopScan();
		}
		mMisfitGattScanCallback = null;
	}
	
	// NOTE: Possible dead code
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
