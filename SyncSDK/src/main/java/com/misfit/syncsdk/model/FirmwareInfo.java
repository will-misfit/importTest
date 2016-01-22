package com.misfit.syncsdk.model;

/**
 * Class to present information of Firmware
 */
public class FirmwareInfo {

    private String mModelName;

    private String mVersionNumber;
    private String mChangeLog;
    private String mChecksum;
    private String mDownloadUrl;

    public FirmwareInfo(String modelName,
                        String versionNumber,
                        String changeLog,
                        String checksum,
                        String downloadUrl) {
        mModelName = modelName;
        mVersionNumber = versionNumber;
        mChangeLog = changeLog;
        mChecksum = checksum;
        mDownloadUrl = downloadUrl;
    }

    public String getModelName() {
        return mModelName;
    }

    public void setModelName(String modelName) {
        mModelName = modelName;
    }

    public String getVersionNumber() {
        return mVersionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.mVersionNumber = versionNumber;
    }

    public String getChangeLog() {
        return mChangeLog;
    }

    public void setChangeLog(String changeLog) {
        this.mChangeLog = changeLog;
    }

    public String getChecksum() {
        return mChecksum;
    }

    public void setChecksum(String checksum) {
        this.mChecksum = checksum;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.mDownloadUrl = downloadUrl;
    }
}
