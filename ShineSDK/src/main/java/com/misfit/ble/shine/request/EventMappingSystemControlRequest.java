package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EventMappingSystemControlRequest extends Request {

	private short mControlBits;
	
	@Override
	public String getRequestName() {
		return "eventMappingSystemControl";
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest(byte controlBits) {
		mControlBits = Convertor.unsignedByteToShort(controlBits);
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_EVENT_MAPPING_CONFIGURATION;
		byte actionId = controlBits;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, actionId);
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("controlBits", mControlBits);
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
