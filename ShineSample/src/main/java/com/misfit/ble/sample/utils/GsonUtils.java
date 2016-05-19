package com.misfit.ble.sample.utils;

import com.google.gson.Gson;

public class GsonUtils {
    private final static Gson mGon;

    static {
        mGon = new Gson();
    }

    public static Gson getGon() {
        return mGon;
    }
}
