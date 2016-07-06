package com.misfit.ble.shine.compatibility;

import android.text.TextUtils;

import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.request.GetActivityTaggingStateRequest;
import com.misfit.ble.shine.request.GetBatteryRequest;
import com.misfit.ble.shine.request.GetClockStateRequest;
import com.misfit.ble.shine.request.GetTripleTapEnableRequest;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.request.SerialNumberChangeAndLockRequest;
import com.misfit.ble.shine.request.SerialNumberGetLockRequest;
import com.misfit.ble.shine.request.SetActivityTaggingStateRequest;
import com.misfit.ble.shine.request.SetClockStateRequest;
import com.misfit.ble.shine.request.SetTripleTapEnableRequest;

public class FirmwareCompatibility {
	
	public static boolean isSupportedRequest(String firmwareVersion, String modelNumber,  Request request) {
		if (TextUtils.isEmpty(firmwareVersion) 
				|| TextUtils.isEmpty(modelNumber) 
				|| request == null) {
			return false;
		}
		
		String minFirmwareVersion = null;
		
		if (request instanceof GetBatteryRequest) {
			minFirmwareVersion = "0.0.50r";
		} else if (request instanceof GetClockStateRequest) {
			minFirmwareVersion = "0.0.28x.almost_press2.2";
		} else if (request instanceof GetTripleTapEnableRequest) {
			minFirmwareVersion = "0.0.28x.almost_press2.2";
		} else if (request instanceof SetClockStateRequest) {
			if (ShineConfiguration.CLOCK_STATE_SHOW_CLOCK_FIRST == ((SetClockStateRequest)request).mClockState) {
				minFirmwareVersion = "0.0.43r";
			} else {
				minFirmwareVersion = "0.0.28x.almost_press2.2";
			}	
		} else if (request instanceof SetTripleTapEnableRequest) {
			minFirmwareVersion = "0.0.28x.almost_press2.2";
		} else if (request instanceof GetActivityTaggingStateRequest
				|| request instanceof SetActivityTaggingStateRequest) {
	        if (firmwareVersion.startsWith("FL")) {
	        	minFirmwareVersion = "FL2.1.4r";
	        } else if (firmwareVersion.startsWith("SV")) {
				minFirmwareVersion = "SV0.1.11r";
			} else if (firmwareVersion.startsWith("S2")) {
				minFirmwareVersion = "S21.1.14r";
			} else if (firmwareVersion.startsWith("B0")) {
				minFirmwareVersion = "B00.0.41r";
			} else {
	        	return false;
	        }
		} else if (request instanceof SerialNumberGetLockRequest
				|| request instanceof SerialNumberChangeAndLockRequest) {
			return true;
		}
		
		if (minFirmwareVersion == null)
			return true;
		
		return compareFirmwareVersion(firmwareVersion, minFirmwareVersion) >= 0;
	}
	
	public static int compareFirmwareVersion(String lhs, String rhs) {
		if ((lhs.equalsIgnoreCase("0.0.28x.boot2_prod") || lhs.equalsIgnoreCase("0.0.28x.boot2_prod_ota"))  
				&& rhs.equalsIgnoreCase("0.0.28x.almost_press2.2")) {
			return -1;
		} else if (lhs.equalsIgnoreCase("0.0.28x.almost_press2.2") 
				&& (rhs.equalsIgnoreCase("0.0.28x.boot2_prod") || rhs.equalsIgnoreCase("0.0.28x.boot2_prod_ota"))) {
			return 1;
		} else {
			return lhs.compareToIgnoreCase(rhs);
		}
	}

	/**
	 * <b>Problem:</b> Flash with firmware version >= 2.2.8 uses StreamingEventWithAppID file handle
	 * <br>Flash with firmware version < 2.2.8 uses StreamingEvent file handle</br>
	 * <br>Other devices use StreamingEventLegacy file handle</br>
	 *
	 * @param deviceFamily:    device family
	 * @param firmwareVersion: firmware version
	 * @return file handle
	 */
	public static short streamingFileHandle(int deviceFamily, String firmwareVersion) {
		short fileHandle = Constants.FILE_HANDLE_STREAMING_EVENTS_LEGACY;

		if (ShineProfile.DEVICE_FAMILY_BUTTON == deviceFamily) {
			if(FirmwareCompatibility.compareFirmwareVersion(firmwareVersion, "FL2.2.8r") >= 0) {
				fileHandle = Constants.FILE_HANDLE_STREAMING_EVENTS_WITH_APP_ID;
			} else {
				fileHandle = Constants.FILE_HANDLE_STREAMING_EVENTS;
			}
		}
		return fileHandle;
	}

	/**
	 * <b>Problem:</b> the Pluto firmware only works when releaseEnable = true in custom mode
	 * <br><b>Solution:</b> checking if deviceFamily = Pluto, force releaseEnable = true; </br>
	 *
	 * @param releaseEnable    old release enable
	 * @param deviceFamily:    device family
	 * @param firmwareVersion: firmware version
	 * @return new releaseEnable
	 */
	public static boolean customModeReleaseEnable(boolean releaseEnable, int deviceFamily, String firmwareVersion) {
		boolean newReleaseEnable = releaseEnable;

		if (ShineProfile.DEVICE_FAMILY_PLUTO == deviceFamily) {
			newReleaseEnable = true;
		}

		return newReleaseEnable;
	}
}
