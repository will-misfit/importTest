package com.misfit.ble.shine.request;

import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 9/16/15.
 */
public class SetCallTextNotificationWindowsRequest extends Request {

	private NotificationsSettings mNotificationsSettings;

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
		return Constants.DEVICE_SETTING_ID_BLE_NOTIFICATIONS;
	}

	public byte getCommandId() {
		return Constants.COMMAND_ID_SETUP_CALL_TEXT_NOTIFICATIONS_TIME_WINDOW;
	}

	public void buildRequest(NotificationsSettings setCallTextNotificationsRequest) {
		mNotificationsSettings = setCallTextNotificationsRequest;

		ByteBuffer byteBuffer = ByteBuffer.allocate(8);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperation());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, getCommandId());

		byteBuffer.put(4, Convertor.unsignedByteFromShort(mNotificationsSettings.getStartHour()));
		byteBuffer.put(5, Convertor.unsignedByteFromShort(mNotificationsSettings.getStartMinute()));
		byteBuffer.put(6, Convertor.unsignedByteFromShort(mNotificationsSettings.getEndHour()));
		byteBuffer.put(7, Convertor.unsignedByteFromShort(mNotificationsSettings.getEndMinute()));

		mRequestData = byteBuffer.array();
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("startHour", mNotificationsSettings.getStartHour());
			json.put("startMinute", mNotificationsSettings.getStartMinute());
			json.put("endHour", mNotificationsSettings.getEndHour());
			json.put("endMinute", mNotificationsSettings.getEndMinute());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "startHour, startMinute, endHour, endMinute";
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
