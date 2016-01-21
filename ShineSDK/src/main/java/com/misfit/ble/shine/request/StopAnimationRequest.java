package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StopAnimationRequest extends Request {
	
	@Override
	public String getRequestName() {
		return "stopAnimation";
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	public byte getParameterId() {
		return Constants.DEVICE_CONFIG_PARAMETER_ID_DIAGNOSTIC_FUNCTIONS;
	}
	
	public byte getFunctionId() {
		return Constants.DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_STOP_ANIMATION;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest() {
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = getParameterId();
		byte functionId = getFunctionId();
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, functionId);
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		buildRequest();
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
