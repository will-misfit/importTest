package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 8/18/15.
 */
public class DisableAllCallTextNotificationsRequest extends Request {

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
		return Constants.DEVICE_CONFIG_PARAMETER_ID_DEVICE_SETTINGS_IN_OUT;
	}

	public byte getSettingId() {
		return Constants.DEVICE_SETTING_ID_BLE_NOTIFICATIONS;
	}

	public byte getCommandId() {
		return Constants.COMMAND_ID_DISABLE_ALL_NOTIFICATIONS;
	}

	public void buildRequest() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperation());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, getCommandId());

		mRequestData = byteBuffer.array();
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
