package com.misfit.ble.shine.request;

import android.util.Log;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;
import com.misfit.ble.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 9/16/15.
 */
public class GetCallTextNotificationWindowsRequest extends Request {

	private static final String TAG = LogUtils.makeTag(GetCallTextNotificationWindowsRequest.class);

	public static class Response extends BaseResponse {
		public short mStartHour;
		public short mStartMinute;
		public short mEndHour;
		public short mEndMinute;
	}

	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}

	@Override
	public String getRequestName() {
		return "GetCallTextNotificationWindows";
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
		return Constants.DEVICE_SETTING_ID_BLE_NOTIFICATIONS;
	}

	public byte getCommandId() {
		return Constants.COMMAND_ID_SETUP_CALL_TEXT_NOTIFICATIONS_TIME_WINDOW;
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
			if (bytes.length < 8) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				response.mStartHour = Convertor.unsignedByteToShort(byteBuffer.get(4));
				response.mStartMinute = Convertor.unsignedByteToShort(byteBuffer.get(5));
				response.mEndHour = Convertor.unsignedByteToShort(byteBuffer.get(6));
				response.mEndMinute = Convertor.unsignedByteToShort(byteBuffer.get(7));
			}
		}
		mResponse = response;
		mIsCompleted = true;
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		Log.e(TAG, "buildRequestWithParams");
		buildRequest();
	}

	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			if (mResponse != null) {
				json.put("startHour", mResponse.mStartHour);
				json.put("startMinute", mResponse.mStartMinute);
				json.put("endHour", mResponse.mEndHour);
				json.put("endMinute", mResponse.mEndMinute);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
