package com.misfit.ble.util;

import java.util.Locale;


public class Convertor {
	
	public static String identity(Object anObject) {
		if (anObject == null)
			return "";
		
		return Integer.toHexString(System.identityHashCode(anObject));
	}
	
	public static byte[] bytesFromString(String string) {
		if (string == null)
			return null;
		
		byte[] bytes = new byte[string.length() / 2];
		for (int i = 0; i < bytes.length; i += 1) {
			bytes[i] = (byte) (((Character.digit(string.charAt(i * 2), 16)) << 4) + Character.digit(string.charAt(i * 2 + 1), 16));
		}
		return bytes;
	}
	
	public static String bytesToString(byte[] bytes) {
    	return bytesToString(bytes, "");
    }
	
	public static String bytesToString(byte[] bytes, String seperator) {
		if (bytes == null || bytes.length == 0)
			return null;
		
		String format = "%02X";
		String formatWithSeperator = format + seperator;
		
    	StringBuilder sb = new StringBuilder();
    	
    	int i = 0;
    	while (i < bytes.length - 1) {
    		sb.append(String.format(Locale.US, formatWithSeperator, bytes[i++]));
    	}
    	if (i == bytes.length - 1) {
    		sb.append(String.format(Locale.US, format, bytes[i]));
    	}
    	return sb.toString();
    }

	public static String bytesToStringInReverse(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			return null;

		String format = "%02X";
		StringBuilder sb = new StringBuilder();

		int i = bytes.length - 1;
		while (i >= 0) {
            sb.append(String.format(Locale.US, format, bytes[i--]));
		}
		return sb.toString();
	}
	
	public static int unsignedIntFromLong(long value) {
		if (value >= 0) {
			return (int)(0xffffffff & value);
		}
		return 0;
	}
	
	public static long unsignedIntToLong(int value) {
		if (value < 0) {
			return 0xffffffffl + 1 + value;
		}
		return value;
	}
	
	public static short unsignedShortFromInteger(int value) {
		if (value >= 0) {
			return (short)(0xffff & value);
		}
		return 0;
	}
	
	public static int unsignedShortToInteger(short value) {
		if (value < 0) {
			return 0xffff + 1 + value;
		}
		return value;
	}
	
	public static byte unsignedByteFromShort(short value) {
		if (value >= 0) {
			return (byte)(0xff & value);
		}
		return 0;
	}
	
	public static short unsignedByteToShort(byte value) {
		if (value < 0) {
			return (short) (0xff + 1 + value);
		}
		return value;
	}

	// Quoc-Hung Le
	public static byte byteFromBoolean(boolean value) {
		return (byte) (value ? 0x01 : 0x00);
	}

	public static boolean byteToBoolean(byte value) {
		return value != 0x00;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

	public static byte[] bytesFromInteger(int value) {
		byte[] data = new byte[2];

		data[0] = (byte) (value & 0xFF);
		data[1] = (byte) ((value >> 8) & 0xFF);

		return data;
	}
}
