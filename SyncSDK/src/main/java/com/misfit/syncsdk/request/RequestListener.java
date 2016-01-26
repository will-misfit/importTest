package com.misfit.syncsdk.request;

import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;

/**
 * similar to .prometheus.api.core.RequestListener
 */
public interface RequestListener<T> extends Listener<T>, ErrorListener {
}
