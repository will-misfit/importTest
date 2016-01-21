package com.misfit.ble.shine.request;

import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 8/18/15.
 */
public class SetCallTextNotificationsRequest extends Request {

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
		return Constants.COMMAND_ID_SETUP_TEXT_CALL_NOTIFICATIONS;
	}

	public void buildRequest(NotificationsSettings setCallTextNotificationsRequest) {
		mNotificationsSettings = setCallTextNotificationsRequest;

		ByteBuffer byteBuffer = ByteBuffer.allocate(10);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperation());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, getCommandId());

		byteBuffer.put(4, Convertor.unsignedByteFromShort(mNotificationsSettings.getCallLEDSequence().getValue()));
		byteBuffer.put(5, Convertor.unsignedByteFromShort(mNotificationsSettings.getCallVibeSequence().getValue()));
		byteBuffer.put(6, Convertor.unsignedByteFromShort(mNotificationsSettings.getCallSoundSequence().getValue()));
		byteBuffer.put(7, Convertor.unsignedByteFromShort(mNotificationsSettings.getTextLEDSequence().getValue()));
		byteBuffer.put(8, Convertor.unsignedByteFromShort(mNotificationsSettings.getTextVibeSequence().getValue()));
		byteBuffer.put(9, Convertor.unsignedByteFromShort(mNotificationsSettings.getTextSoundSequence().getValue()));

		mRequestData = byteBuffer.array();
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("callLEDSequence", mNotificationsSettings.getCallLEDSequence().getValue());
			json.put("callVibeSequence", mNotificationsSettings.getCallVibeSequence().getValue());
			json.put("callSoundSequence", mNotificationsSettings.getCallSoundSequence().getValue());
			json.put("textLEDSequence", mNotificationsSettings.getTextLEDSequence().getValue());
			json.put("textVibeSequence", mNotificationsSettings.getTextVibeSequence().getValue());
			json.put("textSoundSequence", mNotificationsSettings.getTextSoundSequence().getValue());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "callLEDSequence, callVibeSequence, callSoundSequence, textLEDSequence, textVibeSequence, textSoundSequence";
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
