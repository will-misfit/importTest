package com.misfit.ble.setting.pluto;

/**
 * Created by Quoc-Hung Le on 8/18/15.
 * <br>Contain all properties for alarm</br>
 */
public class AlarmSettings {

	// These constants are used for checking requests are executing or not
	public static final byte NON_SET_ALARM_DAY = 0;
	public static final byte MONDAY = 1;
	public static final byte TUESDAY = 2;
	public static final byte WEDNESDAY = 4;
	public static final byte THURSDAY = 8;
	public static final byte FRIDAY = 16;
	public static final byte SATURDAY = 32;
	public static final byte SUNDAY = 64;
	public static final byte ALL_DAYS = -128;

	public static final byte SET_ALARM = 1;
	public static final byte CLEAR_ALARM = 2;
	public static final byte TOGGLE_ALARM = 3;

	public static final byte ONE_SHOT = 1;
	public static final byte REPEATED = 2;

	private static final String SEPARATOR = ",";

	private byte mAlarmDay = NON_SET_ALARM_DAY;
	private byte mAlarmOperation;
	private byte mAlarmType;

	private short mAlarmHour;
	private short mAlarmMinute;

	private PlutoSequence.LED mLEDSequence;
	private PlutoSequence.Vibe mVibeSequence;
	private PlutoSequence.Sound mSoundSequence;

	private short mWindowInMinute;
	private short mSnoozeTimeInMinute;
	private short mAlarmDuration;

	public AlarmSettings() {

	}

	/**
	 * Initialize {@link AlarmSettings} with properties
	 *
	 * @param alarmDay         {@code MONDAY}, {@code TUESDAY},..., {@code ALL_DAYS}
	 * @param alarmOperation   {@code SET_ALARM} or {@code CLEAR_ALARM} or {@code TOGGLE_ALARM}
	 * @param alarmType        {@code ONE_SHOT} or {@code REPEATED}
	 * @param alarmHour        hour for alarm
	 * @param alarmMinute      minute for alarm
	 * @param smartAlarmWinMin minute for smart alarm
	 * @param ledSequence      led sequence id
	 * @param vibeSequence     vibration sequence id
	 * @param soundSequence    sound sequence id
	 * @param minPerSnooze     minute per snooze
	 * @param alarmDuration    alarm duration
	 */
	public AlarmSettings(byte alarmDay, byte alarmOperation, byte alarmType, short alarmHour, short alarmMinute,
						 short smartAlarmWinMin, PlutoSequence.LED ledSequence, PlutoSequence.Vibe vibeSequence, PlutoSequence.Sound soundSequence,
						 short minPerSnooze, short alarmDuration) {
		mAlarmDay = alarmDay;
		mAlarmOperation = alarmOperation;
		mAlarmType = alarmType;
		mAlarmHour = alarmHour;
		mAlarmMinute = alarmMinute;

		mWindowInMinute = smartAlarmWinMin;
		mLEDSequence = ledSequence;
		mVibeSequence = vibeSequence;
		mSoundSequence = soundSequence;
		mSnoozeTimeInMinute = minPerSnooze;
		mAlarmDuration = alarmDuration;
	}

	public byte getAlarmDay() {
		return mAlarmDay;
	}

	public void setAlarmDay(byte alarmDay) {
		this.mAlarmDay = alarmDay;
	}

	public byte getAlarmOperation() {
		return mAlarmOperation;
	}

	public void setAlarmOperation(byte alarmOperation) {
		this.mAlarmOperation = alarmOperation;
	}

	public byte getAlarmType() {
		return mAlarmType;
	}

	public void setAlarmType(byte alarmType) {
		this.mAlarmType = alarmType;
	}

	public short getAlarmHour() {
		return mAlarmHour;
	}

	public void setAlarmHour(short alarmHour) {
		this.mAlarmHour = alarmHour;
	}

	public short getAlarmMinute() {
		return mAlarmMinute;
	}

	public void setAlarmMinute(short alarmMinute) {
		this.mAlarmMinute = alarmMinute;
	}

	public short getWindowInMinute() {
		return mWindowInMinute;
	}

	public void setWindowInMinute(short windowInMinute) {
		this.mWindowInMinute = windowInMinute;
	}

	public short getSnoozeTimeInMinute() {
		return mSnoozeTimeInMinute;
	}

	public void setSnoozeTimeInMinute(short snoozeTimeInMinute) {
		this.mSnoozeTimeInMinute = snoozeTimeInMinute;
	}

	public short getAlarmDuration() {
		return mAlarmDuration;
	}

	public void setAlarmDuration(short alarmDuration) {
		this.mAlarmDuration = alarmDuration;
	}

	public PlutoSequence.LED getLEDSequence() {
		return mLEDSequence;
	}

	public void setLEDSequence(PlutoSequence.LED mLEDSequence) {
		this.mLEDSequence = mLEDSequence;
	}

	public PlutoSequence.Vibe getVibeSequence() {
		return mVibeSequence;
	}

	public void setVibeSequence(PlutoSequence.Vibe mVibeSequence) {
		this.mVibeSequence = mVibeSequence;
	}

	public PlutoSequence.Sound getSoundSequence() {
		return mSoundSequence;
	}

	public void setSoundSequence(PlutoSequence.Sound mSoundSequence) {
		this.mSoundSequence = mSoundSequence;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(mAlarmDay);
		buffer.append(SEPARATOR);
		buffer.append(mAlarmOperation);
		buffer.append(SEPARATOR);
		buffer.append(mAlarmType);
		buffer.append(SEPARATOR);
		buffer.append(mAlarmHour);
		buffer.append(SEPARATOR);
		buffer.append(mAlarmMinute);
		buffer.append(SEPARATOR);
		buffer.append(mWindowInMinute);
		buffer.append(SEPARATOR);
		buffer.append(mLEDSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mVibeSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mSoundSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mSnoozeTimeInMinute);
		buffer.append(SEPARATOR);
		buffer.append(mAlarmDuration);

		return buffer.toString();
	}

	private static final byte DEFAULT_ALARM_DAY = ALL_DAYS;
	private static final byte DEFAULT_ALARM_OPERATION = 0;
	private static final byte DEFAULT_ALARM_TYPE = 0;
	private static final short DEFAULT_ALARM_HOUR = 0;
	private static final short DEFAULT_ALARM_MINUTE = 0;
	private static final short DEFAULT_WINDOW_MINUTE = 20;
	private static final short DEFAULT_LED_SEQUENCE = 0;
	private static final short DEFAULT_VIBE_SEQUENCE = 3;
	private static final short DEFAULT_SOUND_SEQUENCE = 0;
	private static final short DEFAULT_SNOOZE_MINUTE = 8;
	private static final short DEFAULT_ALARM_DURATION = 60;

	public boolean isDefaultValue() {
		return mAlarmDay == DEFAULT_ALARM_DAY && mAlarmOperation == DEFAULT_ALARM_OPERATION &&
				mAlarmType == DEFAULT_ALARM_TYPE && mAlarmHour == DEFAULT_ALARM_HOUR &&
				mAlarmMinute == DEFAULT_ALARM_MINUTE && mWindowInMinute == DEFAULT_WINDOW_MINUTE &&
				mLEDSequence.getValue() == DEFAULT_LED_SEQUENCE && mVibeSequence.getValue() == DEFAULT_VIBE_SEQUENCE &&
				mSoundSequence.getValue() == DEFAULT_SOUND_SEQUENCE && mSnoozeTimeInMinute == DEFAULT_SNOOZE_MINUTE &&
				mAlarmDuration == DEFAULT_ALARM_DURATION;
	}
}
