package com.misfit.ble.shine.controller;

import com.misfit.ble.setting.flashlink.FlashButtonMode;
import com.misfit.ble.setting.lapCounting.LapCountingMode;
import com.misfit.ble.setting.speedo.ActivityType;
import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineConfiguration;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineLapCountingStatus;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.compatibility.FirmwareCompatibility;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.ActivateRequest;
import com.misfit.ble.shine.request.DisplayPairAnimationRequest;
import com.misfit.ble.shine.request.GetActivationStateRequest;
import com.misfit.ble.shine.request.GetActivityPointRequest;
import com.misfit.ble.shine.request.GetActivityTaggingStateRequest;
import com.misfit.ble.shine.request.GetActivityTypeRequest;
import com.misfit.ble.shine.request.GetBatteryRequest;
import com.misfit.ble.shine.request.GetClockStateRequest;
import com.misfit.ble.shine.request.GetConnectionParameterRequest;
import com.misfit.ble.shine.request.GetExtraAdvDataStateRequest;
import com.misfit.ble.shine.request.GetFlashButtonModeRequest;
import com.misfit.ble.shine.request.GetGoalRequest;
import com.misfit.ble.shine.request.GetLapCountingStatusRequest;
import com.misfit.ble.shine.request.GetTimeRequest;
import com.misfit.ble.shine.request.GetTripleTapEnableRequest;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.request.SetActivityPointRequest;
import com.misfit.ble.shine.request.SetActivityTaggingStateRequest;
import com.misfit.ble.shine.request.SetActivityTypeRequest;
import com.misfit.ble.shine.request.SetClockStateRequest;
import com.misfit.ble.shine.request.SetExtraAdvDataStateRequest;
import com.misfit.ble.shine.request.SetFlashButtonModeRequest;
import com.misfit.ble.shine.request.SetGoalRequest;
import com.misfit.ble.shine.request.SetLapCountingLicenseInfoRequest;
import com.misfit.ble.shine.request.SetLapCountingModeRequest;
import com.misfit.ble.shine.request.SetTimeRequest;
import com.misfit.ble.shine.request.SetTripleTapEnableRequest;
import com.misfit.ble.shine.request.StopDisplayingPairAnimationRequest;
import com.misfit.ble.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Quoc-Hung Le on 9/1/15.
 */
public class ShineControllers {

	private static final String TAG = LogUtils.makeTag(ShineControllers.class);

	private PhaseController.PhaseControllerCallback mPhaseControllerCallback;

	public ShineControllers(PhaseController.PhaseControllerCallback phaseControllerCallback) {
		this.mPhaseControllerCallback = phaseControllerCallback;
	}

	public PhaseController activate(final ShineProfile.ConfigurationCallback configurationCallback) {
		ActivateRequest activateRequest = new ActivateRequest();
		activateRequest.buildRequest();

		Request[] requests = {activateRequest};

		return new ControllerBuilder(ActionID.ACTIVATE,
				LogEventItem.EVENT_ACTIVATE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController playAnimation(final ShineProfile.ConfigurationCallback configurationCallback) {
		DisplayPairAnimationRequest displayPairAnimationRequest = new DisplayPairAnimationRequest();
		displayPairAnimationRequest.buildRequest();

		Request[] requests = {displayPairAnimationRequest};

		return new ControllerBuilder(ActionID.ANIMATE,
				LogEventItem.EVENT_PLAY_ANIMATION,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController stopPlayingAnimation(final ShineProfile.ConfigurationCallback configurationCallback) {
		StopDisplayingPairAnimationRequest stopDisplayingPairAnimationRequest = new StopDisplayingPairAnimationRequest();
		stopDisplayingPairAnimationRequest.buildRequest();

		Request[] requests = {stopDisplayingPairAnimationRequest};

		return new ControllerBuilder(ActionID.STOP_ANIMATING,
				LogEventItem.EVENT_STOP_PLAYING_ANIMATION,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController getActivationState(final ShineProfile.ConfigurationCallback configurationCallback) {
		GetActivationStateRequest getActivationStateRequest = new GetActivationStateRequest();
		getActivationStateRequest.buildRequest();

		Request[] requests = {getActivationStateRequest};

		return new ControllerBuilder(ActionID.GET_ACTIVATION_STATE,
				LogEventItem.EVENT_GET_ACTIVATION_STATE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					for (Request request : requests) {
						if (request instanceof GetActivationStateRequest) {
							GetActivationStateRequest.Response response = ((GetActivationStateRequest) request).getResponse();
							Boolean activated = response.activated;
							objects = new Hashtable<>();
							objects.put(ShineProperty.ACTIVATION_STATE, activated);
						}
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}

	public PhaseController getConnectionParameters(final ShineProfile.ConfigurationCallback configurationCallback) {
		GetConnectionParameterRequest getConnectionParameterRequest = new GetConnectionParameterRequest();
		getConnectionParameterRequest.buildRequest();

		Request[] requests = {getConnectionParameterRequest};

		return new ControllerBuilder(ActionID.GET_CONNECTION_PARAMETERS,
				LogEventItem.EVENT_GET_CONNECTION_PARAMETERS,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					for (Request request : requests) {
						if (request instanceof GetConnectionParameterRequest) {
							GetConnectionParameterRequest.Response response = ((GetConnectionParameterRequest) request).getResponse();
							ShineConnectionParameters shineConnectionParameters = new ShineConnectionParameters(response.connectionInterval,
									response.connectionLatency,
									response.supervisionTimeout);

							objects = new Hashtable<>();
							objects.put(ShineProperty.CONNECTION_PARAMETERS, shineConnectionParameters);
						}
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}

	public PhaseController setExtraAdvertisingDataState(boolean advDataState, final ShineProfile.ConfigurationCallback configurationCallback) {
		SetExtraAdvDataStateRequest setExtraAdvDataStateRequest = new SetExtraAdvDataStateRequest();
		setExtraAdvDataStateRequest.buildRequest(advDataState);

		Request[] requests = {setExtraAdvDataStateRequest};

		return new ControllerBuilder(ActionID.SET_EXTRA_ADV_DATA_STATE,
				LogEventItem.EVENT_SET_EXTRA_ADV_DATA_STATE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController getExtraAdvertisingDataState(final ShineProfile.ConfigurationCallback configurationCallback) {
		GetExtraAdvDataStateRequest getExtraAdvDataStateRequest = new GetExtraAdvDataStateRequest();
		getExtraAdvDataStateRequest.buildRequest();

		Request[] requests = {getExtraAdvDataStateRequest};

		return new ControllerBuilder(ActionID.GET_EXTRA_ADV_DATA_STATE,
				LogEventItem.EVENT_GET_EXTRA_ADV_DATA_STATE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					for (Request request : requests) {
						if (request instanceof GetExtraAdvDataStateRequest) {
							GetExtraAdvDataStateRequest.Response response = ((GetExtraAdvDataStateRequest) request).getResponse();
							boolean advDataState = response.advDataState;
							objects = new Hashtable<>();
							objects.put(ShineProperty.EXTRA_ADVERTISING_DATA_STATE, advDataState);
						}
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}

	public PhaseController setFlashButtonMode(FlashButtonMode flashButtonMode, final ShineProfile.ConfigurationCallback configurationCallback) {
		SetFlashButtonModeRequest setFlashButtonModeRequest = new SetFlashButtonModeRequest();
		setFlashButtonModeRequest.buildRequest(true, flashButtonMode);

		Request[] requests = {setFlashButtonModeRequest};

		return new ControllerBuilder(ActionID.SET_FLASH_BUTTON_MODE,
				LogEventItem.EVENT_SET_FLASH_BUTTON_MODE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController getFlashButtonMode(final ShineProfile.ConfigurationCallback configurationCallback) {
		GetFlashButtonModeRequest getFlashButtonModeRequest = new GetFlashButtonModeRequest();
		getFlashButtonModeRequest.buildRequest();

		Request[] requests = {getFlashButtonModeRequest};

		return new ControllerBuilder(ActionID.GET_FLASH_BUTTON_MODE,
				LogEventItem.EVENT_GET_FLASH_BUTTON_MODE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					for (Request request : requests) {
						if (request instanceof GetFlashButtonModeRequest) {
							GetFlashButtonModeRequest.Response response = ((GetFlashButtonModeRequest) request).getResponse();
							FlashButtonMode flashButtonMode = response.flashButtonMode;
							objects = new Hashtable<>();
							objects.put(ShineProperty.FLASH_BUTTON_MODE, flashButtonMode);
						}
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}

    public PhaseController getLapCountingStatus(final ShineProfile.ConfigurationCallback configurationCallback) {
        GetLapCountingStatusRequest getLapCountingStatusRequest = new GetLapCountingStatusRequest();
        getLapCountingStatusRequest.buildRequest();

        Request[] requests = {getLapCountingStatusRequest};

        return new ControllerBuilder(ActionID.GET_LAP_COUNTING_STATUS,
                LogEventItem.EVENT_GET_LAP_COUNTING_STATUS,
                Arrays.asList(requests),
                mPhaseControllerCallback, new ControllerBuilder.Callback() {
            @Override
            public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
                Hashtable<ShineProperty, Object> objects = null;

                if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
                    for (Request request : requests) {
                        if (request instanceof GetLapCountingStatusRequest) {
                            GetLapCountingStatusRequest.Response response = ((GetLapCountingStatusRequest) request).getResponse();
                            ShineLapCountingStatus shineLapCountingStatus = new ShineLapCountingStatus();
                            shineLapCountingStatus.setLicenseStatus(response.licenseStatus);
                            shineLapCountingStatus.setTrialCounter(response.trialCounter);
                            shineLapCountingStatus.setLapCountingMode(response.lapCountingMode);
                            shineLapCountingStatus.setTimeout(response.timeout);

                            objects = new Hashtable<>();
                            objects.put(ShineProperty.LAP_COUNTING_STATUS, shineLapCountingStatus);
                        }
                    }
                }
                configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
            }
        });
    }

    public PhaseController setLapCountingLicenseInfo(byte[] licenseInfo, final ShineProfile.ConfigurationCallback configurationCallback) {
        SetLapCountingLicenseInfoRequest setLapCountingLicenseInfoRequest = new SetLapCountingLicenseInfoRequest();
        setLapCountingLicenseInfoRequest.buildRequest(licenseInfo);

        Request[] requests = {setLapCountingLicenseInfoRequest};

        return new ControllerBuilder(ActionID.SET_LAP_COUNTING_LICENSE_INFO,
                LogEventItem.EVENT_SET_LAP_COUNTING_LICENSE_INFO,
                Arrays.asList(requests),
                mPhaseControllerCallback, new ControllerBuilder.Callback() {
            @Override
            public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
                configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
            }
        });
    }

    public PhaseController setLapCountingMode(LapCountingMode mode, short timeout, final ShineProfile.ConfigurationCallback configurationCallback) {
        SetLapCountingModeRequest setLapCountingModeRequest = new SetLapCountingModeRequest();
        setLapCountingModeRequest.buildRequest(mode, timeout);

        Request[] requests = {setLapCountingModeRequest};

        return new ControllerBuilder(ActionID.SET_LAP_COUNTING_MODE,
                LogEventItem.EVENT_SET_LAP_COUNTING_MODE,
                Arrays.asList(requests),
                mPhaseControllerCallback, new ControllerBuilder.Callback() {
            @Override
            public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
                configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
            }
        });
    }

	public PhaseController setActivityType(ActivityType activityType, final ShineProfile.ConfigurationCallback configurationCallback) {
		SetActivityTypeRequest request = new SetActivityTypeRequest();
		request.buildRequest(activityType);
		Request[] requests = {request};

		return new ControllerBuilder(ActionID.SET_ACTIVITY_TYPE,
				LogEventItem.EVENT_SET_ACTIVITY_TYPE,
				Arrays.asList(requests),
				mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});

	}

	public PhaseController getActivityType(final ShineProfile.ConfigurationCallback configurationCallback) {
		final GetActivityTypeRequest request = new GetActivityTypeRequest();
		request.buildRequest();
		Request[] requests = {request};

		return new ControllerBuilder(ActionID.GET_ACTIVITY_TYPE,
				LogEventItem.EVENT_GET_ACTIVITY_TYPE,
				Arrays.asList(requests),
				mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> hashTable = new Hashtable<>();
				if (ShineProfile.ActionResult.SUCCEEDED == resultCode) {
					for (Request req : requests) {
						if (req instanceof GetActivityTypeRequest) {
							GetActivityTypeRequest.Response response = (GetActivityTypeRequest.Response) req.getResponse();
							hashTable.put(ShineProperty.ACTIVITY_TYPE, response.activityType);
						}
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, hashTable);
			}
		});

	}

	public PhaseController setDeviceConfiguration(String firmwareVersion, String modelNumber, ConfigurationSession session, final ShineProfile.ConfigurationCallback configurationCallback) {
		ConfigurationSession mSyncSession = session;

		List<Request> requests = new ArrayList<>();

		SetTimeRequest setTimeRequest = new SetTimeRequest();
		setTimeRequest.buildRequest(mSyncSession.mTimestamp, mSyncSession.mPartialSecond, mSyncSession.mTimeZoneOffset);
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, setTimeRequest)) {
			requests.add(setTimeRequest);
		}

		SetGoalRequest setGoalRequest = new SetGoalRequest();
		setGoalRequest.buildRequest(mSyncSession.mShineConfiguration.mGoalValue);
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, setGoalRequest)) {
			requests.add(setGoalRequest);
		}

		SetActivityPointRequest setActivityPointRequest = new SetActivityPointRequest();
		setActivityPointRequest.buildRequest(mSyncSession.mShineConfiguration.mActivityPoint);
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, setActivityPointRequest)) {
			requests.add(setActivityPointRequest);
		}

		SetClockStateRequest setClockStateRequest = new SetClockStateRequest();
		setClockStateRequest.buildRequest(mSyncSession.mShineConfiguration.mClockState);
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, setClockStateRequest)) {
			requests.add(setClockStateRequest);
		}

		SetTripleTapEnableRequest setTripleTapEnableRequest = new SetTripleTapEnableRequest();
		setTripleTapEnableRequest.buildRequest((mSyncSession.mShineConfiguration.mTripleTapState > 0) ? true : false);
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, setTripleTapEnableRequest)) {
			requests.add(setTripleTapEnableRequest);
		}

		SetActivityTaggingStateRequest setActivityTaggingStateRequest = new SetActivityTaggingStateRequest();
		setActivityTaggingStateRequest.buildRequest((mSyncSession.mShineConfiguration.mActivityTaggingState > 0) ? true : false);
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, setActivityTaggingStateRequest)) {
			requests.add(setActivityTaggingStateRequest);
		}

		return new ControllerBuilder(ActionID.SET_CONFIGURATION,
				LogEventItem.EVENT_SET_DEVICE_CONFIGURATION,
				requests, mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {

				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public class GetConfigurationSession extends ConfigurationSession {
		public GetConfigurationSession() {
			super();
			mShineConfiguration = new ShineConfiguration();
		}
	}

	public PhaseController getDeviceConfiguration(String firmwareVersion, String modelNumber, final ShineProfile.ConfigurationCallback configurationCallback) {
		List<Request> requests = new ArrayList<>();

		GetTimeRequest getTimeRequest = new GetTimeRequest();
		getTimeRequest.buildRequest();
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, getTimeRequest)) {
			requests.add(getTimeRequest);
		}

		final GetGoalRequest getGoalRequest = new GetGoalRequest();
		getGoalRequest.buildRequest();
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, getGoalRequest)) {
			requests.add(getGoalRequest);
		}

		GetActivityPointRequest getActivityPointRequest = new GetActivityPointRequest();
		getActivityPointRequest.buildRequest();
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, getActivityPointRequest)) {
			requests.add(getActivityPointRequest);
		}

		GetClockStateRequest getClockStateRequest = new GetClockStateRequest();
		getClockStateRequest.buildRequest();
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, getClockStateRequest)) {
			requests.add(getClockStateRequest);
		}

		GetTripleTapEnableRequest getTripleTapEnableRequest = new GetTripleTapEnableRequest();
		getTripleTapEnableRequest.buildRequest();
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, getTripleTapEnableRequest)) {
			requests.add(getTripleTapEnableRequest);
		}

		GetActivityTaggingStateRequest getActivityTaggingStateRequest = new GetActivityTaggingStateRequest();
		getActivityTaggingStateRequest.buildRequest();
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, getActivityTaggingStateRequest)) {
			requests.add(getActivityTaggingStateRequest);
		}

		GetBatteryRequest getBatteryRequest = new GetBatteryRequest();
		getBatteryRequest.buildRequest();
		if (FirmwareCompatibility.isSupportedRequest(firmwareVersion, modelNumber, getBatteryRequest)) {
			requests.add(getBatteryRequest);
		}

		return new ControllerBuilder(ActionID.GET_CONFIGURATION,
				LogEventItem.EVENT_GET_DEVICE_CONFIGURATION,
				requests, mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					ConfigurationSession session = new GetConfigurationSession();

					for (Request request : requests) {
						if (request instanceof GetTimeRequest) {
							GetTimeRequest.Response response = ((GetTimeRequest) request).getResponse();
							session.mTimestamp = response.timestamp;
							session.mPartialSecond = response.partialSeconds;
							session.mTimeZoneOffset = response.timezoneOffsetInMinutes;
						} else if (request instanceof GetGoalRequest) {
							GetGoalRequest.Response response = ((GetGoalRequest) request).getResponse();
							session.mShineConfiguration.mGoalValue = response.goal;
						} else if (request instanceof GetActivityPointRequest) {
							GetActivityPointRequest.Response response = ((GetActivityPointRequest) request).getResponse();
							session.mShineConfiguration.mActivityPoint = response.activityPoint;
						} else if (request instanceof GetClockStateRequest) {
							GetClockStateRequest.Response response = ((GetClockStateRequest) request).getResponse();
							session.mShineConfiguration.mClockState = response.clockState;
						} else if (request instanceof GetTripleTapEnableRequest) {
							GetTripleTapEnableRequest.Response response = ((GetTripleTapEnableRequest) request).getResponse();
							session.mShineConfiguration.mTripleTapState = (byte) (response.tripleTapEnable ? 1 : 0);
						} else if (request instanceof GetActivityTaggingStateRequest) {
							GetActivityTaggingStateRequest.Response response = ((GetActivityTaggingStateRequest) request).getResponse();
							session.mShineConfiguration.mActivityTaggingState = (byte) (response.activityTaggingState ? 1 : 0);
						} else if (request instanceof GetBatteryRequest) {
							GetBatteryRequest.Response response = ((GetBatteryRequest) request).getResponse();
							session.mShineConfiguration.mBatteryLevel = response.batteryLevel;
						}
					}
					objects = new Hashtable<>();
					objects.put(ShineProperty.SHINE_CONFIGURATION_SESSION, session);
				}

				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}
}
