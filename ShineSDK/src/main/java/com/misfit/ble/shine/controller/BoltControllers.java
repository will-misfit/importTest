package com.misfit.ble.shine.controller;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.AddGroupIdRequest;
import com.misfit.ble.shine.request.GetGroupIdRequest;
import com.misfit.ble.shine.request.GetPassCodeRequest;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.request.SetPassCodeRequest;
import com.misfit.ble.util.LogUtils;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Quoc-Hung Le on 9/15/15.
 */
public class BoltControllers {
	private static final String TAG = LogUtils.makeTag(FlashLinkControllers.class);

	private PhaseController.PhaseControllerCallback mPhaseControllerCallback;

	public BoltControllers(PhaseController.PhaseControllerCallback phaseControllerCallback) {
		mPhaseControllerCallback = phaseControllerCallback;
	}

	public PhaseController addGroupId(short groupId, final ShineProfile.ConfigurationCallback configurationCallback) {
		AddGroupIdRequest addGroupIDRequest = new AddGroupIdRequest();
		addGroupIDRequest.buildRequest(groupId);

		Request[] requests = {addGroupIDRequest};

		return new ControllerBuilder(ActionID.ADD_GROUP_ID,
				LogEventItem.EVENT_ADD_GROUP_ID,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController getGroupId(final ShineProfile.ConfigurationCallback configurationCallback) {
		GetGroupIdRequest getGroupIdRequest = new GetGroupIdRequest();
		getGroupIdRequest.buildRequest();

		Request[] requests = {getGroupIdRequest};

		return new ControllerBuilder(ActionID.GET_GROUP_ID,
				LogEventItem.EVENT_GET_GROUP_ID,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					for (Request request : requests) {
						if (request instanceof GetGroupIdRequest) {
							GetGroupIdRequest.Response mResponse = ((GetGroupIdRequest) request).getResponse();

							objects = new Hashtable<>();
							objects.put(ShineProperty.GROUP_ID, mResponse.mGroupId);
						}
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}

	public PhaseController setPassCode(byte[] passcode, final ShineProfile.ConfigurationCallback configurationCallback) {
		SetPassCodeRequest setPassCodeRequest = new SetPassCodeRequest();
		setPassCodeRequest.buildRequest(passcode);

		Request[] requests = {setPassCodeRequest};

		return new ControllerBuilder(ActionID.SET_PASSCODE,
				LogEventItem.EVENT_SET_PASSCODE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController getPassCode(final ShineProfile.ConfigurationCallback configurationCallback) {
		GetPassCodeRequest getPassCodeRequest = new GetPassCodeRequest();
		getPassCodeRequest.buildRequest();

		Request[] requests = {getPassCodeRequest};

		return new ControllerBuilder(ActionID.GET_PASSCODE,
				LogEventItem.EVENT_GET_PASSCODE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					for (Request request : requests) {
						if (request instanceof GetPassCodeRequest) {
							GetPassCodeRequest.Response mResponse = ((GetPassCodeRequest) request).getResponse();
							objects = new Hashtable<>();
							objects.put(ShineProperty.PASSCODE, mResponse.mPasscode);
						}
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}
}
