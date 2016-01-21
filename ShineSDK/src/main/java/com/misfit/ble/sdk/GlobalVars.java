package com.misfit.ble.sdk;

import android.content.Context;
import android.os.Build;

import com.misfit.ble.setting.SDKSetting;
import com.misfit.ble.util.Helper;

public class GlobalVars {

	/*
	 * Class Level
	 */
	private static Context sApplicationContext;
	private static String sSDKVersion;
	private static String sDeviceName;
	private static String sDeviceModel;
	private static String sSystemVersion;
	private static int sSystemAPILevel;
	
	private GlobalVars() {}
	
	public static void setUpApplicationContext(Context context) {
		if (context == null)
			return;
		
		Context applicationContext = context.getApplicationContext();
		if (applicationContext == null)
			return;
		
		sApplicationContext = applicationContext;
	}
	
	public static Context getApplicationContext() {
		return sApplicationContext;
	}
	
	public static String getSDKVersion() {
		if (sSDKVersion == null) {
			sSDKVersion = SDKSetting.getSDKVersion();
		}
		return sSDKVersion;
	}
	
	public static String getDeviceName() {
		if (sDeviceName == null) {
			sDeviceName = Helper.getDeviceName();
		}
		return sDeviceName;
	}
	
	public static String getDeviceModel() {
		if (sDeviceModel == null) {
			sDeviceModel = Helper.getDeviceModel();
		}
		return sDeviceModel;
	}
	
	public static int getSystemAPILevel() {
		if (sSystemAPILevel <= 0) {
			sSystemAPILevel = Build.VERSION.SDK_INT;
		}
		return sSystemAPILevel;
	}
	
	public static String getSystemVersion() {
		if (sSystemVersion == null) {
			sSystemVersion = Build.VERSION.RELEASE;
		}
		return (sSystemVersion != null) ? sSystemVersion : "unknown";
	}
}
