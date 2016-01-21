package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 9/15/15.
 */
public class GetPassCodeRequest extends Request {

	private static final String TAG = LogUtils.makeTag(GetPassCodeRequest.class);

	public static class Response extends BaseResponse{
		public byte[] mPasscode = new byte[16];
	}

	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}

	@Override
	public String getRequestName() {
		return "getPasscode";
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}

	private byte getOperation() {
		return Constants.DEVICE_CONFIG_OPERATION_GET;
	}

	public byte getParameterId() {
		return Constants.MP_DC_ID_BTN_SETTINGS;
	}

	public byte getSettingId() {
		return Constants.BTN_BOLT_CONTROL;
	}

	public byte getGroupId() {
		return Constants.BOLT_PASSCODE;
	}

	public void buildRequest() {
		byte operation = getOperation();
		byte parameterId = getParameterId();
		byte settingsId = getSettingId();
		byte groupId = getGroupId();

		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingsId);
		byteBuffer.put(3, groupId);

		mRequestData = byteBuffer.array();
	}

	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, getParameterId());

		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 19) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				for(int i = 0; i < 16; i++) {
					response.mPasscode[i] = byteBuffer.get(i + 3);
				}
			}
		}
		mResponse = response;
		mIsCompleted = true;
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		buildRequest();
	}

	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			if (mResponse != null) {
				json.put("result", mResponse.result);
				json.put("passCode", mResponse.mPasscode);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
