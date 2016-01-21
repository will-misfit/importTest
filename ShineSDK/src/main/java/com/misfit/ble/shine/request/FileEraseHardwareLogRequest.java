package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

public class FileEraseHardwareLogRequest extends FileEraseRequest {
	
	@Override
	public String getRequestName() {
		return "fileEraseHardwareLog";
	}
	
	public void buildRequest() {
		short fileHandle = Constants.FILE_HANDLE_HARDWARE_LOG;
		super.buildRequest(fileHandle);
	}
}
