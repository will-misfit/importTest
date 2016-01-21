package com.misfit.ble.shine.controller;

import android.util.Log;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfileCore;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.util.LogUtils;

import java.util.List;

/**
 * Created by Quoc-Hung Le on 8/27/15.
 */
public class ControllerBuilder extends PhaseController {
	private static final String TAG = LogUtils.makeTag(ControllerBuilder.class);

	public interface Callback {
		void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode);
	}

	private List<Request> mRequests;
	private int mCurrentIndex;
	private int nRequest;
	private Callback mRequestBuilderCallback;

	public ControllerBuilder(ActionID actionID, String startLogEventName, List<Request> requests,
							 PhaseControllerCallback phaseControllerCallback, Callback requestBuilderCallback) {
		super(actionID, startLogEventName, phaseControllerCallback);

		mRequests = requests;
		nRequest = mRequests.size();

		mCurrentIndex = 0;

		mRequestBuilderCallback = requestBuilderCallback;
	}

	@Override
	public void start() {
		super.start();
		executeNextRequest();
	}

	@Override
	public void stop() {
		super.stop();
		mRequestBuilderCallback.onCompleted(this, mRequests, ShineProfile.ActionResult.INTERRUPTED);
	}

	@Override
	public void onRequestSentResult(Request request, int result) {
		if (request == null || request.equals(mCurrentRequest) == false)
			return;
		super.onRequestSentResult(request, result);

		if (result == ShineProfileCore.RESULT_FAILURE) {
			mResult = RESULT_SENDING_REQUEST_FAILED;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mRequestBuilderCallback.onCompleted(this, mRequests, ShineProfile.ActionResult.FAILED);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			mResult = RESULT_TIMED_OUT;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mRequestBuilderCallback.onCompleted(this, mRequests, ShineProfile.ActionResult.TIMED_OUT);
			return;
		} else if (result == ShineProfileCore.RESULT_UNSUPPORTED) {
			mResult = RESULT_UNSUPPORTED;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mRequestBuilderCallback.onCompleted(this, mRequests, ShineProfile.ActionResult.UNSUPPORTED);
			return;
		}

		if (!request.isWaitingForResponse()) {
			executeNextRequest();
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
			mRequestBuilderCallback.onCompleted(this, mRequests, ShineProfile.ActionResult.FAILED);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			mResult = RESULT_TIMED_OUT;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mRequestBuilderCallback.onCompleted(this, mRequests, ShineProfile.ActionResult.TIMED_OUT);
			return;
		}

		if (request.getResponse() == null) {
			Log.e(TAG, "onResponseReceivedResult called but response is NULL.");
		}

		if (request.getResponse() == null || request.getResponse().result != Constants.RESPONSE_SUCCESS) {
			mResult = RESULT_REQUEST_ERROR;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mRequestBuilderCallback.onCompleted(this, mRequests, ShineProfile.ActionResult.FAILED);
			return;
		}

		executeNextRequest();
	}

	private void executeNextRequest() {
		if (mCurrentIndex < nRequest) {
			sendRequest(mRequests.get(mCurrentIndex));
			mCurrentIndex++;
		} else if (mCurrentIndex == nRequest) {
			setResultCode(PhaseController.RESULT_SUCCESS);
			getPhaseControllerCallback().onPhaseControllerCompleted(this);
			mRequestBuilderCallback.onCompleted(this, mRequests, ShineProfile.ActionResult.SUCCEEDED);
		} else {
			mResult = RESULT_FLOW_BROKEN;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mRequestBuilderCallback.onCompleted(this, mRequests, ShineProfile.ActionResult.INTERNAL_ERROR);
		}
	}
}
