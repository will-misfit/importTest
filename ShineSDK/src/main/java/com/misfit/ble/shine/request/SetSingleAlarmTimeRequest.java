package com.misfit.ble.shine.request;

import android.util.Log;

import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 8/18/15.
 */
public class SetSingleAlarmTimeRequest extends Request {

	private static final String TAG = "SetSingleAlarmTimeReq";

	private AlarmSettings mAlarmSettings;

	@Override
	public int getTimeOut() {
		return 0;
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}

	private byte getOperation() {
		return Constants.DEVICE_CONFIG_OPERATION_SET;
	}

	public byte getParameterId() {
		return Constants.DEVICE_CONFIG_PARAMETER_ID_DEVICE_SETTINGS_IN_OUT;
	}

	public byte getSettingId() {
		return Constants.DEVICE_SETTING_ID_ALARMS;
	}

	public byte getCommandId() {
		return Constants.COMMAND_ID_SETUP_SINGLE_ALARM_TIME;
	}

	public void buildRequest(AlarmSettings alarmSettings) {
		Log.e(TAG, "buildRequest");

		mAlarmSettings = alarmSettings;

		ByteBuffer byteBuffer = ByteBuffer.allocate(9);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperation());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, getCommandId());

		byteBuffer.put(4, mAlarmSettings.getAlarmDay());
		byteBuffer.put(5, mAlarmSettings.getAlarmOperation());
		byteBuffer.put(6, mAlarmSettings.getAlarmType());
		byteBuffer.put(7, Convertor.unsignedByteFromShort(mAlarmSettings.getAlarmHour()));
		byteBuffer.put(8, Convertor.unsignedByteFromShort(mAlarmSettings.getAlarmMinute()));

		mRequestData = byteBuffer.array();
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("alarmDay", mAlarmSettings.getAlarmDay());
			json.put("alarmOperation", mAlarmSettings.getAlarmOperation());
			json.put("alarmType", mAlarmSettings.getAlarmType());
			json.put("alarmHour", mAlarmSettings.getAlarmHour());
			json.put("alarmMinute", mAlarmSettings.getAlarmMinute());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "alarmDay, alarmOperation, alarmType, alarmHour, alarmMinute";
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
