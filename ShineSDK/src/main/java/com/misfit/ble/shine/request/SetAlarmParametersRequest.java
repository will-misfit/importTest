package com.misfit.ble.shine.request;

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
public class SetAlarmParametersRequest extends Request {

	private static final String TAG = "SetAlarmParametersReq";
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
		return Constants.COMMAND_ID_SETUP_ALARM_PARAMETERS;
	}

	public void buildRequest(AlarmSettings alarmSettings) {
		mAlarmSettings = alarmSettings;

		ByteBuffer byteBuffer = ByteBuffer.allocate(10);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperation());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, getCommandId());

		byteBuffer.put(4, Convertor.unsignedByteFromShort(mAlarmSettings.getWindowInMinute()));
		byteBuffer.put(5, Convertor.unsignedByteFromShort(mAlarmSettings.getLEDSequence().getValue()));
		byteBuffer.put(6, Convertor.unsignedByteFromShort(mAlarmSettings.getVibeSequence().getValue()));
		byteBuffer.put(7, Convertor.unsignedByteFromShort(mAlarmSettings.getSoundSequence().getValue()));
		byteBuffer.put(8, Convertor.unsignedByteFromShort(mAlarmSettings.getSnoozeTimeInMinute()));
		byteBuffer.put(9, Convertor.unsignedByteFromShort(mAlarmSettings.getAlarmDuration()));

		mRequestData = byteBuffer.array();
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("windowInMinute", mAlarmSettings.getWindowInMinute());
			json.put("ledSequence", mAlarmSettings.getLEDSequence().getValue());
			json.put("vibeSequence", mAlarmSettings.getVibeSequence().getValue());
			json.put("soundSequence", mAlarmSettings.getSoundSequence().getValue());
			json.put("snoozeTimeInMinute", mAlarmSettings.getSnoozeTimeInMinute());
			json.put("alarmDurationMinutes", mAlarmSettings.getAlarmDuration());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "smartAlarmWindowMinutes, ledSequence, vibeSequence, soundSequence, minutesPerSnooze, alarmDurationMinutes";
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
