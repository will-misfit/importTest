package com.misfit.ble.shine.controller;

import android.util.Log;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfileCore;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.FileAbortRequest;
import com.misfit.ble.shine.request.FileStreamingUserInputEventsRequest;
import com.misfit.ble.shine.request.Request;

public class StreamingUserInputEventsPhaseController extends PhaseController {
	private static final String TAG = StreamingUserInputEventsPhaseController.class.getName();

	private ShineProfile.StreamingCallback mStreamingCallback;

	private FileStreamingUserInputEventsRequest mStreamingRequest;
	private String mDeviceAddress;
	private short mFileHandle;

    public StreamingUserInputEventsPhaseController(PhaseControllerCallback callback, ShineProfile.StreamingCallback streamingCallback, String deviceAddress, short fileHandle) {
        super(ActionID.STREAM_USER_INPUT_EVENTS,
				LogEventItem.EVENT_STREAM_USER_INPUT_EVENTS,
				callback);

		mStreamingCallback = streamingCallback;
		mDeviceAddress = deviceAddress;
        mFileHandle = fileHandle;
    }

    @Override
	public void start() {
		super.start();
		
		mStreamingRequest = new FileStreamingUserInputEventsRequest(mDeviceAddress, mEventListener);
		mStreamingRequest.buildRequest(mFileHandle);
		sendRequest(mStreamingRequest);
	}
	
	@Override
	public void stop() {
		if (mStreamingRequest != null) {
			mStreamingRequest.cancelRequest();
		}
		
		FileAbortRequest abortRequest = new FileAbortRequest();
		abortRequest.buildRequest(mFileHandle);
		sendRequest(abortRequest);
	}
	
	@Override
	public String getPhaseName() {
		return "streamingUserInputEventsPhase";
	}
	
	@Override
	public void onRequestSentResult(Request request, int result) {
		if (request == null || request.equals(mCurrentRequest) == false)
			return;
		super.onRequestSentResult(request, result);
		
		if (result == ShineProfileCore.RESULT_FAILURE) {
			mResult = RESULT_SENDING_REQUEST_FAILED;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mStreamingCallback.onStreamingStopped(ShineProfile.ActionResult.FAILED);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			mResult = RESULT_TIMED_OUT;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mStreamingCallback.onStreamingStopped(ShineProfile.ActionResult.TIMED_OUT);
			return;
		} else if (result == ShineProfileCore.RESULT_UNSUPPORTED) {
			mResult = RESULT_UNSUPPORTED;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mStreamingCallback.onStreamingStopped(ShineProfile.ActionResult.UNSUPPORTED);
			return;
		}
		
		if (request instanceof FileAbortRequest) {
			mResult = RESULT_INTERRUPTED;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mPhaseControllerCallback.onPhaseControllerSubmitLogSession(this);
			mStreamingCallback.onStreamingStopped(ShineProfile.ActionResult.INTERRUPTED);
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
			mStreamingCallback.onStreamingStopped(ShineProfile.ActionResult.FAILED);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			mResult = RESULT_TIMED_OUT;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mStreamingCallback.onStreamingStopped(ShineProfile.ActionResult.TIMED_OUT);
			return;
		}

		if (request instanceof FileStreamingUserInputEventsRequest) {
			FileStreamingUserInputEventsRequest streamingRequest = (FileStreamingUserInputEventsRequest)request;
			FileStreamingUserInputEventsRequest.Response response = streamingRequest.getResponse();
			if (response.result != Constants.RESPONSE_SUCCESS) {
				mResult = RESULT_REQUEST_ERROR;
				mPhaseControllerCallback.onPhaseControllerFailed(this);
				mPhaseControllerCallback.onPhaseControllerSubmitLogSession(this);
				mStreamingCallback.onStreamingStarted(ShineProfile.ActionResult.FAILED);
			} else {
				Log.e(TAG, "Unexpected streaming response!");
			}
		}
	}

	private FileStreamingUserInputEventsRequest.EventListener mEventListener = new FileStreamingUserInputEventsRequest.EventListener() {
		
		@Override
		public void onButtonEventReceived(int eventID) {
			if (mStreamingCallback != null) {
				mStreamingCallback.onStreamingButtonEvent(eventID);
			}
		}

		@Override
		public void onStreamingStarted() {
			if (mStreamingCallback != null) {
				mStreamingCallback.onStreamingStarted(ShineProfile.ActionResult.SUCCEEDED);
			}
		}

		@Override
		public void onHeartbeatReceived() {
			if(mStreamingCallback != null) {
				mStreamingCallback.onHeartbeatReceived();
			}
		}
	};
}
