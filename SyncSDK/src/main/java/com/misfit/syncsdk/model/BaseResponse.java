package com.misfit.syncsdk.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Will Hou on 1/29/16.
 */
public class BaseResponse {
    @Expose
    @SerializedName("code")
    public int code;
}
