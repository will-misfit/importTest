package com.misfit.ble.shine;

import com.misfit.ble.setting.lapCounting.LapCountingLicenseStatus;

public class ShineLapCountingStatus {
    private LapCountingLicenseStatus mLicenseStatus;
    private byte mTrialCounter;
    private byte mLapCountingMode;
    private short mTimeout;

    public ShineLapCountingStatus(LapCountingLicenseStatus mLicenseStatus, byte mTrialCounter, byte mLapCountingMode, short mTimeout) {
        this.mLicenseStatus = mLicenseStatus;
        this.mTrialCounter = mTrialCounter;
        this.mLapCountingMode = mLapCountingMode;
        this.mTimeout = mTimeout;
    }

    public LapCountingLicenseStatus getLicenseStatus() {
        return mLicenseStatus;
    }

    public byte getTrialCounter() {
        return mTrialCounter;
    }

    public byte getLapCountingMode() {
        return mLapCountingMode;
    }

    public short getTimeout() {
        return mTimeout;
    }

    @Override
    public String toString() {
        return  "\n" + "LicenseStatus: " + mLicenseStatus + "\n"
                + "TrialCounter: " + mTrialCounter + "\n"
                + "LapCountingMode: " + mLapCountingMode + "\n"
                + "Timeout: " + mTimeout;
    }
}
