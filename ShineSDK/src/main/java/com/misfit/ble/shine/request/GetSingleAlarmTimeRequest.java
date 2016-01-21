package com.misfit.ble.shine.request;

import android.util.Log;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 8/19/15.
 */
public class GetSingleAlarmTimeRequest extends Request {

	private static final String TAG = "GetSingleAlarmTimeReq";

	public static class Response extends BaseResponse {
		public byte mAlarmDay;
		public byte mAlarmType;
		public short mAlarmHour;
		public short mAlarmMinute;
	}

	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}

	@Override
	public String getRequestName() {
		return "GetSingleAlarmTime";
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}

	private byte getOperationId() {
		return Constants.DEVICE_CONFIG_OPERATION_GET;
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

	public void buildRequest(byte alarmDay) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(5);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperationId());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, getCommandId());

		byteBuffer.put(4, alarmDay);

		mRequestData = byteBuffer.array();
	}

	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, getParameterId());

		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 8) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				response.mAlarmDay = byteBuffer.get(4);
				response.mAlarmType = byteBuffer.get(5);
				response.mAlarmHour = Convertor.unsignedByteToShort(byteBuffer.get(6));
				response.mAlarmMinute = Convertor.unsignedByteToShort(byteBuffer.get(7));
			}
		}
		mResponse = response;
		mIsCompleted = true;
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		Log.d(TAG, "buildRequestWithParams");

		String[] params = paramsString.split(",");
		if (params.length != 1)
			return;

		byte alarmDay = Byte.parseByte(params[0]);
		buildRequest(alarmDay);
	}

	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			if (mResponse != null) {
				json.put("result", mResponse.result);
				json.put("alarmDay", mResponse.mAlarmDay);
				json.put("alarmType", mResponse.mAlarmType);
				json.put("alarmHour", mResponse.mAlarmHour);
				json.put("alarmMinute", mResponse.mAlarmMinute);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
