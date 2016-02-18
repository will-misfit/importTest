package com.misfit.syncsdk.request;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.enums.HttpStatus;
import com.misfit.syncsdk.model.MetaMessage;
import com.misfit.syncsdk.utils.CheckUtils;
import com.misfit.syncsdk.utils.ContextUtils;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.utils.SdkConstants;
import com.misfit.syncsdk.utils.VolleyRequestUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * follow .prometheus.api.core.PrometheusJsonObjectRequest
 * it uses gson to do the json format
 */
public abstract class JsonObjectRequest<T> extends JsonRequest<T> {

    private static final String TAG = "Request_JsonObjectReq";
    private static final String PLATFORM = "android";
    private static final String ACCEPT_FORMAT = "application/json";

    private static final int DEFAULT_MAX_RETRIES_FOR_GET_REQUEST = 2;

    private static final int DEFAULT_TIMEOUT_FOR_POST_REQUEST_IN_MS = 5000; // 5 seconds
    
    public int statusCode;
    protected int method = Method.GET;

    public String customData;

    public String postJson;

    public String callId;

    @Expose
    @SerializedName("meta")
    public MetaMessage metaMessage;

    public JsonObjectRequest(JSONObject jsonRequest,
                             String urlPhrase,
                             Properties parametersForGet,
                             RequestListener<T> listener) {
        super(jsonRequest == null ? Method.GET : Method.POST,  /* method */
              VolleyRequestUtils.getInstance().buildUrl(urlPhrase, parametersForGet),  /* url */
              (jsonRequest == null) ? null : jsonRequest.toString(),  /* request body */
              listener == null ? ((RequestListener<T>) generateEmptyListener()) : listener, /* listener */
              listener == null ? ((RequestListener<T>) generateEmptyListener()) : listener  /* error listener */);
        if (jsonRequest != null) {
            Log.d(TAG, jsonRequest.toString());
        }
        method = (jsonRequest == null ? Method.GET : Method.POST);
        callId = String.valueOf(GeneralUtils.getInstance().randomInt());

        setup();
    }

    private void setup() {
        setRetryPolicy(new DefaultRetryPolicy(getConnectionTimeoutMs(), getMaxRetries(), 0));
        // Only allow cache for GET request.
        setShouldCache(method == Method.GET);
    }

    /**
     * Get the connect and socket timeout in milli seconds
     */
    protected int getConnectionTimeoutMs() {
        return method == Method.GET ?
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS : DEFAULT_TIMEOUT_FOR_POST_REQUEST_IN_MS;
    }

    /**
     * Get the max retries if request failed duo to timeout.
     */
    protected int getMaxRetries() {
        return method == Method.GET ? DEFAULT_MAX_RETRIES_FOR_GET_REQUEST : 0;
    }

    private static <T> RequestListener<T> generateEmptyListener() {
        return new RequestListener<T>() {
            @Override
            public void onResponse(T t) {}

            @Override
            public void onErrorResponse(VolleyError volleyError) {}
        };
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            Log.d(TAG, String.format("%d:%s", response.statusCode, jsonString));

            if (!CheckUtils.isStringEmpty(jsonString)) {
                Object result = VolleyRequestUtils.getInstance().gson.fromJson(jsonString, super.getClass());
                metaMessage = ((JsonObjectRequest) result).metaMessage;
                buildResult(result);
            }

            statusCode = response.statusCode;

            @SuppressWarnings("unchecked")
            Response<T> localResponse = (Response<T>) Response.success(this, HttpHeaderParser.parseCacheHeaders(response));

            return localResponse;
        } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
            return Response.error(new ParseError(response));
        }
    }

    private String getRequestTag() {
        return this.getClass().getSimpleName();
    }

    public void execute() {
        setTag(getRequestTag());
        VolleyRequestUtils.getInstance().getRequestQueue().add(this);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        super.getHeaders();

        Map<String, String> items = new HashMap<>();
        items.put("Accept", ACCEPT_FORMAT);
        items.put("api_key", SdkConstants.HTTP_REQUEST_API_KEY);
        items.put("locale", GeneralUtils.reloadCurrentLocale());
        items.put("platform", PLATFORM);
        items.put("device_type", "all");
        // items.put("User-Agent", PrometheusBuild.USER_AGENT_INFO)
        items.put("call_id", callId);

        /**
         TODO: Cloud team supports two kinds of AuthToken:
         one is designed for App, specified with user account, it varies with user signIn/signOut
         another is designed for SDK/library, specified with serial number.
         Currently only 1st kind of AuthToken is available, so use it temporarily.
         * */
        String authToken = ContextUtils.getInstance().getUserAuthToken();
        if (!CheckUtils.isStringEmpty(authToken)) {
            items.put("auth_token", authToken);
        }
        return items;
    }

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getStatusCode() {
        return statusCode;
    }

    protected abstract void buildResult(Object result);

    public boolean requestIsOK() {
        return (metaMessage != null) && (metaMessage.getCode() == HttpStatus.API_STATUS_OK);
    }

    public int returnRespResult() {
        if (metaMessage != null) {
            return metaMessage.getCode();
        } else {
            return HttpStatus.HTTP_STATUS_UNEXPECTED_SERVER_ERROR;
        }
    }
}
