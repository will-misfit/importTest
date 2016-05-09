package com.misfit.ble.shine;

public class ShineLapCountingStatus {
    private byte mLicenseStatus;
    private byte mTrialCounter;
    private byte mLapCountingMode;
    private short mTimeout;

    public void setLicenseStatus(byte mLicenseStatus) {
        this.mLicenseStatus = mLicenseStatus;
    }

    public void setTrialCounter(byte mTrialCounter) {
        this.mTrialCounter = mTrialCounter;
    }

    public void setLapCountingMode(byte mLapCountingMode) {
        this.mLapCountingMode = mLapCountingMode;
    }

    public void setTimeout(short mTimeout) {
        this.mTimeout = mTimeout;
    }

    public byte getLicenseStatus() {
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
