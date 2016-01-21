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
public class GetCallTextNotificationRequest extends Request {

	private static final String TAG = "GetCallTextNotiRequest";

	public static class Response extends BaseResponse {
		public PlutoSequence.LED callLedSequence;
		public PlutoSequence.Vibe callVibeSequence;
		public PlutoSequence.Sound callSoundSequence;
		public PlutoSequence.LED textLedSequence;
		public PlutoSequence.Vibe textVibeSequence;
		public PlutoSequence.Sound textSoundSequence;
	}

	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}

	@Override
	public String getRequestName() {
		return "GetCallTextNotification";
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
		return Constants.COMMAND_ID_SETUP_TEXT_CALL_NOTIFICATIONS;
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

				response.callLedSequence = new PlutoSequence.LED(Convertor.unsignedByteToShort(byteBuffer.get(4)));
				response.callVibeSequence = new PlutoSequence.Vibe(Convertor.unsignedByteToShort(byteBuffer.get(5)));
				response.callSoundSequence = new PlutoSequence.Sound(Convertor.unsignedByteToShort(byteBuffer.get(6)));
				response.textLedSequence = new PlutoSequence.LED(Convertor.unsignedByteToShort(byteBuffer.get(7)));
				response.textVibeSequence = new PlutoSequence.Vibe(Convertor.unsignedByteToShort(byteBuffer.get(8)));
				response.textSoundSequence = new PlutoSequence.Sound(Convertor.unsignedByteToShort(byteBuffer.get(9)));
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
				json.put("callLedSequence", mResponse.callLedSequence.getValue());
				json.put("callVibeSequence", mResponse.callVibeSequence.getValue());
				json.put("callSoundSequence", mResponse.callSoundSequence.getValue());
				json.put("textLedSequence", mResponse.textLedSequence.getValue());
				json.put("textVibeSequence", mResponse.textVibeSequence.getValue());
				json.put("textSoundSequence", mResponse.textSoundSequence.getValue());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
