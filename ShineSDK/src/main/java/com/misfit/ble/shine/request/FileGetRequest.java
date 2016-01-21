package com.misfit.ble.shine.request;

import android.util.Log;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FileGetRequest extends Request {
	
	private static final String TAG = "GetFileRequest";

	public static class Response extends BaseResponse {
		public byte status;
		public short handle;
	}
	private Response mResponse;
	private short mHandle;
	private int mOffset;
	private int mLength;
	
	private ByteArrayBuffer mResponseRawData;
	private byte mSequenceNumber;
	
	private boolean mGotEOFOnFileTransferCharactersitic;
	private boolean mGotEOFOnFileControlCharactersitic;
	
	public FileGetRequest() {
		mResponseRawData = new ByteArrayBuffer(20);
		mSequenceNumber = -1;
		mGotEOFOnFileTransferCharactersitic = false;
		mGotEOFOnFileControlCharactersitic = false;
	}
	
	@Override
	public String getRequestName() {
		return "fileGet";
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID;
	}
	
	@Override
	public int getTimeOut() {
		return 10000;
	}

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	public void parseFile(byte[] bytes) {
		// Do nothing
	}
	
	public void buildRequest(short fileHandle, int offset, int length) {
		mHandle = fileHandle;
		mOffset = offset;
		mLength = length;
		
		byte operation = Constants.FILE_CONTROL_OPERATION_GET;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(11);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.putShort(1, fileHandle);
		byteBuffer.putInt(3, offset);
		byteBuffer.putInt(7, length);
        
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		if (paramsString == null || paramsString.length() <= 0)
			return;
		
		String[] params = paramsString.split(",");
		if (params.length != 3)
			return;
		
		if (params[0].startsWith("0x"))
			params[0] = params[0].substring(2);
		
		short handle = Short.parseShort(params[0], 16);
		int offset = Convertor.unsignedIntFromLong(Long.parseLong(params[1]));
		int length = Convertor.unsignedIntFromLong(Long.parseLong(params[2]));
		buildRequest(handle, offset, length);
	}
	
	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		if (mIsCompleted == true) {
			Log.w(TAG, "Skip response: " + bytes);
			return;
		}
		
		if (mResponse == null) {
			mResponse = parseResponse(bytes);
			if (mResponse.result != Constants.RESPONSE_SUCCESS) {
				mIsCompleted = true;
			}
		} else if (characteristicUUID.equals(Constants.MFSERVICE_FILE_TRANSFER_DATA_CHARACTERISTIC_UUID)) {
			byte sequenceNumber = (byte) (bytes[0] & Constants.FILE_TRANSFER_SEQUENCE_MASK);
			byte expectedSequenceNumber = (byte) ((mSequenceNumber + 1) & Constants.FILE_TRANSFER_SEQUENCE_MASK);
			if (sequenceNumber != expectedSequenceNumber) {
				Log.w(TAG, "WRONG sequence number: " + sequenceNumber + " - expected: " + expectedSequenceNumber);
				return;
			}
			mSequenceNumber = expectedSequenceNumber;
			mResponseRawData.append(bytes, 1, bytes.length - 1);
			
			boolean isEOF = (boolean) ((bytes[0] & Constants.FILE_TRANSFER_EOF_MASK) != 0);
			if (isEOF) {
				parseFile(mResponseRawData.toByteArray());
				
				mGotEOFOnFileTransferCharactersitic = true;
			}
		} else if (characteristicUUID.equals(Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID)) {
			if (bytes.length > 0 && bytes[0] == Constants.FILE_CONTROL_OPERATION_EOF_RESPONSE) {
				mGotEOFOnFileControlCharactersitic = true; 
			} else {
				mResponse.result = Constants.RESPONSE_ERROR;
				mIsCompleted = true;
			}
		}
		
		if (mResponse.result == Constants.RESPONSE_SUCCESS) {
			mIsCompleted = mGotEOFOnFileControlCharactersitic && mGotEOFOnFileTransferCharactersitic;
		}
	}
	
	protected Response parseResponse(byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.FILE_CONTROL_OPERATION_GET_RESPONSE);
		response.status = 0;
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 4) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				response.handle = byteBuffer.getShort(1);
				response.status = byteBuffer.get(3);
				if (response.handle != mHandle || response.status != Constants.FILE_CONTROL_RESPONSE_SUCCESS) {
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
			json.put("handle", Convertor.unsignedShortToInteger(mHandle));
			json.put("offset", mOffset);
			json.put("length", mLength);
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
				json.put("handle", mResponse.handle);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	@Override
	public String getParamsHint() {
		return "handle:0x100,\noffset:0,\nlength:65535";
	}
}
