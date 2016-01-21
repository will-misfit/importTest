package com.misfit.ble.shine.controller;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfileCore;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.request.SerialNumberChangeAndLockRequest;
import com.misfit.ble.shine.request.SerialNumberGetLockRequest;
import com.misfit.ble.shine.request.SerialNumberSetRequest;

public class ChangeSerialNumberPhaseController extends PhaseController {
	
	private boolean mIsLocked = false;
	private String mSerialNumber;
	private String mCurrentSerialNumber;

	private ShineProfile.ConfigurationCallback mConfigurationCallback;

	public ChangeSerialNumberPhaseController(PhaseControllerCallback callback, ShineProfile.ConfigurationCallback configurationCallback, String serialNumber, String currentSerialNumber) {
        super(ActionID.CHANGE_SERIAL_NUMBER,
				LogEventItem.EVENT_CHANGE_SERIAL_NUMBER,
				callback);

		mConfigurationCallback = configurationCallback;
        mSerialNumber = serialNumber;
        mCurrentSerialNumber = currentSerialNumber;
    }

	public String getSerialNumber() {
		return mSerialNumber;
	}

	public String getCurrentSerialNumber() {
		return mCurrentSerialNumber;
	}
	
	@Override
	public void start() {
		super.start();
		sendNextRequest();
	}

	@Override
	public void stop() {
		super.stop();
		mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.INTERRUPTED, null);
	}
	
	@Override
	public void onRequestSentResult(Request request, int result) {
		if (request == null || request.equals(mCurrentRequest) == false)
			return;
		super.onRequestSentResult(request, result);
		
		if (result == ShineProfileCore.RESULT_FAILURE) {
			mResult = RESULT_SENDING_REQUEST_FAILED;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.FAILED, null);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			mResult = RESULT_TIMED_OUT;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.TIMED_OUT, null);
			return;
		} else if (result == ShineProfileCore.RESULT_UNSUPPORTED) {
			mResult = RESULT_UNSUPPORTED;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.UNSUPPORTED, null);
			return;
		}
		
		if (request instanceof SerialNumberChangeAndLockRequest) {
			sendNextRequest();
		} if (request instanceof SerialNumberSetRequest) {
			sendNextRequest();
		}
	}
	
	@Override
	public void onResponseReceivedResult(Request request, int result) {
		if (request == null || request.equals(mCurrentRequest) == false)
			return;
		super.onResponseReceivedResult(request, result);
		
		if (result == ShineProfileCore.RESULT_FAILURE) {
			mResult = RESULT_RECEIVE_RESPONSE_FAILED;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.FAILED, null);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			mResult = RESULT_TIMED_OUT;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.TIMED_OUT, null);
			return;
		}
		
		if (request instanceof SerialNumberGetLockRequest) {
			SerialNumberGetLockRequest getLockRequest = (SerialNumberGetLockRequest)request;
			SerialNumberGetLockRequest.Response response = getLockRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				mResult = RESULT_REQUEST_ERROR;
				mPhaseControllerCallback.onPhaseControllerFailed(this);
				mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.FAILED, null);
				return;
			}
			
			mIsLocked = response.isLocked;
			sendNextRequest();
		}
	}
	
	private void sendNextRequest() {
		Request nextRequest = null;
		
		if (mCurrentRequest == null) {
			if (mCurrentSerialNumber.equals(Constants.FACTORY_SERIAL_NUMBER)) {
				nextRequest = buildRequest(SerialNumberSetRequest.class);
			} else {
				nextRequest = buildRequest(SerialNumberGetLockRequest.class);
			}
		} else if (mCurrentRequest instanceof SerialNumberGetLockRequest) {
			if (mIsLocked == false) {
				nextRequest = buildRequest(SerialNumberChangeAndLockRequest.class);
			}
		} else if (mCurrentRequest instanceof SerialNumberChangeAndLockRequest) {
			mResult = RESULT_SUCCESS;
			mPhaseControllerCallback.onPhaseControllerCompleted(this);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.SUCCEEDED, null);
			return;
		} else if (mCurrentRequest instanceof SerialNumberSetRequest) {
			mResult = RESULT_SUCCESS;
			mPhaseControllerCallback.onPhaseControllerCompleted(this);
			mPhaseControllerCallback.onPhaseControllerUpdateSerialNumber(this);

			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.SUCCEEDED, null);
			return;
		}
		
		sendRequest(nextRequest);
	}
	
	private Request buildRequest(Class<? extends Request> requestType) {
		Request request = null;
		
		if (requestType.equals(SerialNumberGetLockRequest.class)) {
			SerialNumberGetLockRequest getLockRequest = new SerialNumberGetLockRequest();
			getLockRequest.buildRequest();
			request = getLockRequest;
		} else if (requestType.equals(SerialNumberChangeAndLockRequest.class)) {
			SerialNumberChangeAndLockRequest changeAndLockRequest = new SerialNumberChangeAndLockRequest();
			if (changeAndLockRequest.buildRequest(mSerialNumber)) {
				request = changeAndLockRequest;
			}
		} else if (requestType.equals(SerialNumberSetRequest.class)) {
			SerialNumberSetRequest setSerialNumberRequest = new SerialNumberSetRequest();
			if (setSerialNumberRequest.buildRequest(mSerialNumber)) {
				request = setSerialNumberRequest;
			}
		}
		
		return request;
	}
	
	@Override
	public String getPhaseName() {
		return "ChangeSerialNumberPhaseController";
	}
}
