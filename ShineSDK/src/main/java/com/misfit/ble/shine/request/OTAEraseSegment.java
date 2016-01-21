package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OTAEraseSegment extends Request {
	
	public static class Response extends BaseResponse {
		public byte status;
		public int fileHandle;
		public long newSizeWritten;
	}
	private Response mResponse;
	private long mPageOffset;

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getRequestName() {
		return "otaErase";
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest(long pageOffset) {
		mPageOffset = pageOffset;
		
		byte operation = Constants.FILE_CONTROL_OPERATION_OTA_ERASE;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(7);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.putShort(1, Constants.FILE_HANDLE_OTA);
		byteBuffer.putInt(3, Convertor.unsignedIntFromLong(pageOffset));
		
		mRequestData = byteBuffer.array();
	}
	
	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		mResponse = parseResponse(bytes);
		mIsCompleted = true;
	}
	
	protected Response parseResponse(byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.FILE_CONTROL_OPERATION_OTA_ERASE_RESPONSE);
		response.status = 0;
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 8) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				response.fileHandle = byteBuffer.getShort(1);
				response.status = byteBuffer.get(3);
				response.newSizeWritten = byteBuffer.getInt(4);
				if (Constants.FILE_HANDLE_OTA != response.fileHandle
						|| Constants.FILE_CONTROL_RESPONSE_SUCCESS != response.status) {
					response.result = Constants.RESPONSE_ERROR;
				}
			}
		}
		return response;
	}
	
	public void buildRequest() {
		long pageOffset = 0;
		buildRequest(pageOffset);
	}
	
	@Override
	public void buildRequestWithParams(String paramsString) {
		if (paramsString == null || paramsString.length() <= 0) {
			buildRequest();
			return;
		}
		
		String[] params = paramsString.split(",");
		if (params.length != 1)
			return;
		
		long pageOffset = Long.parseLong(params[0], 16);
		buildRequest(pageOffset);
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("pageOffset", mPageOffset);
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
				json.put("fileHandle", mResponse.fileHandle);
				json.put("newSizeWritten", mResponse.newSizeWritten);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

}
