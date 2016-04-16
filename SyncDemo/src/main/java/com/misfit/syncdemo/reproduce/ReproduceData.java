package com.misfit.syncdemo.reproduce;

import com.google.gson.annotations.SerializedName;

public class ReproduceData {
    @SerializedName("sync_info")
    SyncInfo syncInfo;

    @SerializedName("raw_data")
    GetFileResponse[] getFileResponses;
}
