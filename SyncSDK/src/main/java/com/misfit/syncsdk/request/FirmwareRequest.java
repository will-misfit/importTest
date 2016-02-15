package com.misfit.syncsdk.request;

import com.android.volley.toolbox.JsonRequest;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.misfit.syncsdk.model.FirmwareInfo;

import java.util.LinkedHashMap;

/**
 * to follow .prometheus.api.request.FirmwareRequest
 */
public class FirmwareRequest extends JsonObjectRequest<FirmwareRequest>{
    
    @Expose
    @SerializedName("version_number")
    public String versionNumber;

    @Expose
    @SerializedName("checksum")
    public String checksum;

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

    public FirmwareRequest(RequestListener<FirmwareRequest> paramRequestListener,
                           String modelNumber) {
        super(null, "shine_firmwares/get_latest?deviceModel=" + modelNumber, null,
                paramRequestListener);
    }

    public static FirmwareRequest getLatestRequest(
            RequestListener<FirmwareRequest> paramRequestListener, String modelNumber) {
        FirmwareRequest request = new FirmwareRequest(paramRequestListener, modelNumber);
        return request;
    }

    public FirmwareInfo exportFirmwareInfo() {
        return new FirmwareInfo(modelNumber, versionNumber, changeLog, checksum, downloadUrl);
    }

    protected void buildResult(Object result) {
        FirmwareRequest request = (FirmwareRequest) result;
        versionNumber   = request.versionNumber;
        checksum        = request.checksum;
        downloadUrl     = request.downloadUrl;
        supportCommands = request.supportCommands;
        changeLog       = request.changeLog;
        modelNumber     = request.modelNumber;
    }
}
