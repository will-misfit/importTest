package com.misfit.ble.shine.controller;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfileCore;
import com.misfit.ble.shine.ShineStreamingConfiguration;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.request.SetConnectionHeartbeatIntervalRequest;
import com.misfit.ble.shine.request.SetNumberOfMappedEventPacketsRequest;

public class SetStreamingConfigurationPhaseController extends PhaseController {

	private ShineStreamingConfiguration mConfiguration;
	private ShineProfile.ConfigurationCallback mConfigurationCallback;

    public SetStreamingConfigurationPhaseController(PhaseControllerCallback callback, ShineProfile.ConfigurationCallback configurationCallback, ShineStreamingConfiguration configuration) {
        super(ActionID.SET_STREAMING_CONFIGURATION,
				LogEventItem.EVENT_SET_STREAMING_CONFIGURATION,
				callback);
		mConfigurationCallback = configurationCallback;
        mConfiguration = configuration;
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
		sendNextRequest();
	}
	
	private Request buildRequest(Class<? extends Request> requestType) {
		Request request = null;
		
		if (requestType.equals(SetNumberOfMappedEventPacketsRequest.class)) {
			SetNumberOfMappedEventPacketsRequest numberOfPacketsRequest = new SetNumberOfMappedEventPacketsRequest();
			numberOfPacketsRequest.buildRequest(mConfiguration.mNumberOfMappedEventPackets);
			request = numberOfPacketsRequest;
		} else if (requestType.equals(SetConnectionHeartbeatIntervalRequest.class)) {
			SetConnectionHeartbeatIntervalRequest heartbeatIntervalRequest = new SetConnectionHeartbeatIntervalRequest();
			heartbeatIntervalRequest.buildRequest(mConfiguration.mConnectionHeartbeatInterval);
			request = heartbeatIntervalRequest;
		}
		return request;
	}
	
	private void sendNextRequest() {
		Request nextRequest = null;
		
		if (mCurrentRequest == null) {
			nextRequest = buildRequest(SetNumberOfMappedEventPacketsRequest.class);
		} else if (mCurrentRequest instanceof SetNumberOfMappedEventPacketsRequest) {
			nextRequest = buildRequest(SetConnectionHeartbeatIntervalRequest.class);
		} else if (mCurrentRequest instanceof SetConnectionHeartbeatIntervalRequest) {
			mResult = RESULT_SUCCESS;
			mPhaseControllerCallback.onPhaseControllerCompleted(this);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.SUCCEEDED, null);
			return;
		}
		
		sendRequest(nextRequest);
	}
	
	@Override
	public String getPhaseName() {
		return "SetStreamingConfigurationPhase";
	}
}
