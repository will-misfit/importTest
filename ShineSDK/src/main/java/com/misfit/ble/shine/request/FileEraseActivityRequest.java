package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

public class FileEraseActivityRequest extends FileEraseRequest {
	
	@Override
	public String getRequestName() {
		return "fileEraseActivity";
	}
	
	public void buildRequest() {
		short fileHandle = Constants.FILE_HANDLE_ACTIVITY_FILE;
		super.buildRequest(fileHandle);
	}
}
