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
 * Created by Quoc-Hung Le on 8/18/15.
 */
public class GetGoalHitNotificationRequest extends Request{

	private static final String TAG = "GetGoalHitNotiRequest";

	public static class Response extends BaseResponse {
		public boolean mEnabled;
		public PlutoSequence.LED mLEDSequenceID;
		public PlutoSequence.Vibe mVibeSequenceID;
		public PlutoSequence.Sound mSoundSequenceID;
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
		return "getInactivityNudge";
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}

	public byte getOperationId() {
		return Constants.DEVICE_CONFIG_OPERATION_GET;
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
			if (bytes.length < 12) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				response.mEnabled = Convertor.byteToBoolean(byteBuffer.get(4));
				response.mLEDSequenceID = new PlutoSequence.LED(Convertor.unsignedByteToShort(byteBuffer.get(5)));
				response.mVibeSequenceID = new PlutoSequence.Vibe(Convertor.unsignedByteToShort(byteBuffer.get(6)));
				response.mSoundSequenceID = new PlutoSequence.Sound(Convertor.unsignedByteToShort(byteBuffer.get(7)));
				response.mStartHour = Convertor.unsignedByteToShort(byteBuffer.get(8));
				response.mStartMinute = Convertor.unsignedByteToShort(byteBuffer.get(9));
				response.mEndHour = Convertor.unsignedByteToShort(byteBuffer.get(10));
				response.mEndMinute = Convertor.unsignedByteToShort(byteBuffer.get(11));
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
				json.put("result", mResponse.result);
				json.put("enabled", mResponse.mEnabled);
				json.put("ledSequenceID", mResponse.mLEDSequenceID.getValue());
				json.put("vibeSequenceID", mResponse.mVibeSequenceID.getValue());
				json.put("soundSequenceID", mResponse.mSoundSequenceID.getValue());
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
