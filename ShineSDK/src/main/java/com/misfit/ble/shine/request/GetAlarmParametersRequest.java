package com.misfit.ble.shine.request;

import android.util.Log;

import com.misfit.ble.setting.pluto.PlutoSequence;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 8/19/15.
 */
public class GetAlarmParametersRequest extends Request {

	private static final String TAG = "GetAlarmParametersReq";

	public static class Response extends BaseResponse{
		public short mWindowInMinute;
		public PlutoSequence.LED mLEDSequence;
		public PlutoSequence.Vibe mVibeSequence;
		public PlutoSequence.Sound mSoundSequence;
		public short mSnoozeTimeInMinute;
		public short mAlarmDuration;
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
		return Constants.COMMAND_ID_SETUP_ALARM_PARAMETERS;
	}

	public void buildRequest() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperationId());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, getCommandId());

		mRequestData = byteBuffer.array();
	}

	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, getParameterId());

		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 10) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				response.mWindowInMinute = Convertor.unsignedByteToShort(byteBuffer.get(4));
				response.mLEDSequence = new PlutoSequence.LED(Convertor.unsignedByteToShort(byteBuffer.get(5)));
				response.mVibeSequence = new PlutoSequence.Vibe(Convertor.unsignedByteToShort(byteBuffer.get(6)));
				response.mSoundSequence = new PlutoSequence.Sound(Convertor.unsignedByteToShort(byteBuffer.get(7)));
				response.mSnoozeTimeInMinute = Convertor.unsignedByteToShort(byteBuffer.get(8));
				response.mAlarmDuration = Convertor.unsignedByteToShort(byteBuffer.get(9));
			}
		}
		mResponse = response;
		mIsCompleted = true;
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		Log.d(TAG, "buildRequestWithParams");
		buildRequest();
	}

	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			if (mResponse != null) {
				json.put("result", mResponse.result);
				json.put("smartAlarm", mResponse.mWindowInMinute);
				json.put("ledSequence", mResponse.mLEDSequence.getValue());
				json.put("vibeSequence", mResponse.mVibeSequence.getValue());
				json.put("soundSequence", mResponse.mSoundSequence.getValue());
				json.put("minutePerSnooze", mResponse.mSnoozeTimeInMinute);
				json.put("alarmDuration", mResponse.mAlarmDuration);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
