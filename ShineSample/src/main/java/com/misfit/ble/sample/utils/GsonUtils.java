package com.misfit.ble.sample.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {
    private final static Gson mGon;

    static {
        mGon = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public static Gson getGon() {
        return mGon;
    }

}
