package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 9/15/15.
 */
public class AddGroupIdRequest extends Request {
	private static final String TAG = "AddGroupIdRequest";

	private short mGroupIdValue;

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

	public byte getGroupId() {
		return Constants.BOLT_GROUP_ID;
	}

	public void buildRequest(short groupIdValue) {
		mGroupIdValue = groupIdValue;

		byte operation = getOperation();
		byte parameterId = getParameterId();
		byte settingsId = getSettingId();
		byte groupId = getGroupId();

		ByteBuffer byteBuffer = ByteBuffer.allocate(5);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingsId);
		byteBuffer.put(3, groupId);
		byteBuffer.put(4, Convertor.unsignedByteFromShort(groupIdValue));

		mRequestData = byteBuffer.array();
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 1)
			return;

		short groupId = Short.parseShort(params[0]);
		buildRequest(groupId);
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("groupId", mGroupIdValue);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "groupId";
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
