package com.misfit.syncsdk.utils;

import android.content.Context;

/**
 * Class to save the Context object from App
 */
public class ContextManager {

    private Context mContext;

    private String mUserAuthToken;

    private static ContextManager mInstance;

    private ContextManager() {
    }

    public static ContextManager getInstance() {
        if (mInstance == null) {
            mInstance = new ContextManager();
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
