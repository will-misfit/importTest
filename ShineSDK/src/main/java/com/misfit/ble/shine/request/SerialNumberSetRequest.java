package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class SerialNumberSetRequest extends Request {

	private String mSerialNumber;
	
	@Override
	public String getRequestName() {
		return "serialNumberSet";
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}
	
	public boolean buildRequest(String serialNumber) {
		mSerialNumber = serialNumber;
		
		if (serialNumber == null || serialNumber.length() != 10)
			return false;
		
		byte[] bytes = serialNumber.getBytes(Charset.forName("UTF-8"));
		if (bytes == null || bytes.length != 10)
			return false;
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_DEVICE_SETTINGS_IN_OUT;
		byte settingsId = Constants.DEVICE_SETTING_ID_SERIAL_NUMBER;
		
		byte[] serialNumberBytes = new byte[11];
		System.arraycopy(bytes, 0, serialNumberBytes, 0, bytes.length);
		serialNumberBytes[10] = 0;
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(14);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.put(2, settingsId);
		byteBuffer.position(3);
		byteBuffer.put(serialNumberBytes, 0, serialNumberBytes.length);
		
		mRequestData = byteBuffer.array();
		
		return true;
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("serialNumber", mSerialNumber);
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
