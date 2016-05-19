package com.misfit.ble.setting.speedo;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityType {
    public static final byte RUNNING = 0x01;
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
        JSONObject valueJSON = new JSONObject();
        try {
            valueJSON.put("value", mValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return valueJSON.toString();
    }
}