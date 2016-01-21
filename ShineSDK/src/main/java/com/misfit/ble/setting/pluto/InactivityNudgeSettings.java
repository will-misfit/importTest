package com.misfit.ble.setting.pluto;

/**
 * Created by Quoc-Hung Le on 8/17/15.
 * <br>Contain all properties for notification when user stay idle too long</br>
 */
public class InactivityNudgeSettings {

	private static final String SEPARATOR = ",";

	private boolean mEnabled = false;
	private PlutoSequence.LED mLEDSequence;
	private PlutoSequence.Vibe mVibeSequence;
	private PlutoSequence.Sound mSoundSequence;
	private short mStartHour;
	private short mStartMinute;
	private short mEndHour = 0;
	private short mEndMinute = 0;
	private short mRepeatIntervalInMinute = 0;

	/**
	 * Initialize {@link InactivityNudgeSettings} with properties
	 *
	 * @param enabled                enable setting or not?
	 * @param ledSequence            led sequence
	 * @param vibeSequence           vibration sequence
	 * @param soundSequence          sound sequence
	 * @param startHour              hour for starting goal hit notification
	 * @param startMinute            minute for starting goal hit notification
	 * @param endHour                hour for ending goal hit notification
	 * @param endMinute              minute for ending goal hit notification
	 * @param repeatIntervalInMinute repeat interval in minutes
	 */
	public InactivityNudgeSettings(boolean enabled, PlutoSequence.LED ledSequence, PlutoSequence.Vibe vibeSequence, PlutoSequence.Sound soundSequence, short startHour, short startMinute, short endHour, short endMinute, short repeatIntervalInMinute) {
		mEnabled = enabled;
		mLEDSequence = ledSequence;
		mVibeSequence = vibeSequence;
		mSoundSequence = soundSequence;
		mStartHour = startHour;
		mStartMinute = startMinute;
		mEndHour = endHour;
		mEndMinute = endMinute;
		mRepeatIntervalInMinute = repeatIntervalInMinute;
	}

	public boolean getEnabled() {
		return mEnabled;
	}

	public PlutoSequence.LED getLEDSequence() {
		return mLEDSequence;
	}

	public PlutoSequence.Vibe getVibeSequence() {
		return mVibeSequence;
	}

	public PlutoSequence.Sound getSoundSequence() {
		return mSoundSequence;
	}

	public short getStartHour() {
		return mStartHour;
	}

	public short getStartMinute() {
		return mStartMinute;
	}

	public short getEndHour() {
		return mEndHour;
	}

	public short getEndMinute() {
		return mEndMinute;
	}

	public short getRepeatIntervalInMinute() {
		return mRepeatIntervalInMinute;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(mEnabled);
		buffer.append(SEPARATOR);
		buffer.append(mLEDSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mVibeSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mSoundSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mStartHour);
		buffer.append(SEPARATOR);
		buffer.append(mStartMinute);
		buffer.append(SEPARATOR);
		buffer.append(mEndHour);
		buffer.append(SEPARATOR);
		buffer.append(mEndMinute);
		buffer.append(SEPARATOR);
		buffer.append(mRepeatIntervalInMinute);

		return buffer.toString();
	}
}
