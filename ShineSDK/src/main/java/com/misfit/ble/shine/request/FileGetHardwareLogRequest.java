package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

public class FileGetHardwareLogRequest extends FileGetRequest {
	
	public static class Response extends FileGetRequest.Response {
		public byte[] data;
		
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
		return "fileGetHardwareLog";
	}
	
	@Override
	public int getTimeOut() {
		return 20000;
	}
	
	public void buildRequest() {
		short handle = Constants.FILE_HANDLE_HARDWARE_LOG;
		int offset = 0;
		int length = Constants.FILE_LENGTH_HARDWARE_LOG;
		
		super.buildRequest(handle, offset, length);
	}
	
	@Override
	public void parseFile(byte[] bytes) {
		super.parseFile(bytes);
		mResponse.data = bytes;
	}
	
	@Override
	protected Response parseResponse(byte[] bytes) {
		mResponse = new Response(super.parseResponse(bytes));
		return mResponse;
	}
	
	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = super.getResponseDescriptionJSON();
		if (json == null) {
			json = new JSONObject();
		}
		
		try {
			if (mResponse != null) {
				json.put("data", Convertor.bytesToString(mResponse.data));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
