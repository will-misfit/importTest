package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ActivateRequest extends Request {
	
	@Override
	public String getRequestName() {
		return "activate";
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public byte getParameterId() {
		return Constants.DEVICE_CONFIG_PARAMETER_ID_DIAGNOSTIC_FUNCTIONS;
	}
	
	public void buildRequest() {
		byte operation   = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = getParameterId();
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(0, operation);
        byteBuffer.put(1, parameterId);
        byteBuffer.put(2, Constants.DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_ACTIVATE);
        
        mRequestData = byteBuffer.array();
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
