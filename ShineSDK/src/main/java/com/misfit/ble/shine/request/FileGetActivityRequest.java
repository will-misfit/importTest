package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.CRCCalculator;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FileGetActivityRequest extends FileGetRequest {
	
	public static class Response extends FileGetRequest.Response {
		public int fileHandle;					// unsigned short
		public int fileFormat;					// unsigned short
		public long fileLength;						// unsigned int
		public long fileTimestamp;					// unsigned int
		public int fileMilliseconds;				// unsigned short
		public short fileTimezoneOffsetInMinutes;	// signed short
		
		public long fileCRC32;						// unsigned int
		public byte[] activityData;
		
		public byte[] rawData;
		
		public Response(FileGetRequest.Response response) {
			super();
			
			if (response != null) {
				this.result = response.result;
				this.status = response.status;
				this.handle = response.handle;
			}
		}
	}
	private Response mResponse;
	
	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	@Override
	public String getRequestName() {
		return "fileGetActivity";
	}
	
	public void buildRequest(short fileHandle) {
		short handle = fileHandle;
		int offset = 0;
		int length = Constants.FILE_LENGTH_ACTIVITY_FILE;
		
		super.buildRequest(handle, offset, length);
	}
	
	@Override
	public void parseFile(byte[] bytes) {
		super.parseFile(bytes);
		
		mResponse.rawData = bytes;
		
		// FIXME: check for bytes.length >= 16 + 4. Returns boolean to indicate whether the parsing succeeded. 
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		mResponse.fileHandle = Convertor.unsignedShortToInteger(byteBuffer.getShort());
		mResponse.fileFormat = Convertor.unsignedShortToInteger(byteBuffer.getShort(2));
		mResponse.fileLength = Convertor.unsignedIntToLong(byteBuffer.getInt(4));

		mResponse.fileTimestamp = Convertor.unsignedIntToLong(byteBuffer.getInt(8));
		mResponse.fileMilliseconds = Convertor.unsignedShortToInteger(byteBuffer.getShort(12));
		mResponse.fileTimezoneOffsetInMinutes = byteBuffer.getShort(14);

		byteBuffer.position(16);
		mResponse.activityData = new byte[bytes.length - 16 - 4];
		byteBuffer.get(mResponse.activityData);

		mResponse.fileCRC32 = byteBuffer.getInt(bytes.length - 4);

		if (!validateFileSize(bytes) || !validateCRC(bytes)) {
			mResponse.result = Constants.RESPONSE_WRONG_CRC; 
			mIsCompleted = true;
		}
	}
	
	@Override
	protected Response parseResponse(byte[] bytes) {
		mResponse = new Response(super.parseResponse(bytes));
		return mResponse;
	}
	
	private boolean validateFileSize(byte[] bytes) {
		return bytes.length == mResponse.fileLength;
	}
	
	private boolean validateCRC(byte[] bytes) {
		long CRC = CRCCalculator.calculateCRC(bytes.length - 4, bytes); 
		return CRC == mResponse.fileCRC32;
	}
	
	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = super.getResponseDescriptionJSON();
		if (json == null) {
			json = new JSONObject();
		}
		
		try {
			if (mResponse != null) {
				json.put("fileHandle", mResponse.fileHandle);
				json.put("fileFormat", mResponse.fileFormat);
				json.put("fileLength", mResponse.fileLength);
				json.put("fileTimestamp", mResponse.fileTimestamp);
				json.put("fileMilliseconds", mResponse.fileMilliseconds);
				json.put("fileTimezoneOffsetInMinutes", mResponse.fileTimezoneOffsetInMinutes);
				json.put("fileCRC", mResponse.fileCRC32);
				json.put("activityData", Convertor.bytesToString(mResponse.activityData));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
