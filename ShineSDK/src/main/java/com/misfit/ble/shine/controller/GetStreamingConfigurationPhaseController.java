package com.misfit.ble.shine.controller;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfileCore;
import com.misfit.ble.shine.ShineStreamingConfiguration;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.GetConnectionHeartbeatIntervalRequest;
import com.misfit.ble.shine.request.GetNumberOfMappedEventPacketsRequest;
import com.misfit.ble.shine.request.Request;

import java.util.Hashtable;

public class GetStreamingConfigurationPhaseController extends PhaseController {

	private ShineProfile.ConfigurationCallback mConfigurationCallback;
	private ShineStreamingConfiguration mConfiguration;

    public GetStreamingConfigurationPhaseController(PhaseControllerCallback callback, ShineProfile.ConfigurationCallback configurationCallback) {
        super(ActionID.GET_STREAMING_CONFIGURATION,
				LogEventItem.EVENT_GET_STREAMING_CONFIGURATION,
				callback);

		mConfigurationCallback = configurationCallback;
        mConfiguration = new ShineStreamingConfiguration();
    }

    public ShineStreamingConfiguration getStreamingConfiguration() {
		return mConfiguration;
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
			// Continue with next request
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
		
		if (request instanceof GetNumberOfMappedEventPacketsRequest) {
			GetNumberOfMappedEventPacketsRequest numberOfPacketsRequest = (GetNumberOfMappedEventPacketsRequest)request;
			GetNumberOfMappedEventPacketsRequest.Response response = numberOfPacketsRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				mResult = RESULT_REQUEST_ERROR;
				mPhaseControllerCallback.onPhaseControllerFailed(this);
				mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.FAILED, null);
				return;
			}
			
			mConfiguration.mNumberOfMappedEventPackets = response.numberOfMappedEventPackets;
			
		} else if (request instanceof GetConnectionHeartbeatIntervalRequest) {
			GetConnectionHeartbeatIntervalRequest heartbeatIntervalRequest = (GetConnectionHeartbeatIntervalRequest)request;
			GetConnectionHeartbeatIntervalRequest.Response response = heartbeatIntervalRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				mResult = RESULT_REQUEST_ERROR;
				mPhaseControllerCallback.onPhaseControllerFailed(this);
				mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.FAILED, null);
				return;
			}
			
			mConfiguration.mConnectionHeartbeatInterval = response.connectionHeartbeatInterval;
		}
		
		sendNextRequest();
	}
	
	private Request buildRequest(Class<? extends Request> requestType) {
		Request request = null;
		
		if (requestType.equals(GetNumberOfMappedEventPacketsRequest.class)) {
			GetNumberOfMappedEventPacketsRequest numberOfPacketsRequest = new GetNumberOfMappedEventPacketsRequest();
			numberOfPacketsRequest.buildRequest();
			request = numberOfPacketsRequest;
		} else if (requestType.equals(GetConnectionHeartbeatIntervalRequest.class)) {
			GetConnectionHeartbeatIntervalRequest heartbeatIntervalRequest = new GetConnectionHeartbeatIntervalRequest();
			heartbeatIntervalRequest.buildRequest();
			request = heartbeatIntervalRequest;
		}
		return request;
	}
	
	private void sendNextRequest() {
		Request nextRequest = null;
		
		if (mCurrentRequest == null) {
			nextRequest =  buildRequest(GetNumberOfMappedEventPacketsRequest.class);
		} else if (mCurrentRequest instanceof GetNumberOfMappedEventPacketsRequest) {
			nextRequest =  buildRequest(GetConnectionHeartbeatIntervalRequest.class);
		} else if (mCurrentRequest instanceof GetConnectionHeartbeatIntervalRequest) {
			mResult = RESULT_SUCCESS;
			mPhaseControllerCallback.onPhaseControllerCompleted(this);
			Hashtable<ShineProperty, Object> objects = new Hashtable<>();
			objects.put(ShineProperty.STREAMING_CONFIGURATION, mConfiguration);
			mConfigurationCallback.onConfigCompleted(getActionID(), ShineProfile.ActionResult.SUCCEEDED, objects);
			return;
		}
		
		sendRequest(nextRequest);
	}
	
	@Override
	public String getPhaseName() {
		return "GetStreamingConfigurationPhase";
	}
}
