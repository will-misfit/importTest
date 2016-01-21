package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FileAbortRequest extends Request {
	
	private short mFileHandle;
	
	@Override
	public String getRequestName() {
		return "fileAbort";
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest(short fileHandle) {
		mFileHandle = fileHandle;
		
		byte operation = Constants.FILE_CONTROL_OPERATION_ABORT_REQUEST_RESPONSE;
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		byteBuffer.put(0, operation);
		byteBuffer.putShort(1, fileHandle);
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("fileHandle", Convertor.unsignedShortToInteger(mFileHandle));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
