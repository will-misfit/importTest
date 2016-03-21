package com.misfit.syncsdk.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.misfit.syncsdk.model.BaseResponse;
import com.misfit.syncsdk.model.MetaMessage;
import com.misfit.syncsdk.utils.GeneralUtils;
import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogSession;

import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * network request library via retrofit2
 * */
public class APIClient {
    private static Retrofit retrofit;

    private static class APIClientHolder {
        private final static APIClient INSTANCE = new APIClient();
    }

    public static APIClient getInstance() {
        return APIClientHolder.INSTANCE;
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

    public interface FirmwareAPI {
        // TODO: add header
        @GET("shine_firmwares/get_latest")
        Call<FirmwareResponse> getLatestFirmwareVersion(@Query("deviceModel") String modelName);
    }

    class FirmwareResponse {

        @Expose
        @SerializedName("meta")
        public MetaMessage metaMessage;

        @Expose
        @SerializedName("version_number")
        public String versionNumber;

        @Expose
        @SerializedName("checksum")
        public String checkSum;

        @Expose
        @SerializedName("download_url")
        public String downloadUrl;

        @Expose
        @SerializedName("support_commands")
        public LinkedHashMap<String, Integer> supportCommands;

        @Expose
        @SerializedName("change_log")
        public String changeLog;

        @Expose
        @SerializedName("device_model")
        public String modelNumber;
    }

    public LogAPI getLogAPI() {
        return retrofit.create(LogAPI.class);
    }
}
