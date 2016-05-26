package com.misfit.ble.setting.lapCounting;

import org.json.JSONException;
import org.json.JSONObject;

public final class LapCountingLicenseStatus {
    public static final byte NOT_REDAY = 0;
    public static final byte REDAY = 1;

    private byte mValue;

    public byte getValue() {
        return mValue;
    }

    public LapCountingLicenseStatus(byte status) {
        mValue = status;
    }

    @Override
    public String toString() {
        JSONObject valueJSON = new JSONObject();
        try {
            valueJSON.put("status", mValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return valueJSON.toString();
    }
}
