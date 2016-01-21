package com.misfit.ble.shine.controller;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.request.Request;

public abstract class PhaseController {
    public static final int RESULT_UNKNOWN = -1;
    public static final int RESULT_SUCCESS = 0; // success
    public static final int RESULT_SENDING_REQUEST_FAILED = 1; // failed
    public static final int RESULT_RECEIVE_RESPONSE_FAILED = 2; // failed
    public static final int RESULT_TIMED_OUT = 3; // timed out
    public static final int RESULT_REQUEST_ERROR = 4; // failed
    public static final int RESULT_PARSE_ERROR = 5; // failed
    public static final int RESULT_FLOW_BROKEN = 6; // internal error
    public static final int RESULT_INTERRUPTED = 7; // interrupted
    public static final int RESULT_DATA_TRANSFER_FAILED = 8; // failed
    public static final int RESULT_UNSUPPORTED = 9; // unsupported

    protected PhaseControllerCallback mPhaseControllerCallback;
    protected Request mCurrentRequest;
    protected int mResult;

    public interface PhaseControllerCallback {
		void onPhaseControllerSendRequest(PhaseController syncPhase, Request request);
        void onPhaseControllerCompleted(PhaseController syncPhase);
        void onPhaseControllerFailed(PhaseController syncPhase);
		void onPhaseControllerDisconnect(PhaseController syncPhase);
		void onPhaseControllerUpdateSerialNumber(PhaseController syncPhase);
		void onPhaseControllerSubmitLogSession(PhaseController syncPhase);
    }

    private ActionID mActionID = null;
    private String mStartLogEventName = null;

	public ActionID getActionID() {
		return mActionID;
	}

	public String getStartLogEventName() {
		return mStartLogEventName;
	}

	public PhaseControllerCallback getPhaseControllerCallback() {
		return mPhaseControllerCallback;
	}

	protected PhaseController(ActionID actionID, String startLogEventName, PhaseControllerCallback phaseControllerCallback) {
		mActionID = actionID;
		mStartLogEventName = startLogEventName;

		mPhaseControllerCallback = phaseControllerCallback;
		mResult = RESULT_UNKNOWN;
	}

	public void start() {}

	public void stop() {
		mResult = RESULT_INTERRUPTED;
		mPhaseControllerCallback.onPhaseControllerFailed(this);
		mCurrentRequest = null;
	}

	public void onResponseReceivedResult(Request request, int result) {}

	public void onRequestSentResult(Request request, int result) {}

	public void setResultCode(int resultCode) {
		mResult = resultCode;
	}

	public int getResultCode() {
		return mResult;
	}
	
	public boolean hasFinished() {
		return mResult != RESULT_UNKNOWN;
	}
	
	public String getResultMessage() {
		String message = this.getClass().getName();
		if (mResult == RESULT_SUCCESS) {
			message = message + " - " + "SUCCEEDED";
		} else if (mResult == RESULT_SENDING_REQUEST_FAILED) {
			message = message + " - " + "SENDING REQUEST FAILED";
		} else if (mResult == RESULT_RECEIVE_RESPONSE_FAILED) {
			message = message + " - " + "RECEIVE RESPONSE FAILED";
		} else if (mResult == RESULT_TIMED_OUT) {
			message = message + " - " + "TIMED OUT";
		} else if (mResult == RESULT_REQUEST_ERROR) {
			message = message + " - " + "REQUEST ERROR";
		} else if (mResult == RESULT_PARSE_ERROR) {
			message = message + " - " + "PARSE ERROR";
		} else if (mResult == RESULT_FLOW_BROKEN) {
			message = message + " - " + "FLOW BROKEN";
		} else if (mResult == RESULT_INTERRUPTED) {
			message = message + " - " + "INTERRUPTED";
		} else if (mResult == RESULT_DATA_TRANSFER_FAILED) {
			message = message + " - " + "DATA TRANSFER FAILED";
		} else if (mResult == RESULT_UNSUPPORTED) {
			message = message + " - " + "UNSUPPORTED";
		} else {
			message = message + " - UNKNOWN";
		}
		return message;
	}

	public String getPhaseName() {
		return null;
	}

	public void sendRequest(Request request) {
		if (request != null) {
			mCurrentRequest = request;
			mPhaseControllerCallback.onPhaseControllerSendRequest(this, request);
		} else {
			mResult = RESULT_FLOW_BROKEN;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			// Do not need to invoke every controller's callback because if request equal to NULL, controller must be equal to NULL
		}
	}
}
