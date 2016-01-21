package com.misfit.ble.shine.request;

import com.misfit.ble.setting.flashlink.CustomModeEnum;
import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 9/17/15.
 */
public class UnmapEventRequest extends Request {

	private CustomModeEnum.MemEventNumber mEventNumber;

	@Override
	public String getRequestName() {
		return "unmapEvent";
	}

	@Override
	public int getTimeOut() {
		return 0;
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}

	public void buildRequest(CustomModeEnum.MemEventNumber eventNumber) {
		mEventNumber = eventNumber;

		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_EVENT_MAPPING_CONFIGURATION;
		byte actionId = Constants.EVENT_MAPPING_UNMAP_EVENT;

		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, actionId);
		byteBuffer.put(3, eventNumber.getId());

		mRequestData = byteBuffer.array();
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 1)
			return;

		CustomModeEnum.MemEventNumber eventNumber = CustomModeEnum.MemEventNumber.valueOf(paramsString);
		buildRequest(eventNumber);
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("eventMember", mEventNumber);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
