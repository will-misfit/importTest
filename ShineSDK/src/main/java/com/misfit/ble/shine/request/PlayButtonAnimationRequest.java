package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PlayButtonAnimationRequest extends Request {
	
	private short mAnimationCode;
	private short mNumOfRepeats;
	
	@Override
	public String getRequestName() {
		return "playButtonAnimation";
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest(short animationCode, short numOfRepeats) {
		mAnimationCode = animationCode;
		mNumOfRepeats = numOfRepeats;
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_EVENT_MAPPING_CONFIGURATION;
		
		ByteBuffer byteBuffer = null;
		if (mNumOfRepeats == 1) {
			byte actionId = Constants.EVENT_MAPPING_START_ANIMATION;
			
			byteBuffer = ByteBuffer.allocate(4);
	        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	        
			byteBuffer.put(0, operation);
			byteBuffer.put(1, parameterId);
			byteBuffer.put(2, actionId);
			byteBuffer.put(3, Convertor.unsignedByteFromShort(mAnimationCode));
		} else {
			byte actionId = Constants.EVENT_MAPPING_START_AND_REPEAT_ANIMATION;
			
			byteBuffer = ByteBuffer.allocate(5);
	        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	        
			byteBuffer.put(0, operation);
			byteBuffer.put(1, parameterId);
			byteBuffer.put(2, actionId);
			byteBuffer.put(3, Convertor.unsignedByteFromShort(mAnimationCode));
			byteBuffer.put(4, Convertor.unsignedByteFromShort(mNumOfRepeats));
		}
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("animation", mAnimationCode);
			json.put("numOfRepeats", mNumOfRepeats);
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
