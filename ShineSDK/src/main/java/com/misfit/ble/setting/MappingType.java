package com.misfit.ble.setting;

public class MappingType {
    public static final byte HID = 0;
    public static final byte BOLT = 1;
    public static final byte TRACKER = 2;
    public static final byte APP = 3;
    public static final byte UNMAPPED = (byte) 255;

    private byte mValue;

    public MappingType(byte type) {
        mValue = type;
    }

    public byte getValue() {
        return mValue;
    }

    @Override
    public String toString() {
        return "mappingType = " + mValue;
    }
}
