package com.misfit.ble.shine.request;

import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 8/17/15.
 */
public class SetInactivityNudgeRequest extends Request {

	private InactivityNudgeSettings mInactivityNudgeSettings;

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
		return Constants.DEVICE_CONFIG_PARAMETER_ID_ALGORITHM;
	}

	public byte getSettingId() {
		return Constants.ALGORITHM_SETTING_ID_INACTIVITY_NUDGE;
	}

	public byte getCommandId() {
		return Constants.COMMAND_ID_INACTIVITY_NUDGE_SETUP;
	}

	public void buildRequest(InactivityNudgeSettings inactivityNudgeSettings) {
		mInactivityNudgeSettings = inactivityNudgeSettings;

		ByteBuffer byteBuffer = ByteBuffer.allocate(13);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperation());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, getCommandId());

		byteBuffer.put(4, Convertor.byteFromBoolean(mInactivityNudgeSettings.getEnabled()));
		byteBuffer.put(5, Convertor.unsignedByteFromShort(mInactivityNudgeSettings.getLEDSequence().getValue()));
		byteBuffer.put(6, Convertor.unsignedByteFromShort(mInactivityNudgeSettings.getVibeSequence().getValue()));
		byteBuffer.put(7, Convertor.unsignedByteFromShort(mInactivityNudgeSettings.getSoundSequence().getValue()));
		byteBuffer.put(8, Convertor.unsignedByteFromShort(mInactivityNudgeSettings.getStartHour()));
		byteBuffer.put(9, Convertor.unsignedByteFromShort(mInactivityNudgeSettings.getStartMinute()));
		byteBuffer.put(10, Convertor.unsignedByteFromShort(mInactivityNudgeSettings.getEndHour()));
		byteBuffer.put(11, Convertor.unsignedByteFromShort(mInactivityNudgeSettings.getEndMinute()));
		byteBuffer.put(12, Convertor.unsignedByteFromShort(mInactivityNudgeSettings.getRepeatIntervalInMinute()));

		mRequestData = byteBuffer.array();
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("enabled", mInactivityNudgeSettings.getEnabled());
			json.put("ledSequenceID", mInactivityNudgeSettings.getLEDSequence());
			json.put("vibeSequenceID", mInactivityNudgeSettings.getVibeSequence());
			json.put("soundSequenceID", mInactivityNudgeSettings.getSoundSequence());
			json.put("startHour", mInactivityNudgeSettings.getStartHour());
			json.put("startMinute", mInactivityNudgeSettings.getStartMinute());
			json.put("endHour", mInactivityNudgeSettings.getEndHour());
			json.put("endMinute", mInactivityNudgeSettings.getEndMinute());
			json.put("repeatIntervalMinutes", mInactivityNudgeSettings.getRepeatIntervalInMinute());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "enabled?, LED?, Vibe?, Sound?, startHour, startMinute, endHour, endMinute, repeatIntervalMin";
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
