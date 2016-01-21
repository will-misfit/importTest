package com.misfit.ble.shine.request;

import android.util.Log;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OTAPutRequest extends Request {
	
	private static final String TAG = "OTAPutRequest";
	
	public static class Response extends BaseResponse {
		public byte status;
		public long actualByteWritten;
	}
	private Response mResponse;
	
	public boolean mHasReceivedEOF;
	
	private long mOffset;
	private long mLength;
	private long mTotalLength;

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getRequestName() {
		return "otaPut";
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID;
	}
	
	@Override
	public int getTimeOut() {
		return 0;
	}
	
	@Override
	public boolean isWaitingForResponse() {
		return !(mIsCompleted && mHasReceivedEOF);
	}
	
	public long getDataOffset() {
		return mOffset;
	}
	
	public long getDataLength() {
		return mLength;
	}
	
	public void buildRequest(long offset, long length, long totalLength) {
		mOffset = offset;
		mLength = length;
		mTotalLength = totalLength;
		
		byte operation = Constants.FILE_CONTROL_OPERATION_OTA_PUT;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(15);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.putShort(1, Constants.FILE_HANDLE_OTA);
		byteBuffer.putInt(3, Convertor.unsignedIntFromLong(offset));
		byteBuffer.putInt(7, Convertor.unsignedIntFromLong(length));
		byteBuffer.putInt(11, Convertor.unsignedIntFromLong(totalLength));
        
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 3)
			return;
		
		int offset = Integer.parseInt(params[0]);
		int length = Integer.parseInt(params[1]);
		int totalLength = Integer.parseInt(params[2]);
		buildRequest(offset, length, totalLength);
	}
	
	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		if (mIsCompleted && mHasReceivedEOF) {
			Log.w(TAG, "Skip response: " + bytes);
			return;
		}
		
		if (mResponse == null) {
			mResponse = parseResponse(bytes);
		} else if (mResponse.result == Constants.RESPONSE_SUCCESS){
			mResponse = parseResponseEOF(bytes);
			mHasReceivedEOF = true;
		}
		mIsCompleted = true;
	}
	
	private Response parseResponse(byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.FILE_CONTROL_OPERATION_OTA_PUT_RESPONSE);
		response.status = 0;
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 4) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				short handle = byteBuffer.getShort(1);
				response.status = byteBuffer.get(3);
				if (handle != Constants.FILE_HANDLE_OTA || response.status != Constants.FILE_CONTROL_RESPONSE_SUCCESS) {
					response.result = Constants.RESPONSE_ERROR;
				}
			}
		}
		return response;
	}
	
	private Response parseResponseEOF(byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.FILE_CONTROL_OPERATION_OTA_PUT_EOF);
		response.status = 0;
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 9) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				short handle = byteBuffer.getShort(1);
				response.status = byteBuffer.get(3);
				response.actualByteWritten = byteBuffer.getInt(5);
				
				if (handle != Constants.FILE_HANDLE_OTA || response.status != Constants.FILE_CONTROL_RESPONSE_SUCCESS) {
					response.result = Constants.RESPONSE_ERROR;
				}
			}
		}
		return response;
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("offset", mOffset);
			json.put("length", mLength);
			json.put("totalLength", mTotalLength);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			if (mResponse != null) {
				json.put("result", mResponse.result);
				json.put("status", mResponse.status);
				json.put("actualByteWritten", mResponse.actualByteWritten);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "url:http...42r.bin";
	}
}
