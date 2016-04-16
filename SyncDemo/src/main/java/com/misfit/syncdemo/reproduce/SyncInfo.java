package com.misfit.syncdemo.reproduce;

import com.google.gson.annotations.SerializedName;

public class SyncInfo {
    @SerializedName("user_id")
    public String uid;
    @SerializedName("serial_number")
    public String serialNumber;
    @SerializedName("sync_time")
    public long syncTime;
    @SerializedName("last_sync_time")
    public long lastSyncTime;

    @Override
    public String toString() {
        return "SyncInfo{" +
                "uid='" + uid + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", syncTime=" + syncTime +
                ", lastSyncTime=" + lastSyncTime +
                '}';
    }
}
