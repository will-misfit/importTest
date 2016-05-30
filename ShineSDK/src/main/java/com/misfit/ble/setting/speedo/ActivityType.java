package com.misfit.ble.setting.speedo;

public class ActivityType {
    public final static byte RUNNING = 0x01;
    public final static byte CYCLING = 0x02;
    public final static byte SWIMMING = 0x03;
    public final static byte WALKING = 0x04;
    public final static byte TENNINS = 0x05;
    public final static byte BASKETBALL = 0x06;
    public final static byte FOOTBALL = 0x07;

    private byte mValue;

    public byte getValue() {
        return mValue;
    }

    public ActivityType(byte value) {
        mValue = value;
    }

    @Override
    public String toString() {
        return "type = " + mValue;
    }
}