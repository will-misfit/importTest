package com.misfit.syncsdk.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * copy from .promethues.model.MetaMessage
 */
public class MetaMessage {
    @Expose
    @SerializedName("code") 
    private int code;

    @Expose
    @SerializedName("debug") 
    private String debug;

    @Expose
    @SerializedName("msg")
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
