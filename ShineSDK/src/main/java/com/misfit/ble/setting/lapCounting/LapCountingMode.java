package com.misfit.ble.setting.lapCounting;

public enum LapCountingMode {
    MANUAL((byte) 0x00),
    TIMEOUT((byte) 0x01);

    private byte mMode;

    private LapCountingMode(byte mode) {
        this.mMode = mode;
    }

    public byte getMode() {
        return mMode;
    }

    public static LapCountingMode getModeFromByte(byte mode) {
        switch (mode) {
            case 0x00:
                return MANUAL;
            case 0x01:
                return TIMEOUT;
            default:
                return MANUAL;
        }
    }
}
