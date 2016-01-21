package com.misfit.ble.shine.parser;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.result.Activity;
import com.misfit.ble.shine.result.OrientationEvent;
import com.misfit.ble.shine.result.SessionEvent;
import com.misfit.ble.shine.result.TapEvent;
import com.misfit.ble.shine.result.TapEventSummary;
import com.misfit.ble.util.Convertor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ActivityDataParserLegacy extends ActivityDataParser {
	private byte[] mData;
	private long mCurrentTimestamp;
	private int mOffset;
	private short mMinorFileType;

	public ActivityDataParserLegacy() {
		super();

		mData = null;
		mCurrentTimestamp = 0;
		mOffset = 0;
		mMinorFileType = 0;
	}

	public boolean parseRawData(byte[] rawData, int fileFormat, long fileTimestamp) {
		byte[] activityData = Arrays.copyOfRange(rawData, Constants.ACTIVITY_FILE_HEADER_LENGTH_LEGACY, rawData.length - Constants.ACTIVITY_FILE_CRC_LENGTH);
		return parseActivityData(fileFormat, fileTimestamp, activityData);
	}

	public boolean parseActivityData(int format, long timestamp, byte[] data) {
		if (data == null || data.length <= 0)
			return false;

		mCurrentTimestamp = timestamp;
		mData = data;
		mOffset = 0;

		if (format == 0x0011) {
			return parseFile_v0011();
		} else if (format == 0x0012) {
			return parseFile_v0012();
		} else {
			// TODO: report unknown fileFormat.
			return true;
		}
		// return false;
	}

	private boolean parseFile_v0011() {
		boolean isValid = true;
		int length = mData.length;

		while (mOffset < length && isValid) {
			short entryType = Convertor.unsignedByteToShort(mData[mOffset]);

			if (entryType < 200) {
				handleEntryNormalActivity_v0011();
			} else {
				switch (entryType) {
				case 200:
					isValid = handleEntry200Dormant();
					break;
				case 201:
					isValid = handleEntry201LowActivity();
					break;

				case 202:
					isValid = handleEntry202OrientationChange();
					break;
				case 203:
					isValid = handleEntry203LostData();
					break;
				case 204:
					isValid = handleEntry204NewTimestamp();
					break;

				case 205:
					isValid = handleEntry205BatteryVoltagesPlusTemperature();
					break;
				case 206:
					isValid = handleEntry206BatteryVoltages();
					break;

				case 207:
					isValid = handleEntry207Padding();
					break;

				case 208:
					isValid = handleEntry208IgnoreLastEntry();
					break;
				case 209:
					isValid = handleEntry209ErrorCode();
					break;

				case 210:
					isValid = handleEntry210SingleTap();
					break;
				case 211:
					isValid = handleEntry211SingleTapSummary();
					break;

				case 212:
					isValid = handleEntry212DoubleTap();
					break;
				case 213:
					isValid = handleEntry213DoubleTapSummary();
					break;

				case 214:
					isValid = handleEntry214TripleTap();
					break;
				case 215:
					isValid = handleEntry215TripleTapSummary();
					break;

				case 220:
					isValid = handleEntry220BigActivity();
					break;

				default:
					isValid = handleEntryUnknown();
				}
			}
		}
		return isValid;
	}

	private boolean parseFile_v0012() {
		boolean isValid = true;
		int length = mData.length;

		while (mOffset < length && isValid) {
			short entryType = Convertor.unsignedByteToShort(mData[mOffset]);

			if (entryType < 200) {
				handleEntryNormalActivity_v0012();
			} else {
				switch (entryType) {
				case 200:
					isValid = handleEntry200Dormant();
					break;
				case 201:
					isValid = handleEntry201LowActivity();
					break;

				case 202:
					isValid = handleEntry202OrientationChange();
					break;
				case 203:
					isValid = handleEntry203LostData();
					break;
				case 204:
					isValid = handleEntry204NewTimestamp();
					break;

				case 205:
					isValid = handleEntry205BatteryVoltagesPlusTemperature();
					break;
				case 206:
					isValid = handleEntry206BatteryVoltages();
					break;

				case 207:
					isValid = handleEntry207Padding();
					break;

				case 208:
					isValid = handleEntry208IgnoreLastEntry();
					break;
				case 209:
					isValid = handleEntry209ErrorCode();
					break;

				case 210:
					isValid = handleEntry210SingleTap();
					break;
				case 211:
					isValid = handleEntry211SingleTapSummary();
					break;

				case 212:
					isValid = handleEntry212DoubleTap();
					break;
				case 213:
					isValid = handleEntry213DoubleTapSummary();
					break;

				case 214:
					isValid = handleEntry214TripleTap();
					break;
				case 215:
					isValid = handleEntry215TripleTapSummary();
					break;

				case 220:
					isValid = handleEntry220BigActivity();
					break;

				case 221: {
					if (mMinorFileType == 0x00)
						isValid = false;
					else
						isValid = handleEntry221StartSession();
					break;
				}
				case 222: {
					if (mMinorFileType == 0x00)
						isValid = false;
					else
						isValid = handleEntry222EndSession();
					break;
				}

				case 223:
					isValid = handleEntry223MinorFileTypeVersion();
					break;

				case 224:
					isValid = handleEntry224AbsoluteFileNumber();
					break;
				case 225:
					isValid = handleEntry225InformationalCode();
					break;

				default:
					isValid = handleEntryUnknown();
				}
			}
		}
		return isValid;
	}

	private boolean handleEntryUnknown() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}
		mOffset += length;
		return true;
	}

	private boolean handleEntry200Dormant() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short duration = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (duration < 1 || duration > 253)
			return false;

		long durationInSeconds = duration * 60l;

		Activity activity = new Activity(mCurrentTimestamp, (mCurrentTimestamp
				+ durationInSeconds - 1), 0, 0, Activity.DEFAULT_VARIANCE);
		mActivities.add(activity);
		mCurrentTimestamp += durationInSeconds;
		mOffset += length;
		return true;
	}

	private boolean handleEntry201LowActivity() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short duration = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (duration < 1 || duration > 253)
			return false;

		long durationInSeconds = duration * 60l;

		// FIXME: Low Activity ~~> bipedalCount and points is less than 1 (NOT
		// 0)
		Activity activity = new Activity(mCurrentTimestamp, (mCurrentTimestamp
				+ durationInSeconds - 1), 0, 0, Activity.DEFAULT_VARIANCE);
		mActivities.add(activity);
		mCurrentTimestamp += durationInSeconds;
		mOffset += length;
		return true;
	}

	private boolean handleEntry202OrientationChange() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short count = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (count < 1 || count > 253)
			return false;

		OrientationEvent event = new OrientationEvent(mCurrentTimestamp, count);
		mOrientationEvents.add(event);
		mOffset += length;
		return true;
	}

	private boolean handleEntry203LostData() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}
		mOffset += length;
		return true;
	}

	private boolean handleEntry204NewTimestamp() {
		int length = 10;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(mData, mOffset + 2, 8);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		long newTimestamp = Convertor.unsignedIntToLong(byteBuffer.getInt());
		int miliseconds = Convertor.unsignedShortToInteger(byteBuffer.getShort());
		int timezoneOffsetInMinute = Convertor.unsignedShortToInteger(byteBuffer.getShort());

		mCurrentTimestamp = newTimestamp;
		mOffset += length;
		return true;
	}

	private boolean handleEntry205BatteryVoltagesPlusTemperature() {
		int length = 6;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}
		mOffset += length;
		return true;
	}

	private boolean handleEntry206BatteryVoltages() {
		int length = 4;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}
		mOffset += length;
		return true;
	}

	private boolean handleEntry207Padding() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}
		mOffset += length;
		return true;
	}

	private boolean handleEntry208IgnoreLastEntry() {
		int length = 4;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}
		mOffset += length;
		return false;
	}

	private boolean handleEntry209ErrorCode() {
		int length = 6;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}
		mOffset += length;
		return false;
	}

	private boolean handleEntry210SingleTap() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short second = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (second < 0 || second > 59)
			return false;

		TapEvent event = new TapEvent(mCurrentTimestamp + second,
				TapEvent.TAP_TYPE_SINGLE);
		mTapEvents.add(event);
		mOffset += length;
		return true;
	}

	private boolean handleEntry211SingleTapSummary() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short count = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (count < 0)
			return false;

		TapEventSummary event = new TapEventSummary(mCurrentTimestamp,
				TapEvent.TAP_TYPE_SINGLE, count);
		mTapEventSummarys.add(event);
		mOffset += length;
		return true;
	}

	private boolean handleEntry212DoubleTap() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short second = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (second < 0)
			return false;

		TapEvent event = new TapEvent(mCurrentTimestamp + second,
				TapEvent.TAP_TYPE_DOUBLE);
		mTapEvents.add(event);
		mOffset += length;
		return true;
	}

	private boolean handleEntry213DoubleTapSummary() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short count = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (count < 0)
			return false;

		TapEventSummary event = new TapEventSummary(mCurrentTimestamp,
				TapEvent.TAP_TYPE_DOUBLE, count);
		mTapEventSummarys.add(event);
		mOffset += length;
		return true;
	}

	private boolean handleEntry214TripleTap() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short second = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (second < 0)
			return false;

		TapEvent event = new TapEvent(mCurrentTimestamp + second,
				TapEvent.TAP_TYPE_TRIPLE);
		mTapEvents.add(event);
		mOffset += length;
		return true;
	}

	private boolean handleEntry215TripleTapSummary() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short count = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (count < 0)
			return false;

		TapEventSummary event = new TapEventSummary(mCurrentTimestamp,
				TapEvent.TAP_TYPE_TRIPLE, count);
		mTapEventSummarys.add(event);
		mOffset += length;
		return true;
	}

	private boolean handleEntry221StartSession() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short second = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (second < 0 || second > 59)
			return false;

		SessionEvent event = new SessionEvent(mCurrentTimestamp + second,
				SessionEvent.SESSION_EVENT_TYPE_START);
		mSessionEvents.add(event);
		mOffset += length;
		return true;
	}

	private boolean handleEntry222EndSession() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short second = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (second < 0 || second > 59)
			return false;

		SessionEvent event = new SessionEvent(mCurrentTimestamp + second,
				SessionEvent.SESSION_EVENT_TYPE_END);
		mSessionEvents.add(event);
		mOffset += length;
		return true;
	}

	private boolean handleEntry223MinorFileTypeVersion() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short minorVersion = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if (minorVersion < 0 || minorVersion > 255)
			return false;

		mMinorFileType = minorVersion;
		mOffset += length;
		return true;
	}

	private boolean handleEntry220BigActivity() {
		int length = 6;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(mData, mOffset + 2, 4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		int bipedalCount = Convertor.unsignedShortToInteger(byteBuffer.getShort());
		int points = Convertor.unsignedShortToInteger(byteBuffer.getShort());

		Activity activity = new Activity(mCurrentTimestamp,
				mCurrentTimestamp + 59, bipedalCount, points,
				Activity.DEFAULT_VARIANCE);
		mActivities.add(activity);
		mCurrentTimestamp += 60;
		mOffset += length;
		return true;
	}

	private boolean handleEntryNormalActivity_v0011() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		short bipedalCount = Convertor.unsignedByteToShort(mData[mOffset]);
		short points = Convertor.unsignedByteToShort(mData[mOffset + 1]);

		Activity activity = new Activity(mCurrentTimestamp,
				mCurrentTimestamp + 59, bipedalCount, points,
				Activity.DEFAULT_VARIANCE);
		mActivities.add(activity);
		mCurrentTimestamp += 60;
		mOffset += length;
		return true;
	}

	private boolean handleEntryNormalActivity_v0012() {
		int length = 2;
		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}

		Activity activity = null;

		short firstByte = Convertor.unsignedByteToShort(mData[mOffset]);
		short secondByte = Convertor.unsignedByteToShort(mData[mOffset + 1]);
		if ((firstByte & 0x01) > 0) {
			// "Sneak in variance"
			short bipedalCount = (short) (firstByte & 0x0e);
			short points = (short) (secondByte & 0x03);
			int variance = (secondByte >> 2) + ((firstByte >> 4) << 6); // no
																		// need
																		// to
																		// convert
																		// to
																		// unsigned
																		// since
																		// variance
																		// only
																		// use 9
																		// bits
			activity = new Activity(mCurrentTimestamp, mCurrentTimestamp + 59,
					bipedalCount, points, variance);
		} else {
			short bipedalCount = firstByte;
			short points = secondByte;
			activity = new Activity(mCurrentTimestamp, mCurrentTimestamp + 59,
					bipedalCount, points, Activity.DEFAULT_VARIANCE);
		}

		mActivities.add(activity);
		mCurrentTimestamp += 60;
		mOffset += length;
		return true;
	}

	private boolean handleEntry224AbsoluteFileNumber() {
		int length = 2;

		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}
		// TODO: handle those 2 bytes
		mOffset += length;
		return true;
	}

	private boolean handleEntry225InformationalCode() {
		int length = 2;

		if (mOffset + length > mData.length) {
			mOffset = mData.length;
			return false;
		}
		// TODO: handle those 2 bytes
		mOffset += length;
		return true;
	}
}
