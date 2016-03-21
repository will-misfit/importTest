package com.misfit.syncsdk.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * utility class to support gson usage
 */
public class GsonUtils {

    private Gson mGson;

    private GsonBuilder mGsonBuilder;

    private static GsonUtils mInstance;

    private GsonUtils() {
        mGsonBuilder = new GsonBuilder();
        mGson = mGsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .excludeFieldsWithoutExposeAnnotation().create();
    }

    public static GsonUtils getInstance() {
        if (mInstance == null) {
            mInstance = new GsonUtils();
        }
        return mInstance;
    }

    public Gson getGson() {
        return mGson;
    }
}
