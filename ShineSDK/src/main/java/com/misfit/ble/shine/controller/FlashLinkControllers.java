package com.misfit.ble.shine.controller;

import com.misfit.ble.setting.flashlink.CustomModeEnum;
import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.SetCustomModeRequest;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.request.UnmapAllEventsRequest;
import com.misfit.ble.shine.request.UnmapEventRequest;
import com.misfit.ble.util.LogUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Quoc-Hung Le on 9/14/15.
 */
public class FlashLinkControllers {

	private static final String TAG = LogUtils.makeTag(FlashLinkControllers.class);

	private PhaseController.PhaseControllerCallback mPhaseControllerCallback;

	public FlashLinkControllers(PhaseController.PhaseControllerCallback phaseControllerCallback) {
		mPhaseControllerCallback = phaseControllerCallback;
	}

	public PhaseController setCustomMode(CustomModeEnum.ActionType actionType, CustomModeEnum.MemEventNumber eventNumber,
										 CustomModeEnum.AnimNumber animNumber, CustomModeEnum.KeyCode keyCode,
										 boolean releaseEnable, final ShineProfile.ConfigurationCallback configurationCallback) {
		SetCustomModeRequest customModeRequest = new SetCustomModeRequest();
		customModeRequest.buildRequest(actionType, eventNumber, animNumber, keyCode, releaseEnable);

		Request[] requests = {customModeRequest};

		return new ControllerBuilder(ActionID.SET_CUSTOM_MODE,
				LogEventItem.EVENT_SET_CUSTOM_MODE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController unmapAllEvents(final ShineProfile.ConfigurationCallback configurationCallback) {
		UnmapAllEventsRequest unmapAllEventsRequest = new UnmapAllEventsRequest();
		unmapAllEventsRequest.buildRequest();

		Request[] requests = {unmapAllEventsRequest};

		return new ControllerBuilder(ActionID.UNMAP_ALL_EVENTS,
				LogEventItem.EVENT_UNMAP_ALL_EVENTS,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController unmapEvent(CustomModeEnum.MemEventNumber eventNumber, final ShineProfile.ConfigurationCallback configurationCallback) {
		UnmapEventRequest unmapEventRequest = new UnmapEventRequest();
		unmapEventRequest.buildRequest(eventNumber);

		Request[] requests = {unmapEventRequest};

		return new ControllerBuilder(ActionID.UNMAP_EVENT,
				LogEventItem.EVENT_UNMAP_EVENT,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}
}
