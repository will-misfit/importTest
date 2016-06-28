package com.misfit.ble.shine.controller;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfileCore;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.GetConnectionParameterRequest;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.request.SetConnectionParameterRequest;

import java.util.Hashtable;

public class SetConnectionParametersPhaseController extends PhaseController {

	private static final int MAX_RETRY_COUNT = 5;

	private ShineProfile.ConfigurationCallback mConfigurationCallback;

	private ShineConnectionParameters mExpectedConnectionParameters;
	private ShineConnectionParameters mCurrConnectionParameters;
    private double mMinConnectionInterval = 0f;
	private double mMaxConnectionInterval = 0f;
	private int mNumOfSetActionAttempts = 0;

    public SetConnectionParametersPhaseController(PhaseControllerCallback callback, ShineProfile.ConfigurationCallback configurationCallback, ShineConnectionParameters connectionParametersRequested) {
        super(ActionID.SET_CONNECTION_PARAMETERS,
				LogEventItem.EVENT_SET_CONNECTION_PARAMETERS,
				callback);
		//interval less than 7.5 would lead to timeout in Flash
		double expectedInterval = Math.max(connectionParametersRequested.getConnectionInterval(), Constants.MINIMUM_CONNECTION_INTERVAL);
        mExpectedConnectionParameters = new ShineConnectionParameters(expectedInterval,
				connectionParametersRequested.getConnectionLatency(),
				connectionParametersRequested.getSupervisionTimeout());
		mConfigurationCallback = configurationCallback;
    }
	
	@Override
	public void start() {
		super.start();

		GetConnectionParameterRequest getConnectionParameterRequest = new GetConnectionParameterRequest();
		getConnectionParameterRequest.buildRequest();
		sendRequest(getConnectionParameterRequest);
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
		
		if (request instanceof GetConnectionParameterRequest) {
			GetConnectionParameterRequest getConnectionParameterRequest = (GetConnectionParameterRequest)request;
			GetConnectionParameterRequest.Response response = getConnectionParameterRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
                notifySetConnParamsFailed();
				return;
			}

			mCurrConnectionParameters = new ShineConnectionParameters(response.connectionInterval,
																  response.connectionLatency, 
																  response.supervisionTimeout);
			if (isConnectionParamsAcceptable(mExpectedConnectionParameters, mCurrConnectionParameters)) {
                notifySetConnParamsSucceed();
            } else {
                attemptToUpdateConnectionParameters();
            }
			
		} else if (request instanceof SetConnectionParameterRequest) {
			SetConnectionParameterRequest setConnectionParameterRequest = (SetConnectionParameterRequest)request;
			SetConnectionParameterRequest.Response response = setConnectionParameterRequest.getResponse();
			
			mCurrConnectionParameters = new ShineConnectionParameters(response.connectionInterval,
																  response.connectionLatency, 
																  response.supervisionTimeout);
			boolean isMaxRetryAndMajorParamsAcceptable = mNumOfSetActionAttempts >= MAX_RETRY_COUNT
														&& isMajorConnectionParamsAcceptable(mExpectedConnectionParameters, mCurrConnectionParameters);
			if (response.result == Constants.RESPONSE_SUCCESS
					&& (isMaxRetryAndMajorParamsAcceptable || isConnectionParamsAcceptable(mExpectedConnectionParameters, mCurrConnectionParameters))) {
				notifySetConnParamsSucceed();
			} else if (mNumOfSetActionAttempts >= MAX_RETRY_COUNT) {
				notifySetConnParamsFailed();
			} else {
				attemptToUpdateConnectionParameters();
			}
		}
	}

	private boolean isConnectionParamsAcceptable(ShineConnectionParameters lhs, ShineConnectionParameters rhs) {
		int lhsTimeoutBlockIndex = (int)Math.floor(lhs.getSupervisionTimeout() * 1.0 / Constants.SUPERVISION_TIMEOUT_UNIT);
		int rhsTimeoutBlockIndex = (int)Math.floor(rhs.getSupervisionTimeout() * 1.0 / Constants.SUPERVISION_TIMEOUT_UNIT);

		return (isConnectionIntervalAcceptable(lhs.getConnectionInterval(), rhs.getConnectionInterval())
                && mExpectedConnectionParameters.getConnectionLatency() == mCurrConnectionParameters.getConnectionLatency()
				&& lhsTimeoutBlockIndex == rhsTimeoutBlockIndex);
	}

    /**
     * compare ConnectionInterval separately
     * for interval in getConnParams() response, compare by the value/STEP, to make sure it in the range [expected - STEP*1, expected + STEP*1]
     * for interval in last setConnParams() response, assert it whether among range [min, max] which is sent last time
     * */
    private boolean isConnectionIntervalAcceptable(double expectedInterval, double responseInterval) {
        if (mNumOfSetActionAttempts > 0) {
            return (responseInterval >= mMinConnectionInterval && responseInterval <= mMaxConnectionInterval);
        } else {
            int lhsIntervalBlockIndex = (int)Math.floor(expectedInterval / Constants.CONNECTION_INTERVAL_STEP);
            int rhsIntervalBlockIndex = (int)Math.floor(responseInterval / Constants.CONNECTION_INTERVAL_STEP);
            return Math.abs(lhsIntervalBlockIndex - rhsIntervalBlockIndex) <= 1;
        }
    }

    /**
     * if in need, only compare connection intervals and latency and skip superision timeout
     * */
    private boolean isMajorConnectionParamsAcceptable(ShineConnectionParameters expected, ShineConnectionParameters response) {
        return isConnectionIntervalAcceptable(expected.getConnectionInterval(), response.getConnectionInterval())
            && expected.getConnectionLatency() == response.getConnectionLatency();
    }

    /**
     * attempt to send a setConnParams request after the assertion of ConnectionParams not pass
     * */
	private void attemptToUpdateConnectionParameters() {
		Request request = buildSetConnectionParamsRequest();
		++mNumOfSetActionAttempts;
		sendRequest(request);
	}

    /**
     * in case setConnectionParams requests succeed eventually
     * */
    private void notifySetConnParamsSucceed() {
        mResult = RESULT_SUCCESS;
        mPhaseControllerCallback.onPhaseControllerCompleted(this);
        Hashtable<ShineProperty, Object> objects = new Hashtable<>();
        objects.put(ShineProperty.CONNECTION_PARAMETERS, mCurrConnectionParameters);
        mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.SUCCEEDED, objects);
    }

    /**
     * in case all setConnectionParams requests fail
     * */
    private void notifySetConnParamsFailed() {
        mResult = RESULT_REQUEST_ERROR;
        mPhaseControllerCallback.onPhaseControllerFailed(this);
        mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.FAILED, null);
    }

    /**
     * build a SetConnParamsRequest, without assertion of ConnectionParams
     * */
	private Request buildSetConnectionParamsRequest() {
        double expectedInterval = mExpectedConnectionParameters.getConnectionInterval();
		double delta = 0;

        if (!isConnectionIntervalAcceptable(expectedInterval, mCurrConnectionParameters.getConnectionInterval())) {
            delta = mNumOfSetActionAttempts * Constants.CONNECTION_INTERVAL_STEP;
		}
		generateInterval(expectedInterval, delta);

		SetConnectionParameterRequest setConnectionParameterRequest = new SetConnectionParameterRequest();
		setConnectionParameterRequest.buildRequest(mMinConnectionInterval, mMaxConnectionInterval,
            mExpectedConnectionParameters.getConnectionLatency(), mExpectedConnectionParameters.getSupervisionTimeout());
		return setConnectionParameterRequest;
	}

	private void generateInterval(double expectedInterval, double delta) {
		mMinConnectionInterval = expectedInterval + delta;
		mMaxConnectionInterval = mMinConnectionInterval +  Constants.CONNECTION_INTERVAL_STEP - 0.01f;
	}

	@Override
	public String getPhaseName() {
		return "SetConnectionParametersPhase";
	}
}
