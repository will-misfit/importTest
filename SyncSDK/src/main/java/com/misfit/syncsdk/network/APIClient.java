package com.misfit.syncsdk.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.misfit.syncsdk.model.BaseResponse;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogSession;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class APIClient {
    private static Retrofit retrofit;
    private static APIClient mInstance;

    public static APIClient getInstance() {
        if (mInstance == null) {
            mInstance = new APIClient();
        }
        return mInstance;
    }

    private APIClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://cloud-int.misfit.com/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public interface LogAPI {
        @Headers({"Content-Type: application/json"})
        @POST("log/sessions/{sessionId}/events")
        Call<BaseResponse> uploadEvents(@Path("sessionId") String sessionId, @Body List<LogEvent> logEvents);

        @Headers({"Content-Type: application/json"})
        @POST("log/sessions")
        Call<BaseResponse> uploadSession(@Body LogSession logSession);
    }

    public LogAPI getLogAPI() {
        return retrofit.create(LogAPI.class);
    }
}
