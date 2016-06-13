package com.misfit.ble.shine;

import com.misfit.ble.setting.lapCounting.LapCountingLicenseStatus;

public class ShineLapCountingStatus {
    private LapCountingLicenseStatus mLicenseStatus;
    private byte mTrialCounter;
    private byte mLapCountingMode;
    private short mTimeout;
    private byte mMaxTrialNumber;

    public ShineLapCountingStatus(LapCountingLicenseStatus mLicenseStatus, byte mTrialCounter,
                                  byte mLapCountingMode, short mTimeout, byte mMaxTrialNumber) {
        this.mLicenseStatus = mLicenseStatus;
        this.mTrialCounter = mTrialCounter;
        this.mLapCountingMode = mLapCountingMode;
        this.mTimeout = mTimeout;
        this.mMaxTrialNumber = mMaxTrialNumber;
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

    public byte getMaxTrialNumber() {
        return mMaxTrialNumber;
    }
    @Override
    public String toString() {
        return  "\n" + "LicenseStatus: " + mLicenseStatus + "\n"
                + "TrialCounter: " + mTrialCounter + "\n"
                + "LapCountingMode: " + mLapCountingMode + "\n"
                + "Timeout: " + mTimeout + "\n"
                + "MaxTrialNumber " + mMaxTrialNumber ;
    }
}
