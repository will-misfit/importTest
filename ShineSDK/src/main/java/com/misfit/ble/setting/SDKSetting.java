package com.misfit.ble.setting;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.misfit.ble.BuildConfig;
import com.misfit.ble.android.AndroidGattProfile;
import com.misfit.ble.android.AndroidHIDProfile;
import com.misfit.ble.interfaces.BluetoothGattFactory;
import com.misfit.ble.sdk.GlobalVars;

public class SDKSetting {

	static {
		System.loadLibrary("CRCCalculator");
		System.loadLibrary("SwimLap");
	}

	private static String mUserId; 
	private final static String UNUSUAL_USER_ID = "00000000-0000-0000-0000-000000000000";

	/**
	 * 
	 * @param applicationContext
	 * @param userId as the bridge between dc log and sync log
	 * @throws IllegalArgumentException
     */
	public static void setUp(Context applicationContext, @NonNull String userId) throws IllegalArgumentException {
		setUserId(userId);
		setApplicationContext(applicationContext);

		AndroidHIDProfile.setUp(applicationContext);
		AndroidGattProfile.setUp(applicationContext);
	}
	
	public static String getUserId() {
		return mUserId;
	}

	private static void setUserId(String userId) throws IllegalArgumentException {
		String pattern = "[\\w\\d@\\._-]{0,30}";
        if (userId.matches(pattern) == false) {
        	userId = UNUSUAL_USER_ID;
        }
		SDKSetting.mUserId = userId;
	}
	
	private static void setApplicationContext(Context applicationContext) {
		if (applicationContext == null 
				|| applicationContext != applicationContext.getApplicationContext()) {
			throw new IllegalArgumentException("Invalid application context.");
		}
		GlobalVars.setUpApplicationContext(applicationContext);
	}
	
	public static void validateSettings() throws IllegalStateException {
		if (mUserId == null) {
			throw new IllegalStateException("userId is NOT specified yet.");
		} else if (GlobalVars.getApplicationContext() == null) {
			throw new IllegalStateException("applicationContext is NOT specified yet.");
		}
	}

	public static String getSDKVersion() {
		return BuildConfig.VERSION_NAME + "-" + BuildConfig.FLAVOR + "-" + BuildConfig.BUILD_TYPE;
	}

	/**
	 * public API to check current h/w and Android O/S whether support Bluetooth LE
	 * 0. the min Android SDK version in build.gradle is JELLY_BEAN_MR1(Android 4.2)
	 * 1. h/w supports Bluetooth or not
	 * 2. on Android 4.2, has Samsung BLE SDK v2 or not
	 * 3. on higher Android, support FEATURE_BLUETOOTH_LE or not
	 */
	public static boolean isBleSupported(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context should not be null.");
		}

		if (BluetoothAdapter.getDefaultAdapter() == null) {
			return false;
		}

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return BluetoothGattFactory.isClassExist("com.samsung.android.sdk.bt.gatt.BluetoothGatt");
		}

		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
	}
}
