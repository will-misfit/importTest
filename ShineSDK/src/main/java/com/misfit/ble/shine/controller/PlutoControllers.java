package com.misfit.ble.shine.controller;

import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.setting.pluto.PlutoSequence;
import com.misfit.ble.setting.pluto.SpecifiedAnimationSetting;
import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProperty;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.ClearAllAlarmsRequest;
import com.misfit.ble.shine.request.DisableAllCallTextNotificationsRequest;
import com.misfit.ble.shine.request.GetAlarmParametersRequest;
import com.misfit.ble.shine.request.GetCallTextNotificationRequest;
import com.misfit.ble.shine.request.GetCallTextNotificationWindowsRequest;
import com.misfit.ble.shine.request.GetGoalHitNotificationRequest;
import com.misfit.ble.shine.request.GetInactivityNudgeRequest;
import com.misfit.ble.shine.request.GetSingleAlarmTimeRequest;
import com.misfit.ble.shine.request.PlayLEDAnimationRequest;
import com.misfit.ble.shine.request.PlaySoundRequest;
import com.misfit.ble.shine.request.PlayVibrationRequest;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.request.SendCallNotificationRequest;
import com.misfit.ble.shine.request.SendTextNotificationRequest;
import com.misfit.ble.shine.request.SetAlarmParametersRequest;
import com.misfit.ble.shine.request.SetCallTextNotificationWindowsRequest;
import com.misfit.ble.shine.request.SetCallTextNotificationsRequest;
import com.misfit.ble.shine.request.SetGoalHitNotificationRequest;
import com.misfit.ble.shine.request.SetInactivityNudgeRequest;
import com.misfit.ble.shine.request.SetSingleAlarmTimeRequest;
import com.misfit.ble.shine.request.StartSpecifiedAnimationRequest;
import com.misfit.ble.shine.request.StopNotificationRequest;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Quoc-Hung Le on 8/31/15.
 */
public class PlutoControllers {

	private PhaseController.PhaseControllerCallback mPhaseControllerCallback;

	public PlutoControllers(PhaseController.PhaseControllerCallback phaseControllerCallback) {
		mPhaseControllerCallback = phaseControllerCallback;
	}

	public PhaseController setInactivityNudge(InactivityNudgeSettings inactivityNudgeSettings, final ShineProfile.ConfigurationCallback configurationCallback) {
		SetInactivityNudgeRequest setInactivityNudgeRequest = new SetInactivityNudgeRequest();
		setInactivityNudgeRequest.buildRequest(inactivityNudgeSettings);

		Request[] requests = {setInactivityNudgeRequest};

		return new ControllerBuilder(ActionID.SET_INACTIVITY_NUDGE,
				LogEventItem.EVENT_SET_INACTIVITY_NUDGE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController getInactivityNudge(final ShineProfile.ConfigurationCallback configurationCallback) {
		GetInactivityNudgeRequest getInactivityNudgeRequest = new GetInactivityNudgeRequest();
		getInactivityNudgeRequest.buildRequest();

		Request[] requests = {getInactivityNudgeRequest};

		return new ControllerBuilder(ActionID.GET_INACTIVITY_NUDGE,
				LogEventItem.EVENT_GET_INACTIVITY_NUDGE,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					for (Request request : requests) {
						if (request instanceof GetInactivityNudgeRequest) {
							GetInactivityNudgeRequest.Response response = ((GetInactivityNudgeRequest) request).getResponse();
							InactivityNudgeSettings inactivityNudgeSettings = new InactivityNudgeSettings(response.mEnabled,
									response.mLEDSequenceID, response.mVibeSequenceID, response.mSoundSequenceID,
									response.mStartHour, response.mStartMinute, response.mEndHour, response.mEndMinute,
									response.mRepeatIntervalMinutes);

							objects = new Hashtable<>();
							objects.put(ShineProperty.INACTIVITY_NUDGE_SETTINGS, inactivityNudgeSettings);
						}
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}

	public PhaseController setSingleAlarm(AlarmSettings alarmSettings, final ShineProfile.ConfigurationCallback configurationCallback) {
		SetSingleAlarmTimeRequest setSingleAlarmTimeRequest = new SetSingleAlarmTimeRequest();
		setSingleAlarmTimeRequest.buildRequest(alarmSettings);

		SetAlarmParametersRequest setAlarmParametersRequest = new SetAlarmParametersRequest();
		setAlarmParametersRequest.buildRequest(alarmSettings);

		Request[] requests = {setSingleAlarmTimeRequest, setAlarmParametersRequest};

		return new ControllerBuilder(ActionID.SET_SINGLE_ALARM_TIME,
				LogEventItem.EVENT_SET_SINGLE_ALARM_TIME,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController getSingleAlarm(byte alarmDay, final ShineProfile.ConfigurationCallback configurationCallback) {
		GetSingleAlarmTimeRequest getSingleAlarmTimeRequest = new GetSingleAlarmTimeRequest();
		getSingleAlarmTimeRequest.buildRequest(alarmDay);

		GetAlarmParametersRequest getAlarmParametersRequest = new GetAlarmParametersRequest();
		getAlarmParametersRequest.buildRequest();

		Request[] requests = {getSingleAlarmTimeRequest, getAlarmParametersRequest};

		return new ControllerBuilder(ActionID.GET_SINGLE_ALARM_TIME,
				LogEventItem.EVENT_GET_SINGLE_ALARM_TIME,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					AlarmSettings alarmSettings = new AlarmSettings();

					for (Request request : requests) {
						if (request instanceof GetSingleAlarmTimeRequest) {
							GetSingleAlarmTimeRequest.Response response = ((GetSingleAlarmTimeRequest) request).getResponse();
							alarmSettings.setAlarmDay(response.mAlarmDay);
							alarmSettings.setAlarmType(response.mAlarmType);
							alarmSettings.setAlarmHour(response.mAlarmHour);
							alarmSettings.setAlarmMinute(response.mAlarmMinute);
						} else if (request instanceof GetAlarmParametersRequest) {
							GetAlarmParametersRequest.Response response = ((GetAlarmParametersRequest) request).getResponse();
							alarmSettings.setWindowInMinute(response.mWindowInMinute);
							alarmSettings.setLEDSequence(response.mLEDSequence);
							alarmSettings.setVibeSequence(response.mVibeSequence);
							alarmSettings.setSoundSequence(response.mSoundSequence);
							alarmSettings.setSnoozeTimeInMinute(response.mSnoozeTimeInMinute);
							alarmSettings.setAlarmDuration(response.mAlarmDuration);
						}
					}

					if(!alarmSettings.isDefaultValue()) {
						objects = new Hashtable<>();
						objects.put(ShineProperty.ALARM_SETTINGS, alarmSettings);
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}

	public PhaseController clearAllAlarms(final ShineProfile.ConfigurationCallback configurationCallback) {
		ClearAllAlarmsRequest clearAllAlarmsRequest = new ClearAllAlarmsRequest();
		clearAllAlarmsRequest.buildRequest();

		Request[] requests = {clearAllAlarmsRequest};

		return new ControllerBuilder(ActionID.CLEAR_ALL_ALARMS,
				LogEventItem.EVENT_CLEAR_ALL_ALARMS,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController setGoalReachNotification(GoalHitNotificationSettings goalHitNotificationSettings, final ShineProfile.ConfigurationCallback configurationCallback) {
		SetGoalHitNotificationRequest setGoalHitNotificationRequest = new SetGoalHitNotificationRequest();
		setGoalHitNotificationRequest.buildRequest(goalHitNotificationSettings);

		Request[] requests = {setGoalHitNotificationRequest};

		return new ControllerBuilder(ActionID.SET_GOAL_HIT_NOTIFICATION,
				LogEventItem.EVENT_SET_GOAL_HIT_NOTIFICATION,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController getGoalReachNotification(final ShineProfile.ConfigurationCallback configurationCallback) {
		GetGoalHitNotificationRequest getGoalHitNotificationRequest = new GetGoalHitNotificationRequest();
		getGoalHitNotificationRequest.buildRequest();

		Request[] requests = {getGoalHitNotificationRequest};

		return new ControllerBuilder(ActionID.GET_GOAL_HIT_NOTIFICATION,
				LogEventItem.EVENT_GET_GOAL_HIT_NOTIFICATION,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					for (Request request : requests) {
						if (request instanceof GetGoalHitNotificationRequest) {
							GetGoalHitNotificationRequest.Response response = ((GetGoalHitNotificationRequest) request).getResponse();
							GoalHitNotificationSettings goalHitNotificationSettings = new GoalHitNotificationSettings(response.mEnabled,
									response.mLEDSequenceID, response.mVibeSequenceID, response.mSoundSequenceID,
									response.mStartHour, response.mStartMinute, response.mEndHour, response.mEndMinute);

							objects = new Hashtable<>();
							objects.put(ShineProperty.GOAL_HIT_NOTIFICATION_SETTINGS, goalHitNotificationSettings);
						}
					}
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}

	public PhaseController setCallTextNotifications(NotificationsSettings notificationsSettings, final ShineProfile.ConfigurationCallback configurationCallback) {
		SetCallTextNotificationsRequest setCallTextNotificationsRequest = new SetCallTextNotificationsRequest();
		setCallTextNotificationsRequest.buildRequest(notificationsSettings);

		SetCallTextNotificationWindowsRequest setCallTextNotificationWindowsRequest = new SetCallTextNotificationWindowsRequest();
		setCallTextNotificationWindowsRequest.buildRequest(notificationsSettings);

		Request[] requests = {setCallTextNotificationsRequest, setCallTextNotificationWindowsRequest};

		return new ControllerBuilder(ActionID.SET_CALL_TEXT_NOTIFICATIONS,
				LogEventItem.EVENT_SET_CALL_TEXT_NOTIFICATIONS,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController getCallTextNotifications(final ShineProfile.ConfigurationCallback configurationCallback) {
		GetCallTextNotificationRequest getCallTextNotificationRequest = new GetCallTextNotificationRequest();
		getCallTextNotificationRequest.buildRequest();

		GetCallTextNotificationWindowsRequest getCallTextNotificationWindowsRequest = new GetCallTextNotificationWindowsRequest();
		getCallTextNotificationWindowsRequest.buildRequest();

		Request[] requests = {getCallTextNotificationRequest, getCallTextNotificationWindowsRequest};

		return new ControllerBuilder(ActionID.GET_CALL_TEXT_NOTIFICATIONS,
				LogEventItem.EVENT_GET_CALL_TEXT_NOTIFICATIONS,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				Hashtable<ShineProperty, Object> objects = null;

				if (resultCode == ShineProfile.ActionResult.SUCCEEDED) {
					NotificationsSettings notificationsSettings = new NotificationsSettings();
					for (Request request : requests) {
						if (request instanceof GetCallTextNotificationRequest) {
							GetCallTextNotificationRequest.Response response = ((GetCallTextNotificationRequest) request).getResponse();

							notificationsSettings.setCallLEDSequence(response.callLedSequence);
							notificationsSettings.setCallVibeSequence(response.callVibeSequence);
							notificationsSettings.setCallSoundSequence(response.callSoundSequence);
							notificationsSettings.setTextLEDSequence(response.textLedSequence);
							notificationsSettings.setTextVibeSequence(response.textVibeSequence);
							notificationsSettings.setTextSoundSequence(response.textSoundSequence);
						} else if(request instanceof GetCallTextNotificationWindowsRequest) {
							GetCallTextNotificationWindowsRequest.Response response = ((GetCallTextNotificationWindowsRequest) request).getResponse();

							notificationsSettings.setStartHour(response.mStartHour);
							notificationsSettings.setStartMinute(response.mStartMinute);
							notificationsSettings.setEndHour(response.mEndHour);
							notificationsSettings.setEndMinute(response.mEndMinute);
						}
					}

					objects = new Hashtable<>();
					objects.put(ShineProperty.CALL_TEXT_NOTIFICATION_SETTINGS, notificationsSettings);
				}
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, objects);
			}
		});
	}

	public PhaseController disableAllNotifications(final ShineProfile.ConfigurationCallback configurationCallback) {
		DisableAllCallTextNotificationsRequest disableAllCallTextNotificationsRequest = new DisableAllCallTextNotificationsRequest();
		disableAllCallTextNotificationsRequest.buildRequest();

		Request[] requests = {disableAllCallTextNotificationsRequest};

		return new ControllerBuilder(ActionID.DISABLE_ALL_CALL_TEXT_NOTIFICATIONS,
				LogEventItem.EVENT_DISABLE_ALL_CALL_TEXT_NOTIFICATIONS,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController sendCallNotification(final ShineProfile.ConfigurationCallback configurationCallback) {
		SendCallNotificationRequest sendCallNotificationRequest = new SendCallNotificationRequest();
		sendCallNotificationRequest.buildRequest();

		Request[] requests = {sendCallNotificationRequest};
 
		return new ControllerBuilder(ActionID.SEND_CALL_NOTIFICATION,
				LogEventItem.EVENT_SEND_CALL_NOTIFICATION,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController sendTextNotification(final ShineProfile.ConfigurationCallback configurationCallback) {
		SendTextNotificationRequest sendTextNotificationRequest = new SendTextNotificationRequest();
		sendTextNotificationRequest.buildRequest();

		Request[] requests = {sendTextNotificationRequest};

		return new ControllerBuilder(ActionID.SEND_TEXT_NOTIFICATION,
				LogEventItem.EVENT_SEND_TEXT_NOTIFICATION,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController stopNotification(final ShineProfile.ConfigurationCallback configurationCallback) {
		StopNotificationRequest stopNotificationRequest = new StopNotificationRequest();
		stopNotificationRequest.buildRequest();

		Request[] requests = {stopNotificationRequest};

		return new ControllerBuilder(ActionID.STOP_NOTIFICATION,
			LogEventItem.EVENT_STOP_NOTIFICATION,
			Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController playLedAnimation(PlutoSequence.LED ledAnimationID, short numOfRepeats, int millisBetweenRepeats, final ShineProfile.ConfigurationCallback configurationCallback) {
		PlayLEDAnimationRequest playLEDAnimationRequest = new PlayLEDAnimationRequest();
		playLEDAnimationRequest.buildRequest(ledAnimationID, numOfRepeats, millisBetweenRepeats);

		Request[] requests = {playLEDAnimationRequest};

		return new ControllerBuilder(ActionID.PLAY_LED_ANIMATION,
				LogEventItem.EVENT_PLAY_LED_ANIMATION,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController playVibration(PlutoSequence.Vibe vibeSequence, short numOfRepeats, int millisBetweenRepeats, final ShineProfile.ConfigurationCallback configurationCallback) {
		PlayVibrationRequest playVibrationRequest = new PlayVibrationRequest();
		playVibrationRequest.buildRequest(vibeSequence, numOfRepeats, millisBetweenRepeats);

		Request[] requests = {playVibrationRequest};

		return new ControllerBuilder(ActionID.PLAY_VIBRATION,
				LogEventItem.EVENT_PLAY_VIBRATION,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController playSound(PlutoSequence.Sound soundSequence, short numOfRepeats, int millisBetweenRepeats, final ShineProfile.ConfigurationCallback configurationCallback) {
		PlaySoundRequest playSoundRequest = new PlaySoundRequest();
		playSoundRequest.buildRequest(soundSequence, numOfRepeats, millisBetweenRepeats);

		Request[] requests = {playSoundRequest};

		return new ControllerBuilder(ActionID.PLAY_SOUND,
				LogEventItem.EVENT_PLAY_SOUND,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}

	public PhaseController startSpecifiedAnimation(SpecifiedAnimationSetting specifiedAnimationSetting, final ShineProfile.ConfigurationCallback configurationCallback) {
		StartSpecifiedAnimationRequest request = new StartSpecifiedAnimationRequest();
		request.buildRequest(specifiedAnimationSetting);

		Request[] requests = {request};

		return new ControllerBuilder(ActionID.START_SPECIFIED_ANIMATION,
				LogEventItem.EVENT_START_SPECIFIED_ANIMATION,
				Arrays.asList(requests), mPhaseControllerCallback, new ControllerBuilder.Callback() {
			@Override
			public void onCompleted(PhaseController phaseController, List<Request> requests, ShineProfile.ActionResult resultCode) {
				configurationCallback.onConfigCompleted(phaseController.getActionID(), resultCode, null);
			}
		});
	}
}
