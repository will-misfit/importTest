package com.misfit.ble.setting.pluto;

/**
 * Created by Quoc-Hung Le on 8/18/15.
 * <br>Contain all properties for notifications when call(s) or text message(s) come</br>
 */
public class NotificationsSettings {

	private static final String SEPARATOR = ",";

	private PlutoSequence.LED mCallLEDSequence;
	private PlutoSequence.Vibe mCallVibeSequence;
	private PlutoSequence.Sound mCallSoundSequence;
	private PlutoSequence.LED mTextLEDSequence;
	private PlutoSequence.Vibe mTextVibeSequence;
	private PlutoSequence.Sound mTextSoundSequence;

	private short mStartHour;
	private short mStartMinute;
	private short mEndHour;
	private short mEndMinute;

	/**
	 * Initialize {@link NotificationsSettings}
	 */
	public NotificationsSettings() {

	}

	/**
	 * Initialize {@link NotificationsSettings} with properties
	 *
	 * @param callLEDSequence   led sequence for call
	 * @param callVibeSequence  vibration sequence for call
	 * @param callSoundSequence sound sequence for call
	 * @param textLEDSequence   led sequence for text message
	 * @param textVibeSequence  vibration sequence for text message
	 * @param textSoundSequence sound sequence for text message
	 * @param startHour         start hour
	 * @param startMinute       start minute
	 * @param endHour           end hour
	 * @param endMinute         end minute
	 */
	public NotificationsSettings(PlutoSequence.LED callLEDSequence, PlutoSequence.Vibe callVibeSequence, PlutoSequence.Sound callSoundSequence,
								 PlutoSequence.LED textLEDSequence, PlutoSequence.Vibe textVibeSequence, PlutoSequence.Sound textSoundSequence,
								 short startHour, short startMinute, short endHour, short endMinute) {
		mCallLEDSequence = callLEDSequence;
		mCallVibeSequence = callVibeSequence;
		mCallSoundSequence = callSoundSequence;
		mTextLEDSequence = textLEDSequence;
		mTextVibeSequence = textVibeSequence;
		mTextSoundSequence = textSoundSequence;

		mStartHour = startHour;
		mStartMinute = startMinute;
		mEndHour = endHour;
		mEndMinute = endMinute;
	}

	public PlutoSequence.LED getCallLEDSequence() {
		return mCallLEDSequence;
	}

	public PlutoSequence.Vibe getCallVibeSequence() {
		return mCallVibeSequence;
	}

	public PlutoSequence.Sound getCallSoundSequence() {
		return mCallSoundSequence;
	}

	public PlutoSequence.LED getTextLEDSequence() {
		return mTextLEDSequence;
	}

	public PlutoSequence.Vibe getTextVibeSequence() {
		return mTextVibeSequence;
	}

	public PlutoSequence.Sound getTextSoundSequence() {
		return mTextSoundSequence;
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

	public void setCallLEDSequence(PlutoSequence.LED callLEDSequence) {
		this.mCallLEDSequence = callLEDSequence;
	}

	public void setCallVibeSequence(PlutoSequence.Vibe callVibeSequence) {
		this.mCallVibeSequence = callVibeSequence;
	}

	public void setCallSoundSequence(PlutoSequence.Sound callSoundSequence) {
		this.mCallSoundSequence = callSoundSequence;
	}

	public void setTextLEDSequence(PlutoSequence.LED textLEDSequence) {
		this.mTextLEDSequence = textLEDSequence;
	}

	public void setTextVibeSequence(PlutoSequence.Vibe textVibeSequence) {
		this.mTextVibeSequence = textVibeSequence;
	}

	public void setTextSoundSequence(PlutoSequence.Sound textSoundSequence) {
		this.mTextSoundSequence = textSoundSequence;
	}

	public void setStartHour(short startHour) {
		this.mStartHour = startHour;
	}

	public void setStartMinute(short startMinute) {
		this.mStartMinute = startMinute;
	}

	public void setEndHour(short endHour) {
		this.mEndHour = endHour;
	}

	public void setEndMinute(short endMinute) {
		this.mEndMinute = endMinute;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(mCallLEDSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mCallVibeSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mCallSoundSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mTextLEDSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mTextVibeSequence.getValue());
		buffer.append(SEPARATOR);
		buffer.append(mTextSoundSequence.getValue());

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
