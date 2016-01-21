package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 9/15/15.
 */
public class SetPassCodeRequest extends Request {

	private byte[] mPassCode;

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
		return Constants.MP_DC_ID_BTN_SETTINGS;
	}

	public byte getSettingId() {
		return Constants.BTN_BOLT_CONTROL;
	}

	public byte getCommandId() {
		return Constants.BOLT_PASSCODE;
	}

	public void buildRequest(byte[] passCode) {
		mPassCode = passCode;

		byte operation = getOperation();
		byte parameterId = getParameterId();
		byte settingsId = getSettingId();
		byte commandId = getCommandId();

		ByteBuffer byteBuffer = ByteBuffer.allocate(20);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingsId);
		byteBuffer.put(3, commandId);

		for(int i = 0; i < 16; i++) {
			byteBuffer.put(i + 4, passCode[i]);
		}

		mRequestData = byteBuffer.array();
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 1)
			return;

		byte[] passCode = paramsString.getBytes();
		buildRequest(passCode);
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("passCode", mPassCode);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "passCode";
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
