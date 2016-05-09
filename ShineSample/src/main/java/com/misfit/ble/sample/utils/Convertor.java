package com.misfit.ble.sample.utils;

import java.util.Locale;

/**
 * Created by Quoc-Hung Le on 9/16/15.
 */
public class Convertor {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
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
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
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

    /**
     * note: the converted bytes is big endian.
     */
    public static byte[] long2BEBytes(long num) {
        byte[] result = new byte[8];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (num >> (8 * (7 - i)));
        }
        return result;
    }

    /**
     * note: the converted bytes is big endian.
     */
    public static byte[] unsignedInt2BEBytes(int num) {
        byte[] result = new byte[4];
        result[0] = (byte) (num >> 24);
        result[1] = (byte) (num >> 16);
        result[2] = (byte) (num >> 8);
        result[3] = (byte) (num);
        return result;
    }
}
