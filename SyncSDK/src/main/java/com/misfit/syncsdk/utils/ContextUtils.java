package com.misfit.syncsdk.utils;

import android.content.Context;

/**
 * Class to save the Context object from App
 */
public class ContextUtils {

    private Context mContext;

    private String mUserAuthToken;

    private static ContextUtils mInstance;

    private ContextUtils() {
    }

    public static ContextUtils getInstance() {
        if (mInstance == null) {
            mInstance = new ContextUtils();
        }
        return mInstance;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setUserAuthToken(String authToken) {
        this.mUserAuthToken = authToken;
    }

    public String getUserAuthToken() {
        return this.mUserAuthToken;
    }
}
