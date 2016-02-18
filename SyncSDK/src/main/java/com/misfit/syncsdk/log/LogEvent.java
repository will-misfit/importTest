package com.misfit.syncsdk.log;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Will Hou on 1/29/16.
 */
public class LogEvent {

    public final static int RESULT_SUCCESS = 1;
    public final static int RESULT_FAILURE = 2;
    public final static int RESULT_TIMEOUT = 3;
    public final static int RESULT_OTHER = 4;

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
    @Nullable
    private String mParameter;

    @Expose
    @SerializedName("timestamp")
    private long mStartTime;

    @Expose
    @SerializedName("mResultCode")
    private int mResultCode;

    @Expose
    @SerializedName("result")
    @Nullable
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

    public long getDuration() {
        return mDuration;
    }
}
