package com.misfit.ble.shine.parser;

import android.util.Log;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.parser.swim.SwimLapEntry;
import com.misfit.ble.shine.parser.swim.SwimSessionEntry;
import com.misfit.ble.shine.parser.swim.SwimSessionEntry.SwimSessionEntryType;
import com.misfit.ble.shine.result.Activity;
import com.misfit.ble.shine.result.SessionEvent;
import com.misfit.ble.shine.result.TapEvent;
import com.misfit.ble.shine.result.TapEventSummary;
import com.misfit.ble.util.Convertor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;

public class ActivityDataParserNew extends ActivityDataParser {
	private static final String TAG = ActivityDataParserNew.class.getName();
	
	private int mFileHandle;
	private int mFileFormat;
	private long mFileLength;

	private long mFileTimestamp;
	private int mFileMilliseconds;
	private short mFileTimeZoneOffsetInMinutes;

	private int mFileAbsoluteNumber;
	private short mFileMinorVersion;
	private short mFileNumOfSpecialFields;
	
	private HashMap<Short, Short> mFileSpecialFieldsDataLength;

	private long mFileCRC;

	private byte[] mActivityData;
	private byte[] mRawData;

	private long mCurrentTimestamp;
	private int mCurrentOffset;

	public ActivityDataParserNew() {
		super();
	}
	
	public boolean parseRawData(byte[] rawData, int fileFormat, long fileTimestamp) {
	    if (rawData == null || rawData.length <= 0)
	        return false;
	    
	    mRawData = rawData;
	    
	    if (!handleFileHeader())
	        return true; // FIXME: should return false, and let SyncPhase discard the data
	    
	    if (mFileFormat != 0x0013)
	        return true; // FIXME: should return false, and let SyncPhase discard the data
	    
	    if (!handleActivityData())
	        return true; // FIXME: should return false, and let SyncPhase discard the data
	    
	    return true;
	}
	
	private boolean handleFileHeader() {
	    if (mRawData.length < Constants.ACTIVITY_FILE_HEADER_LENGTH)
	        return false;
	    
	    ByteBuffer byteBuffer = ByteBuffer.wrap(mRawData);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
	    mFileHandle = Convertor.unsignedShortToInteger(byteBuffer.getShort());
	    mFileFormat = Convertor.unsignedShortToInteger(byteBuffer.getShort(2));
	    mFileLength = Convertor.unsignedIntToLong(byteBuffer.getInt(4));
	    
	    mFileTimestamp = Convertor.unsignedIntToLong(byteBuffer.getInt(8));
	    mFileMilliseconds = Convertor.unsignedShortToInteger(byteBuffer.getShort(12));
	    mFileTimeZoneOffsetInMinutes = byteBuffer.getShort(14);
	    
	    mFileAbsoluteNumber = Convertor.unsignedShortToInteger(byteBuffer.getShort(16));
	    mFileMinorVersion = Convertor.unsignedByteToShort(byteBuffer.get(18));
	    
	    mFileNumOfSpecialFields = Convertor.unsignedByteToShort(byteBuffer.get(19));
	    
	    if (mRawData.length < Constants.ACTIVITY_FILE_HEADER_LENGTH 
	    		+ Constants.ACTIVITY_FILE_SPECIAL_FIELD_LENGTH * mFileNumOfSpecialFields 
	    		+ Constants.ACTIVITY_FILE_CRC_LENGTH)
	        return false;
	    
	    HashMap<Short, Short> specialFieldsLength = new HashMap<Short, Short>(mFileNumOfSpecialFields);
	    for (int i = 0; i < mFileNumOfSpecialFields; ++i) {
	    	int index = Constants.ACTIVITY_FILE_HEADER_LENGTH + Constants.ACTIVITY_FILE_SPECIAL_FIELD_LENGTH * i;
	        short fieldID = Convertor.unsignedByteToShort(byteBuffer.get(index));
	        short fieldLength = Convertor.unsignedByteToShort(byteBuffer.get(index + 1));
	        specialFieldsLength.put(fieldID, fieldLength);
	    }
	    mFileSpecialFieldsDataLength = specialFieldsLength;
	    
	    mFileCRC = byteBuffer.getInt(mRawData.length - 4);
	    
	    int activityDataOffset = Constants.ACTIVITY_FILE_HEADER_LENGTH + Constants.ACTIVITY_FILE_SPECIAL_FIELD_LENGTH * mFileNumOfSpecialFields;
	    int activityDataEndOffset = mRawData.length - Constants.ACTIVITY_FILE_CRC_LENGTH;
	    mActivityData = Arrays.copyOfRange(mRawData, activityDataOffset, activityDataEndOffset);
	    
	    return true;
	}
	
	private boolean handleActivityData() {
	    boolean isValid = true;
	    
	    mCurrentTimestamp = mFileTimestamp; // FIXME: the fileTimestamp is, in fact, corresponding with the third minute.
	    mCurrentOffset = 0;
	    
	    while (mCurrentOffset < mActivityData.length && isValid) {
	    	short entryType = Convertor.unsignedByteToShort(mActivityData[mCurrentOffset]);
	        int entryLength = lengthOfEntry(entryType);
	        
	        int nextEntryOffset = mCurrentOffset + entryLength;
	        if (nextEntryOffset < mActivityData.length) {
	        	short nextEntryType = mActivityData[nextEntryOffset];
	        	if (nextEntryType == Constants.IGNORE_PREV_ENTRY_CODE) {
	        		mCurrentOffset += entryLength + lengthOfEntry(Constants.IGNORE_PREV_ENTRY_CODE);
	        		continue;
	        	}
	        }
	        
	        if (entryType < Constants.MIN_SPECIAL_ENTRY_CODE) {
	        	handleEntryNormalActivity();
	        }
	        else {
	            switch (entryType) {
	                case 213: isValid = handleEntry213DoubleTapSummary(); break;
	                case 215: isValid = handleEntry215TripleTapSummary(); break;
	                case 220: isValid = handleEntry220BigActivity(); break;
	                case 221: isValid = handleEntry221StartSession(); break;
	                case 222: isValid = handleEntry222EndSession(); break;
	                case 225: isValid = handleEntry225InformationCode(); break;
	                case 226: isValid = handleEntry226InformationCodeWithTimestamp(); break;
	                case 230: isValid = handleEntry230LapCountSessionMarker(); break;
	                case 231: isValid = handleEntry231NewLapDetected(); break;
	                case 254: isValid = handleEntry254PadBytes(); break;
	                case 201: isValid = handleEntry201QuadrupleTap(); break;

	                default:
	                    Log.w(TAG, "Unknown entry: offset=" + mCurrentOffset + ", entryType=" + entryType);
	                    isValid = handleEntryUnknown();
	                    break;
	            }
	            
	            if (!isValid) {
	            	Log.w(TAG, "Invalid entry: offset=" + mCurrentOffset + ", entryType=" + entryType);
	            }
	        }
	        mCurrentOffset += entryLength;
	    }
	    return isValid;
	}

    private boolean handleEntry201QuadrupleTap() {
        int length = 5;
        if (mCurrentOffset + length > mActivityData.length) {
            mCurrentOffset = mActivityData.length;
            return false;
        }
        short event = Convertor.unsignedByteToShort(mActivityData[mCurrentOffset+1]);
        ByteBuffer byteBuffer = ByteBuffer.wrap(mActivityData, mCurrentOffset + 2, 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        long time = Convertor.unsignedIntToLong(byteBuffer.getInt());
        mTapEventSummarys.add(new TapEventSummary(time, event, 1));
        return true;
    }

	/**
	 * Get the length of specified entry.
	 * @param entryType
	 * @return the length of specified entry, include the entry type length
     */
	private int lengthOfEntry(short entryType) {
		if (entryType < Constants.MIN_SPECIAL_ENTRY_CODE) {
			return 2;
		}

		if (mFileSpecialFieldsDataLength.get(entryType) == null) {
			return 1;
		}
		return mFileSpecialFieldsDataLength.get(entryType) + 1;
	}

	private boolean handleEntryUnknown() {
		int length = 2;
		if (mCurrentOffset + length > mActivityData.length) {
			mCurrentOffset = mActivityData.length;
			return false;
		}
		return true;
	}

	private boolean handleEntry213DoubleTapSummary() {
		int length = 2;
		if (mCurrentOffset + length > mActivityData.length) {
			mCurrentOffset = mActivityData.length;
			return false;
		}

		short count = Convertor.unsignedByteToShort(mActivityData[mCurrentOffset + 1]);
		if (count < 0)
			return false;

		TapEventSummary event = new TapEventSummary(mCurrentTimestamp, 
				TapEvent.TAP_TYPE_DOUBLE, count);
		mTapEventSummarys.add(event);
		return true;
	}

	private boolean handleEntry215TripleTapSummary() {
		int length = 2;
		if (mCurrentOffset + length > mActivityData.length) {
			mCurrentOffset = mActivityData.length;
			return false;
		}

		short count = Convertor.unsignedByteToShort(mActivityData[mCurrentOffset + 1]);
		if (count < 0)
			return false;

		TapEventSummary event = new TapEventSummary(mCurrentTimestamp,
				TapEvent.TAP_TYPE_TRIPLE, count);
		mTapEventSummarys.add(event);
		return true;
	}

	private boolean handleEntry221StartSession() {
		int length = 2;
		if (mCurrentOffset + length > mActivityData.length) {
			mCurrentOffset = mActivityData.length;
			return false;
		}

		short second = Convertor.unsignedByteToShort(mActivityData[mCurrentOffset + 1]);
		if (second < 0 || second > 59)
			return false;

		SessionEvent event = new SessionEvent(mCurrentTimestamp + second,
				SessionEvent.SESSION_EVENT_TYPE_START);
		mSessionEvents.add(event);
		return true;
	}

	private boolean handleEntry222EndSession() {
		int length = 2;
		if (mCurrentOffset + length > mActivityData.length) {
			mCurrentOffset = mActivityData.length;
			return false;
		}

		short second = Convertor.unsignedByteToShort(mActivityData[mCurrentOffset + 1]);
		if (second < 0 || second > 59)
			return false;

		SessionEvent event = new SessionEvent(mCurrentTimestamp + second,
				SessionEvent.SESSION_EVENT_TYPE_END);
		mSessionEvents.add(event);
		return true;
	}

	private boolean handleEntry220BigActivity() {
		int length = 6;
		if (mCurrentOffset + length > mActivityData.length) {
			mCurrentOffset = mActivityData.length;
			return false;
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(mActivityData, mCurrentOffset + 2, 4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		int bipedalCount = Convertor.unsignedShortToInteger(byteBuffer.getShort());
		int points = Convertor.unsignedShortToInteger(byteBuffer.getShort());

		Activity activity = new Activity(mCurrentTimestamp,
				mCurrentTimestamp + 59, bipedalCount, points,
				Activity.DEFAULT_VARIANCE);
		mActivities.add(activity);
		mCurrentTimestamp += 60;
		return true;
	}

	private boolean handleEntryNormalActivity() {
		int length = 2;
		if (mCurrentOffset + length > mActivityData.length) {
			mCurrentOffset = mActivityData.length;
			return false;
		}

		Activity activity = null;

		short firstByte = Convertor.unsignedByteToShort(mActivityData[mCurrentOffset]);
		short secondByte = Convertor.unsignedByteToShort(mActivityData[mCurrentOffset + 1]);
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
		return true;
	}
	
	private boolean handleEntry225InformationCode() {
	    return true;
	}
	
	private boolean handleEntry226InformationCodeWithTimestamp() {
	    return true;
	}
	
	private boolean handleEntry230LapCountSessionMarker() {
	    int length = 2;
	    if (mCurrentOffset + length > mActivityData.length) {
	        return false;
	    }
	    
	    short dataByte = Convertor.unsignedByteToShort(mActivityData[mCurrentOffset + 1]);
	    
	    SwimSessionEntry sessionEntry = new SwimSessionEntry();
	    sessionEntry.mEntryType = SwimSessionEntryType.valueOf((dataByte & 0xC0) >> 6);
	    sessionEntry.mTimestamp = mCurrentTimestamp + (dataByte & 0x3F) * 1.0 / 10;
	    mSwimEntries.add(sessionEntry);
	    return true;
	}

	private boolean handleEntry231NewLapDetected() {
	    int length = 8;
	    if (mCurrentOffset + length > mActivityData.length)
	        return false;
	    
	    ByteBuffer byteBuffer = ByteBuffer.wrap(mActivityData, mCurrentOffset + 1, 7);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
	    int stroke = Convertor.unsignedByteToShort(byteBuffer.get());
	    long duration = Convertor.unsignedShortToInteger(byteBuffer.getShort());
	    long endTime = (Convertor.unsignedByteToShort(byteBuffer.get()) 
	    		+ (Convertor.unsignedByteToShort(byteBuffer.get()) << 8) 
	    		+ (Convertor.unsignedByteToShort(byteBuffer.get()) << 16));
	    int svm = Convertor.unsignedByteToShort(byteBuffer.get());
	    
	    SwimLapEntry lapEntry = new SwimLapEntry();
	    lapEntry.mStrokes = stroke;
	    lapEntry.mDurationInOneTenthSeconds = duration;
	    lapEntry.mEndTimeSinceSessionStartInOneTenthSeconds = endTime;
	    lapEntry.mSvm = svm;
	    mSwimEntries.add(lapEntry);
	    return true;
	}

	private boolean handleEntry254PadBytes() {
	    return true;
	}
}
