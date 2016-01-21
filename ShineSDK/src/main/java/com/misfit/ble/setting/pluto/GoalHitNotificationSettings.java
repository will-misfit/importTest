package com.misfit.ble.setting.pluto;

/**
 * Created by Quoc-Hung Le on 8/18/15.
 * <br>Contain all properties for notifications when user hit goal</br>
 */
public class GoalHitNotificationSettings {

	private static final String SEPARATOR = ",";

	private boolean mEnabled;
	private PlutoSequence.LED mLEDSequence;
	private PlutoSequence.Vibe mVibeSequence;
	private PlutoSequence.Sound mSoundSequence;
	private short mStartHour;
	private short mStartMinute;
	private short mEndHour;
	private short mEndMinute;

	/**
	 * Initialize {@link GoalHitNotificationSettings} with properties
	 *
	 * @param enabled       enable setting or not?
	 * @param ledSequence   led sequence
	 * @param vibeSequence  vibration sequence
	 * @param soundSequence sound sequence
	 * @param startHour     hour for starting goal hit notification
	 * @param startMinute   minute for starting goal hit notification
	 * @param endHour       hour for ending goal hit notification
	 * @param endMinute     minute for ending goal hit notification
	 */
	public GoalHitNotificationSettings(boolean enabled, PlutoSequence.LED ledSequence,
									   PlutoSequence.Vibe vibeSequence, PlutoSequence.Sound soundSequence,
									   short startHour, short startMinute, short endHour, short endMinute) {
		mEnabled = enabled;
		mLEDSequence = ledSequence;
		mVibeSequence = vibeSequence;
		mSoundSequence = soundSequence;
		mStartHour = startHour;
		mStartMinute = startMinute;
		mEndHour = endHour;
		mEndMinute = endMinute;
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

		return buffer.toString();
	}
}
