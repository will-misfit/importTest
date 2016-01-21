package com.misfit.ble.shine.controller;

import android.util.Log;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfileCore;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.GetConnectionParameterRequest;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.request.SetConnectionParameterRequest;

import java.util.Hashtable;

public class SetConnectionParametersPhaseController extends PhaseController {

	private static final String TAG = SetConnectionParametersPhaseController.class.getName();

	private static final int MAX_RETRY_COUNT = 5;

	private ShineProfile.ConfigurationCallback mConfigurationCallback;

	private ShineConnectionParameters mConnectionParametersRequested;
	private ShineConnectionParameters mConnectionParameters;
	private int mNumOfSetConnectionIntervalAttempts = 0;

    public SetConnectionParametersPhaseController(PhaseControllerCallback callback, ShineProfile.ConfigurationCallback configurationCallback, ShineConnectionParameters connectionParametersRequested) {
        super(ActionID.SET_CONNECTION_PARAMETERS,
				LogEventItem.EVENT_SET_CONNECTION_PARAMETERS,
				callback);

        mConnectionParametersRequested = connectionParametersRequested;
		mConfigurationCallback = configurationCallback;
    }

	public void setConnectionParameters(ShineConnectionParameters connectionParameters) {
		mConnectionParameters = connectionParameters;
	}

	public ShineConnectionParameters getConnectionParameters() {
		return mConnectionParameters;
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
				mResult = RESULT_REQUEST_ERROR;
				mPhaseControllerCallback.onPhaseControllerFailed(this);
				mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.FAILED, null);
				return;
			}

			mConnectionParameters = new ShineConnectionParameters(response.connectionInterval, 
																  response.connectionLatency, 
																  response.supervisionTimeout);

			attemptToUpdateConnectionParameters();
			
		} else if (request instanceof SetConnectionParameterRequest) {
			SetConnectionParameterRequest setConnectionParameterRequest = (SetConnectionParameterRequest)request;
			SetConnectionParameterRequest.Response response = setConnectionParameterRequest.getResponse();
			
			mConnectionParameters = new ShineConnectionParameters(response.connectionInterval, 
																  response.connectionLatency, 
																  response.supervisionTimeout);
			
			if (response.result != Constants.RESPONSE_SUCCESS ) {
				attemptToUpdateConnectionParameters();
				return;
			} else if (!isEqualConnectionParameters(mConnectionParameters, mConnectionParametersRequested)) {
				attemptToUpdateConnectionParameters();
				return;
			}
			
			mResult = RESULT_SUCCESS;
			mPhaseControllerCallback.onPhaseControllerCompleted(this);
			Hashtable<ShineProperty, Object> objects = new Hashtable<>();
			objects.put(ShineProperty.CONNECTION_PARAMETERS, mConnectionParameters);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.SUCCEEDED, objects);
		}
	}

	private boolean isEqualConnectionParameters(ShineConnectionParameters lhs, ShineConnectionParameters rhs) {
		int lhsIntervalBlockIndex = (int)Math.floor(lhs.getConnectionInterval() / Constants.CONNECTION_INTERVAL_STEP);
		int rhsIntervalBlockIndex = (int)Math.floor(rhs.getConnectionInterval() / Constants.CONNECTION_INTERVAL_STEP);

		int lhsSupervisionBlockIndex = (int)Math.floor(lhs.getSupervisionTimeout() * 1.0 / Constants.SUPERVISION_TIMEOUT_UNIT);
		int rhsSupervisionBlockIndex = (int)Math.floor(rhs.getSupervisionTimeout() * 1.0 / Constants.SUPERVISION_TIMEOUT_UNIT);

		if (lhsIntervalBlockIndex == rhsIntervalBlockIndex
				&& lhsSupervisionBlockIndex == rhsSupervisionBlockIndex
				&& mConnectionParametersRequested.getConnectionLatency() == mConnectionParameters.getConnectionLatency()) {
			return true;
		}

		return false;
	}

	private void attemptToUpdateConnectionParameters() {
		if (mNumOfSetConnectionIntervalAttempts >= MAX_RETRY_COUNT) {
			mResult = RESULT_REQUEST_ERROR;
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.FAILED, null);
			return;
		}

		Request request = buildSetConnectionParamsRequest();
		if (null == request) {
			// Already at that connection interval
			mResult = RESULT_SUCCESS;
			mPhaseControllerCallback.onPhaseControllerCompleted(this);
			Hashtable<ShineProperty, Object> objects = new Hashtable<>();
			objects.put(ShineProperty.CONNECTION_PARAMETERS, mConnectionParameters);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.SUCCEEDED, objects);
			return;
		}

		sendRequest(request);
		++mNumOfSetConnectionIntervalAttempts;
	}

	private Request buildSetConnectionParamsRequest() {
		double minConnectionInterval = mConnectionParametersRequested.getConnectionInterval() + mNumOfSetConnectionIntervalAttempts * Constants.CONNECTION_INTERVAL_STEP;
		double maxConnectionInterval = minConnectionInterval + Constants.CONNECTION_INTERVAL_STEP - 0.01f;
		ShineConnectionParameters nextConnectionParams = new ShineConnectionParameters(minConnectionInterval,
				mConnectionParametersRequested.getConnectionLatency(),
				mConnectionParametersRequested.getSupervisionTimeout());

		if (isEqualConnectionParameters(nextConnectionParams, mConnectionParameters)) {
			Log.i(TAG, "Already at requested connection parameters!");
			return null;
		}

		SetConnectionParameterRequest setConnectionParameterRequest = new SetConnectionParameterRequest();
		setConnectionParameterRequest.buildRequest(minConnectionInterval, maxConnectionInterval, mConnectionParametersRequested.getConnectionLatency(), mConnectionParametersRequested.getSupervisionTimeout());
		return setConnectionParameterRequest;
	}
	
	@Override
	public String getPhaseName() {
		return "SetConnectionParametersPhase";
	}
}
