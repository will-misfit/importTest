package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetNumberOfMappedEventPacketsRequest extends Request {

	private int mNumberOfMappedEventPackets;
	
	@Override
	public String getRequestName() {
		return "setNumberOfMappedEventPackets";
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest(int numberOfMappedEventPackets) {
		mNumberOfMappedEventPackets = numberOfMappedEventPackets;
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_STREAMING_CONFIGURATION;
		byte settingId = Constants.STREAMING_SETTING_ID_NUMBER_OF_MAPPED_EVENT_PACKETS;
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingId);
		byteBuffer.putShort(3, Convertor.unsignedShortFromInteger(mNumberOfMappedEventPackets));
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("numberOfMappedEventPackets", mNumberOfMappedEventPackets);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	public String getParamsHint() {
		return "numberOfMappedEventPackets";
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}

}
