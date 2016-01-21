package com.misfit.ble.shine.request;

import android.util.Log;

import com.misfit.ble.shine.core.Constants;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FileListRequest extends Request {
	
	private static final String TAG = "ListFileRequest";
			
	public static class Response extends BaseResponse{
		public byte status;
		public short numberOfFiles;		// unsigned byte
		public long totalFileSize;		// unsigned int
	}
	private Response mResponse;
	
	private ByteArrayBuffer mResponseRawData;
	private byte mSequenceNumber;
	
	private boolean mGotEOFOnFileTransferCharacteristic;
	private boolean mGotEOFOnFileControlCharacteristic;
	
	public FileListRequest() {
		mResponseRawData = new ByteArrayBuffer(20);
		mSequenceNumber = -1;
		mGotEOFOnFileTransferCharacteristic = false;
		mGotEOFOnFileControlCharacteristic = false;
	}
	
	@Override
	public String getRequestName() {
		return "fileList";
	}
	
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest() {
		byte operation = Constants.FILE_CONTROL_OPERATION_LIST;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(1);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		
		mRequestData = byteBuffer.array();
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
		} else if (characteristicUUID.equals(Constants.MFSERVICE_FILE_TRANSFER_DATA_CHARACTERISTIC_UUID)){
			byte sequenceNumber = (byte) (bytes[0] & Constants.FILE_TRANSFER_SEQUENCE_MASK);
			if (sequenceNumber != mSequenceNumber + 1) {
				Log.w(TAG, "WRONG sequence number: " + sequenceNumber + " - expected: " + (mSequenceNumber + 1));
				return;
			}
			mSequenceNumber += 1;
			mResponseRawData.append(bytes, 1, bytes.length - 1);
			
			boolean isEOF = (boolean) ((bytes[0] & Constants.FILE_TRANSFER_EOF_MASK) != 0);
			if (isEOF) {
				ByteBuffer resultByteBuffer = ByteBuffer.wrap(mResponseRawData.buffer());
				resultByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				mResponse.numberOfFiles = resultByteBuffer.get(0);
				mResponse.totalFileSize = resultByteBuffer.getInt(1);
				
				mGotEOFOnFileTransferCharacteristic = true;
			}
		} else if (characteristicUUID.equals(Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID)) {
			if (bytes[0] == Constants.FILE_CONTROL_OPERATION_EOF_RESPONSE) {
				mGotEOFOnFileControlCharacteristic = true;
			} else {
				mResponse.result = Constants.RESPONSE_ERROR;
				mIsCompleted = true;
			}
		}
		
		if (mResponse.result == Constants.RESPONSE_SUCCESS) {
			mIsCompleted = mGotEOFOnFileControlCharacteristic && mGotEOFOnFileTransferCharacteristic;
		}
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		buildRequest();
	}
	
	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			if (mResponse != null) {
				json.put("result", mResponse.result);
				json.put("status", mResponse.status);
				json.put("numberOfFiles", mResponse.numberOfFiles);
				json.put("totalFileSize", mResponse.totalFileSize);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	private Response parseResponse(byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.FILE_CONTROL_OPERATION_LIST_RESPONSE);
		response.status = 0;
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length <= 1) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				response.status = bytes[1];
				if (response.status != Constants.FILE_CONTROL_RESPONSE_SUCCESS) {
					response.result = Constants.RESPONSE_ERROR;
				}
			}
		}
		return response;
	}
}
