package com.misfit.syncsdk.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * provide utility methods for volley http request process.
 * some methods are moved from .prometheus.api.core.PrometheusApi
 * some variables/methods are moved form .prometheus.common.utils.PrometheusUtils
 */
public class VolleyRequestUtils {

    private static VolleyRequestUtils instance;

    private RequestQueue volleyQueue;
    public Gson gson;
    private GsonBuilder gsonBuilder;

    // TODO: API_Server is varied for staging and production, it will be moved to build config later
    protected static String API_SERVER_V8_STAGING = "https://api.int.misfit.com/shine/v8/";

    protected VolleyRequestUtils(@NonNull Context context) {
        this.volleyQueue = Volley.newRequestQueue(context);
        this.gsonBuilder = new GsonBuilder();
        this.gson  = gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .excludeFieldsWithoutExposeAnnotation().create();
    }

    public static VolleyRequestUtils getInstance() {
        if (instance == null) {
            instance = new VolleyRequestUtils(ContextManager.getInstance().getContext());
        }
        return instance;
    }

    public String buildUrl(String paramString, Properties paramProperties) {
        Properties localProperties = new Properties();
        if (paramProperties != null)
            localProperties.putAll(paramProperties);
        Uri.Builder localBuilder = Uri.parse(
                API_SERVER_V8_STAGING + paramString).buildUpon();
        Iterator<Entry<Object, Object>> localIterator = localProperties
                .entrySet().iterator();
        while (localIterator.hasNext()) {
            Entry<Object, Object> localEntry = localIterator.next();
            localBuilder.appendQueryParameter(localEntry.getKey().toString(),
                    localEntry.getValue().toString());
        }
        String result = localBuilder.build().toString();
        return result;
    }

    public RequestQueue getRequestQueue() {
        return this.volleyQueue;
    }
}
