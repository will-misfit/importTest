package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 9/15/15.
 */
public class UnmapAllEventsRequest extends Request {
	@Override
	public String getRequestName() {
		return "unmapAllEvents";
	}

	@Override
	public int getTimeOut() {
		return 0;
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}

	public void buildRequest() {
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_EVENT_MAPPING_CONFIGURATION;
		byte actionId = Constants.EVENT_MAPPING_UNMAP_ALL;

		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, actionId);

		mRequestData = byteBuffer.array();
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
