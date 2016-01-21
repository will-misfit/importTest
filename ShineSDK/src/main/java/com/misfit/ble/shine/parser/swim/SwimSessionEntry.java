package com.misfit.ble.shine.parser.swim;

import java.util.HashMap;
import java.util.Map;

public class SwimSessionEntry {
	public enum SwimSessionEntryType {
		SwimSessionStartedByUser(1),
		SwimSessionEndedByUser(0),
		SwimSessionEndedByFirmware(2),
		SwimSessionIgnore(3),
		SwimSessionEndedBySDK(4);
		
		private int mValue;
	    private static Map<Integer, SwimSessionEntryType> map = new HashMap<Integer, SwimSessionEntryType>();

	    static {
	        for (SwimSessionEntryType legEnum : SwimSessionEntryType.values()) {
	            map.put(legEnum.mValue, legEnum);
	        }
	    }

		SwimSessionEntryType(final int value) { mValue = value; }
	    public static SwimSessionEntryType valueOf(int value) {
	        return map.get(value);
	    }
	}
	
	public double mTimestamp;
	public SwimSessionEntryType mEntryType;
}
