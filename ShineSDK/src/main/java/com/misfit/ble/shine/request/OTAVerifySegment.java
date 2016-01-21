package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.CRCCalculator;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class OTAVerifySegment extends Request {
	
	public static class Response extends BaseResponse {
		public byte status;
		public long segmentCRC;			// unsigned int
	}
	private Response mResponse;
	private long mSegmentOffset;
	private long mSegmentLength;
	private long mTotalFileLength;
	private long mExpectedCRC;

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getRequestName() {
		return "otaVerifySegment";
	}
	
	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID;
	}
	
	public void buildRequest(byte[] firmwareData, long segmentOffset, long segmentLength, long totalFileLength) {
		mSegmentOffset = segmentOffset;
		mSegmentLength = segmentLength;
		mTotalFileLength = totalFileLength;
		
		byte operation = Constants.FILE_CONTROL_OPERATION_OTA_VERIFY_SEGMENT;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(15);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		byteBuffer.put(0, operation);
		byteBuffer.putShort(1, Constants.FILE_HANDLE_OTA);
		byteBuffer.putInt(3, Convertor.unsignedIntFromLong(segmentOffset));
		byteBuffer.putInt(7, Convertor.unsignedIntFromLong(segmentLength));
		byteBuffer.putInt(11, Convertor.unsignedIntFromLong(totalFileLength));
	
		mRequestData = byteBuffer.array();
		
		byte[] verifyingData = Arrays.copyOfRange(firmwareData, Convertor.unsignedIntFromLong(segmentOffset), Convertor.unsignedIntFromLong(segmentLength));
		mExpectedCRC = CRCCalculator.calculateCRC(verifyingData.length, verifyingData);
	}
	
	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		mResponse = parseResponse(bytes);
		if (mResponse.result == Constants.RESPONSE_SUCCESS) {
			if (mResponse.segmentCRC != mExpectedCRC) {
				mResponse.result = Constants.RESPONSE_WRONG_CRC;
			}
		}
		mIsCompleted = true;
	}
	
	protected Response parseResponse(byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.FILE_CONTROL_OPERATION_OTA_VERIFY_SEGMENT_RESPONSE);
		response.status = 0;
		
		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 8) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				short fileHandle = byteBuffer.getShort(1);
				response.status = byteBuffer.get(3);
				response.segmentCRC = byteBuffer.getInt(4);
				if (Constants.FILE_HANDLE_OTA != fileHandle || Constants.FILE_CONTROL_RESPONSE_SUCCESS != response.status) {
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
			json.put("segmentOffset", mSegmentOffset);
			json.put("segmentLength", mSegmentLength);
			json.put("totalFileLength", mTotalFileLength);
			json.put("expectedCRC", mExpectedCRC);
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
				json.put("segmentCRC", mResponse.segmentCRC);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
