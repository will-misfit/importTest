package com.misfit.ble.shine.parser;

public class ActivityDataParserFactory {
	public static ActivityDataParser getDataParser(int fileFormat) {
		if (fileFormat == 0x0011 || fileFormat == 0x0012) {
	        return new ActivityDataParserLegacy();
	    } else {
	        return new ActivityDataParserNew();
	    }
	}
}
