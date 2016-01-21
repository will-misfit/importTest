package com.misfit.ble.shine.request;

import com.misfit.ble.shine.ShineEventAnimationMapping;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class EventAnimationMappingRequest extends Request {

	private static final int MAX_LENGTH = 20;
	private static final int BYTES_HEADER = 2;
	private static final int BYTES_PER_MAPPING = 8;
	public static final int MAX_EVENTS = (MAX_LENGTH - BYTES_HEADER) / BYTES_PER_MAPPING;
	
	private ShineEventAnimationMapping[] mMappings;
	
	@Override
	public String getRequestName() {
		return "eventAnimationMapping";
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest(ShineEventAnimationMapping[] mappings) {
		mMappings = Arrays.copyOf(mappings, mappings.length);
		
	    /*!
	     *  Structure:
	     *   - [2 bytes]: event mapping header.
	     *   - [1 bytes]: control bits - unmap all events.
	     *   - [8 bytes each]: event mapping command, which consist of:
	     *      + [1 byte]: 0x28, command code.
	     *      + [1 byte]: event code.
	     *      + [2 bytes]: active and connected default actions animation A1 + number of repeats N1.
	     *      + [2 bytes]: unconnected default action animation A2 + number of repeats N2.
	     *      + [2 bytes]: timeout animation A3 + number of repeats N3.
	     *   - [1 bytes]: control bits - unpause and reset event sequence number.
	     */
	    int length = BYTES_HEADER + mMappings.length * BYTES_PER_MAPPING;
	    
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_EVENT_MAPPING_CONFIGURATION;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(length);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		// Header
		byteBuffer.put(operation);
		byteBuffer.put(parameterId);
		
	    // Event mapping command
		for (ShineEventAnimationMapping mapping : mappings) {
			byteBuffer.put(buildButtonAnimationCommand(mapping));
		}
		
		mRequestData = byteBuffer.array();
	}
	
	private byte[] buildButtonAnimationCommand(ShineEventAnimationMapping mapping) {
		byte[] bytes = new byte[BYTES_PER_MAPPING];
		bytes[0] = Constants.EVENT_MAPPING_MAP_POSITIVE_AND_NEGATIVE_AND_TIMEOUT_ANIMATION_WITH_REPEAT;
		bytes[1] = Convertor.unsignedByteFromShort(mapping.getButtonEventType());
		bytes[2] = Convertor.unsignedByteFromShort(mapping.getActiveAndConnectedDefaultAnimationCode());
		bytes[3] = Convertor.unsignedByteFromShort(mapping.getActiveAndConnectedDefaultAnimationRepeat());
		bytes[4] = Convertor.unsignedByteFromShort(mapping.getUnconnectedDefaultAnimationCode());
		bytes[5] = Convertor.unsignedByteFromShort(mapping.getUnconnectedDefaultAnimationRepeat());
		bytes[6] = Convertor.unsignedByteFromShort(mapping.getTimeoutAnimationCode());
		bytes[7] = Convertor.unsignedByteFromShort(mapping.getTimeoutAnimationRepeat());
		return bytes;
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			JSONArray jsonArray = new JSONArray();
			for (ShineEventAnimationMapping mapping : mMappings) {
				jsonArray.put(mapping.toJSON());
			}
			json.put("mappings", jsonArray);
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
