package com.misfit.ble.shine.request;

import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 8/18/15.
 */
public class SetGoalHitNotificationRequest extends Request {

	private GoalHitNotificationSettings mGoalHitNotificationSettings;

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
		return Constants.ALGORITHM_SETTING_ID_GOAL_HIT_NOTIFICATION;
	}

	public byte getCommandId() {
		return Constants.COMMAND_ID_GOAL_HIT_NOTIFICATION_SETUP;
	}

	public void buildRequest(GoalHitNotificationSettings goalHitNotificationSettings) {
		mGoalHitNotificationSettings = goalHitNotificationSettings;

		ByteBuffer byteBuffer = ByteBuffer.allocate(12);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperation());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, getCommandId());

		byteBuffer.put(4, Convertor.byteFromBoolean(mGoalHitNotificationSettings.getEnabled()));
		byteBuffer.put(5, Convertor.unsignedByteFromShort(mGoalHitNotificationSettings.getLEDSequence().getValue()));
		byteBuffer.put(6, Convertor.unsignedByteFromShort(mGoalHitNotificationSettings.getVibeSequence().getValue()));
		byteBuffer.put(7, Convertor.unsignedByteFromShort(mGoalHitNotificationSettings.getSoundSequence().getValue()));
		byteBuffer.put(8, Convertor.unsignedByteFromShort(mGoalHitNotificationSettings.getStartHour()));
		byteBuffer.put(9, Convertor.unsignedByteFromShort(mGoalHitNotificationSettings.getStartMinute()));
		byteBuffer.put(10, Convertor.unsignedByteFromShort(mGoalHitNotificationSettings.getEndHour()));
		byteBuffer.put(11, Convertor.unsignedByteFromShort(mGoalHitNotificationSettings.getEndMinute()));

		mRequestData = byteBuffer.array();
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("enabled", mGoalHitNotificationSettings.getEnabled());
			json.put("ledSequenceID", mGoalHitNotificationSettings.getLEDSequence().getValue());
			json.put("vibeSequenceID", mGoalHitNotificationSettings.getVibeSequence().getValue());
			json.put("soundSequenceID", mGoalHitNotificationSettings.getSoundSequence().getValue());
			json.put("startHour", mGoalHitNotificationSettings.getStartHour());
			json.put("startMinute", mGoalHitNotificationSettings.getStartMinute());
			json.put("endHour", mGoalHitNotificationSettings.getEndHour());
			json.put("endMinute", mGoalHitNotificationSettings.getEndMinute());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "enabled?, LED?, Vibe?, Sound?, startHour, startMinute, endHour, endMinute";
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
