package com.misfit.syncsdk.log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Will Hou on 1/29/16.
 */
public class LogEvent {
    @Expose
    @SerializedName("sessionId")
    String sessionId;

    @Expose
    @SerializedName("id")
    int id;

    @Expose
    @SerializedName("eventName")
    String eventName;

    @Expose
    @SerializedName("seq")
    int sequence;

    @Expose
    @SerializedName("parameter")
    private String mParameter;

    @Expose
    @SerializedName("timestamp")
    private long mStartTime;

    @Expose
    @SerializedName("mResultCode")
    private int mResultCode;

    @Expose
    @SerializedName("result")
    private String mResultMsg;

    @Expose
    @SerializedName("duration")
    private long mDuration;

    public LogEvent(int id, String eventName) {
        this.id = id;
        this.eventName = eventName;
    }

    public LogEvent start(String parameter) {
        mParameter = parameter;
        mStartTime = System.currentTimeMillis();
        return this;
    }

    public LogEvent end(int resultCode, String resultMsg) {
        mDuration = System.currentTimeMillis() - mStartTime;
        mResultCode = resultCode;
        mResultMsg = resultMsg;
        return this;
    }
}
