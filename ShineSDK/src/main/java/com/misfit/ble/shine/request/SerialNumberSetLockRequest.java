package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SerialNumberSetLockRequest extends Request {

	@Override
	public String getRequestName() {
		return "serialNumberSetLock";
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
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_DEVICE_SETTINGS_IN_OUT;
		byte settingsId = Constants.DEVICE_SETTING_ID_SERIAL_NUMBER_EXTENDED;
		byte subOperationId = Constants.SERIAL_NUMBER_EXTENDED_OPERATION_ID_LOCK;
		
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingsId);
		byteBuffer.put(3, subOperationId);
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}

}
