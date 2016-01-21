package com.misfit.ble.sample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.misfit.ble.setting.pluto.AlarmSettings;
import com.misfit.ble.setting.pluto.GoalHitNotificationSettings;
import com.misfit.ble.setting.pluto.InactivityNudgeSettings;
import com.misfit.ble.setting.pluto.NotificationsSettings;
import com.misfit.ble.setting.pluto.PlutoSequence;


/**
 * Created by Quoc-Hung Le on 8/24/15.
 */
public class PlutoActivity extends BaseActivity implements View.OnClickListener {

	private static final String TAG = "PlutoActivity";

	private View mViewContainCheckBox = null;

	private Switch mSwitch;
	private boolean on = false;

	private EditText mEditText1;
	private EditText mEditText2;
	private EditText mEditText3;
	private EditText mEditText4;
	private EditText mEditText5;
	private EditText mEditText6;
	private EditText mEditText7;
	private EditText mEditText8;

	private CheckBox mCheckbox;
	private Button mActionButton;

	private Button mPlutoInterruptButton;

	private Button mPlutoToShineButton;

	private TextView mPlutoApiInfoTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pluto_layout);

		mSwitch = (Switch) findViewById(R.id.switchEnable);
		mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				on = isChecked;
			}
		});

		mEditText1 = (EditText) findViewById(R.id.editText1);
		mEditText2 = (EditText) findViewById(R.id.editText2);
		mEditText3 = (EditText) findViewById(R.id.editText3);
		mEditText4 = (EditText) findViewById(R.id.editText4);
		mEditText5 = (EditText) findViewById(R.id.editText5);
		mEditText6 = (EditText) findViewById(R.id.editText6);
		mEditText7 = (EditText) findViewById(R.id.editText7);
		mEditText8 = (EditText) findViewById(R.id.editText8);

		mCheckbox = (CheckBox) findViewById(R.id.cbPlutoGetSet);

		mActionButton = (Button) findViewById(R.id.btnAction);
		mActionButton.setOnClickListener(this);

		mPlutoInterruptButton = (Button) findViewById(R.id.btnPlutoInterrupt);
		mPlutoInterruptButton.setOnClickListener(this);

		mPlutoApiInfoTextView = (TextView) findViewById(R.id.txtPlutoResult);
		mPlutoApiInfoTextView.setMovementMethod(new ScrollingMovementMethod());

		mPlutoToShineButton = (Button) findViewById(R.id.btnPlutoToShine);
		mPlutoToShineButton.setOnClickListener(this);
	}

	public void onRadioButtonClicked(View view) {
		// Get Checkbox View
		mViewContainCheckBox = view;

		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
			case R.id.radioInActNudge:
				if (checked) {
					mSwitch.setEnabled(true);
					mCheckbox.setEnabled(true);
					enableDisableTextView(true, true, true, true,
							true, true, true, true,
							"led", "vibe", "sound", "start Hour",
							"start Min", "end Hour", "end Min", "repeat Min");
				}
				break;
			case R.id.radioAlarm:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(true);
					enableDisableTextView(true, true, true, true,
							true, true, true, true,
							"hour", "minute", "windowMin", "led",
							"vibe", "sound", "snoozeMin", "duration");
				}
				break;
			case R.id.radioClearAlarm:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(false);
					enableDisableTextView(false, false, false, false,
							false, false, false, false,
							"", "", "", "",
							"", "", "", "");
				}
				break;
			case R.id.radioGoalHit:
				if (checked) {
					mSwitch.setEnabled(true);
					mCheckbox.setEnabled(true);
					enableDisableTextView(true, true, true, true,
							true, true, true, false,
							"led", "vibe", "sound", "start Hour",
							"start Min", "end Hour", "end Min", "");
				}
				break;
			case R.id.radioCallTextNoti:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(true);
					enableDisableTextView(true, true, true, true,
							true, true, true, false,
							"call led", "call vibe", "call sound", "text led",
							"text vibe", "text sound", "startHour,startMin,endHour,endMin", "");
				}
				break;
			case R.id.radioDisableNoti:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(false);
					enableDisableTextView(false, false, false, false,
							false, false, false, false,
							"", "", "", "",
							"", "", "", "");
				}
				break;
			case R.id.radioSendCallNoti:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(false);
					enableDisableTextView(false, false, false, false,
							false, false, false, false,
							"", "", "", "",
							"", "", "", "");
				}
				break;
			case R.id.radioSendTextNoti:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(false);
					enableDisableTextView(false, false, false, false,
							false, false, false, false,
							"", "", "", "",
							"", "", "", "");
				}
				break;
			case R.id.radioStopNoti:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(false);
					enableDisableTextView(false, false, false, false,
							false, false, false, false,
							"", "", "", "",
							"", "", "", "");
				}
				break;
			case R.id.radioLed:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(false);
					enableDisableTextView(true, true, true, false,
							false, false, false, false,
							"led sequence", "nRepeat", "repeatInterval", "",
							"", "", "", "");
				}
				break;
			case R.id.radioVibe:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(false);
					enableDisableTextView(true, true, true, false,
							false, false, false, false,
							"vibe sequence", "nRepeat", "repeatInterval", "",
							"", "", "", "");
				}
				break;
			case R.id.radioSound:
				if (checked) {
					mSwitch.setEnabled(false);
					mCheckbox.setEnabled(false);
					enableDisableTextView(true, true, true, false,
							false, false, false, false,
							"sound sequence", "nRepeat", "repeatInterval", "",
							"", "", "", "");
				}
				break;
		}

	}

	private void enableDisableTextView(boolean tv1, boolean tv2, boolean tv3, boolean tv4,
									   boolean tv5, boolean tv6, boolean tv7, boolean tv8,
									   String hint1, String hint2, String hint3, String hint4,
									   String hint5, String hint6, String hint7, String hint8) {
		mEditText1.setText("");
		mEditText1.setEnabled(tv1);
		mEditText1.setHint(hint1);

		mEditText2.setText("");
		mEditText2.setEnabled(tv2);
		mEditText2.setHint(hint2);

		mEditText3.setText("");
		mEditText3.setEnabled(tv3);
		mEditText3.setHint(hint3);

		mEditText4.setText("");
		mEditText4.setEnabled(tv4);
		mEditText4.setHint(hint4);

		mEditText5.setText("");
		mEditText5.setEnabled(tv5);
		mEditText5.setHint(hint5);

		mEditText6.setText("");
		mEditText6.setEnabled(tv6);
		mEditText6.setHint(hint6);

		mEditText7.setText("");
		mEditText7.setEnabled(tv7);
		mEditText7.setHint(hint7);

		mEditText8.setText("");
		mEditText8.setEnabled(tv8);
		mEditText8.setHint(hint8);
	}

	@Override
	protected void setUiState() {
		super.setUiState();

		// Pluto
		mActionButton.setEnabled(isReady);
		mPlutoInterruptButton.setEnabled(isBusy);

		mPlutoApiInfoTextView.setText(mMessage);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnAction:
				if (mViewContainCheckBox != null) {
					conductAction(mViewContainCheckBox);
				}
				break;
			case R.id.btnPlutoInterrupt:
				stopCurrentOperation();
				break;
			case R.id.btnPlutoToShine:
				Intent shineIntent = new Intent(PlutoActivity.this, ShineActivity.class);
				startActivity(shineIntent);
				finish();
				break;
			case R.id.btnPlutoToBolt:
				Intent boltIntent = new Intent(PlutoActivity.this, BoltActivity.class);
				startActivity(boltIntent);
				finish();
				break;
		}
	}

	private void conductAction(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
			case R.id.radioInActNudge:
				if (checked) {
					if(mCheckbox.isChecked()) {
						if(checkEmptyForAllTextView()) {
							Toast.makeText(PlutoActivity.this, "Some fields are empty, please recheck", Toast.LENGTH_SHORT).show();
							return;
						}

						setMessage("SET INACTIVITY NUDGE");

						PlutoSequence.LED led = new PlutoSequence.LED(Short.parseShort(mEditText1.getText().toString()));
						PlutoSequence.Vibe vibe = new PlutoSequence.Vibe(Short.parseShort(mEditText2.getText().toString()));
						PlutoSequence.Sound sound = new PlutoSequence.Sound(Short.parseShort(mEditText3.getText().toString()));
						short startHour = Short.parseShort(mEditText4.getText().toString());
						short startMin = Short.parseShort(mEditText5.getText().toString());
						short endHour = Short.parseShort(mEditText6.getText().toString());
						short endMin = Short.parseShort(mEditText7.getText().toString());
						short repeatMin = Short.parseShort(mEditText8.getText().toString());

						InactivityNudgeSettings inactivityNudgeSettings = new InactivityNudgeSettings(on, led, vibe, sound,
								startHour, startMin, endHour, endMin, repeatMin);

						mService.startSettingInactivityNudge(inactivityNudgeSettings);
					} else {
						setMessage("GET INACTIVITY NUDGE");
						mService.startGettingInactivityNudge();
					}
				}
				break;
			case R.id.radioAlarm:
				if (checked) {
					if(mCheckbox.isChecked()) {
						if(checkEmptyForAllTextView()) {
							Toast.makeText(PlutoActivity.this, "Some fields are empty, please recheck", Toast.LENGTH_SHORT).show();
							return;
						}

						setMessage("SET ALARM");

						byte alarmDay = AlarmSettings.ALL_DAYS;
						byte alarmOperation = AlarmSettings.SET_ALARM;
						byte alarmType = AlarmSettings.REPEATED;
						short alarmHour = Short.parseShort(mEditText1.getText().toString());
						short alarmMinute = Short.parseShort(mEditText2.getText().toString());
						short smartAlarm = Short.parseShort(mEditText3.getText().toString());
						PlutoSequence.LED led = new PlutoSequence.LED(Short.parseShort(mEditText4.getText().toString()));
						PlutoSequence.Vibe vibe = new PlutoSequence.Vibe(Short.parseShort(mEditText5.getText().toString()));
						PlutoSequence.Sound sound = new PlutoSequence.Sound(Short.parseShort(mEditText6.getText().toString()));
						short minPerSnooze = Short.parseShort(mEditText7.getText().toString());
						short alarmDuration = Short.parseShort(mEditText8.getText().toString());

						AlarmSettings alarmSettings = new AlarmSettings(alarmDay, alarmOperation, alarmType,
								alarmHour, alarmMinute, smartAlarm, led, vibe, sound, minPerSnooze, alarmDuration);

						mService.startSettingAlarm(alarmSettings);
					} else {
						setMessage("GET ALARM");
						mService.startGettingAlarm();
					}
				}
				break;
			case R.id.radioClearAlarm:
				if (checked) {
					setMessage("CLEAR ALL ALARMS");
					mService.startClearingAllAlarms();
				}
				break;
			case R.id.radioGoalHit:
				if (checked) {
					if(mCheckbox.isChecked()) {
						if(checkEmptyForAllTextView()) {
							Toast.makeText(PlutoActivity.this, "Some fields are empty, please recheck", Toast.LENGTH_SHORT).show();
							return;
						}

						setMessage("SET GOAL HIT NOTIFICATION");

						PlutoSequence.LED led = new PlutoSequence.LED(Short.parseShort(mEditText1.getText().toString()));
						PlutoSequence.Vibe vibe = new PlutoSequence.Vibe(Short.parseShort(mEditText2.getText().toString()));
						PlutoSequence.Sound sound = new PlutoSequence.Sound(Short.parseShort(mEditText3.getText().toString()));
						short startHour = Short.parseShort(mEditText4.getText().toString());
						short startMin = Short.parseShort(mEditText5.getText().toString());
						short endHour = Short.parseShort(mEditText6.getText().toString());
						short endMin = Short.parseShort(mEditText7.getText().toString());

						GoalHitNotificationSettings goalHitNotificationSettings = new GoalHitNotificationSettings(on, led, vibe, sound,
								startHour, startMin, endHour, endMin);

						mService.startSettingGoalHitNotification(goalHitNotificationSettings);
					} else {
						setMessage("GET GOAL HIT NOTIFICATION");
						mService.startGettingGoalHitNotification();
					}
				}
				break;
			case R.id.radioCallTextNoti:
				if (checked) {
					if(mCheckbox.isChecked()) {
						String strNotiWinParam = mEditText7.getText().toString();

						if(checkEmptyForAllTextView() || strNotiWinParam == null) {
							Toast.makeText(PlutoActivity.this, "Some fields are empty, please recheck", Toast.LENGTH_SHORT).show();
							return;
						}

						String[] params = strNotiWinParam.split(",");
						if(params.length != 4) {
							Toast.makeText(PlutoActivity.this, "Wrong format", Toast.LENGTH_SHORT).show();
							return;
						}

						setMessage("SET CALL TEXT NOTIFICATIONS");

						PlutoSequence.LED callLED = new PlutoSequence.LED(Short.parseShort(mEditText1.getText().toString()));
						PlutoSequence.Vibe callVibe = new PlutoSequence.Vibe(Short.parseShort(mEditText2.getText().toString()));
						PlutoSequence.Sound callSound = new PlutoSequence.Sound(Short.parseShort(mEditText3.getText().toString()));
						PlutoSequence.LED textLED = new PlutoSequence.LED(Short.parseShort(mEditText4.getText().toString()));
						PlutoSequence.Vibe textVibe = new PlutoSequence.Vibe(Short.parseShort(mEditText5.getText().toString()));
						PlutoSequence.Sound textSound = new PlutoSequence.Sound(Short.parseShort(mEditText6.getText().toString()));

						short startHour = Short.parseShort(params[0]);
						short startMinute = Short.parseShort(params[1]);
						short endHour = Short.parseShort(params[2]);
						short endMinute = Short.parseShort(params[3]);

						NotificationsSettings notificationsSettings = new NotificationsSettings(callLED, callVibe, callSound,
								textLED, textVibe, textSound, startHour, startMinute, endHour, endMinute);
;
						mService.startSettingCallTextNotification(notificationsSettings);
					} else {
						setMessage("GET CALL TEXT NOTIFICATIONS");
						mService.startGettingCallTextNotification();
					}
				}
				break;
			case R.id.radioDisableNoti:
				if (checked) {
					setMessage("DISABLE ALL NOTIFICATIONS");
					mService.startDisablingAllNofications();
				}
				break;
			case R.id.radioSendCallNoti:
				if (checked) {
					setMessage("CALL NOTIFICATION");
					mService.startSendingCallNotification();
				}
				break;
			case R.id.radioSendTextNoti:
				if (checked) {
					setMessage("TEXT NOTIFICATION");
					mService.startSendingTextNofication();
				}
				break;
			case R.id.radioStopNoti:
				if (checked) {
					setMessage("STOP NOTIFICATION");
					mService.startSendingStopNofication();
				}
				break;
			case R.id.radioLed:
				if (checked) {
					if(checkEmptyForAllTextView()) {
						Toast.makeText(PlutoActivity.this, "Some fields are empty, please recheck", Toast.LENGTH_SHORT).show();
						return;
					}

					setMessage("PLAY LED ANIMATION");

					PlutoSequence.LED sequence = new PlutoSequence.LED(Short.parseShort(mEditText1.getText().toString()));
					Short mRepeat = Short.parseShort(mEditText2.getText().toString());
					Integer milliSecondsRepeat = Integer.parseInt(mEditText3.getText().toString());

					mService.playLEDAnimation(sequence, mRepeat, milliSecondsRepeat);
				}
				break;
			case R.id.radioVibe:
				if (checked) {
					if(checkEmptyForAllTextView()) {
						Toast.makeText(PlutoActivity.this, "Some fields are empty, please recheck", Toast.LENGTH_SHORT).show();
						return;
					}

					setMessage("PLAY VIBRATION");

					PlutoSequence.Vibe sequence = new PlutoSequence.Vibe(Short.parseShort(mEditText1.getText().toString()));
					Short mRepeat = Short.parseShort(mEditText2.getText().toString());
					Integer milliSecondsRepeat = Integer.parseInt(mEditText3.getText().toString());

					mService.playVibration(sequence, mRepeat, milliSecondsRepeat);
				}
				break;
			case R.id.radioSound:
				if (checked) {
					if(checkEmptyForAllTextView()) {
						Toast.makeText(PlutoActivity.this, "Some fields are empty, please recheck", Toast.LENGTH_SHORT).show();
						return;
					}

					setMessage("PLAY SOUND");

					PlutoSequence.Sound sequence = new PlutoSequence.Sound(Short.parseShort(mEditText1.getText().toString()));
					Short mRepeat = Short.parseShort(mEditText2.getText().toString());
					Integer milliSecondsRepeat = Integer.parseInt(mEditText3.getText().toString());

					mService.playSound(sequence, mRepeat, milliSecondsRepeat);
				}
				break;
		}
	}

	private boolean checkEmptyForAllTextView() {
		return (isTextViewEmpty(mEditText1) || isTextViewEmpty(mEditText2) ||
				isTextViewEmpty(mEditText3) || isTextViewEmpty(mEditText4) ||
				isTextViewEmpty(mEditText5) || isTextViewEmpty(mEditText6) ||
				isTextViewEmpty(mEditText7) || isTextViewEmpty(mEditText8));
	}

	private boolean isTextViewEmpty(TextView tv) {
		return tv.isEnabled() && TextUtils.isEmpty(tv.getText().toString());
	}

	@Override
	public void onBackPressed() {
		Intent shineIntent = new Intent(PlutoActivity.this, ShineActivity.class);
		startActivity(shineIntent);
		finish();
	}
}
