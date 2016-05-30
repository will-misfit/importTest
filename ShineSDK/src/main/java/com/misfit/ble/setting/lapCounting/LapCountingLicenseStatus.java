package com.misfit.ble.setting.lapCounting;

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
        return "status = " + mValue;
    }
}
