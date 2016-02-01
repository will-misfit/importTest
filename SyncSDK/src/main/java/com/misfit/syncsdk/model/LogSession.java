package com.misfit.syncsdk.model;

import android.os.Build;

import com.google.gson.annotations.SerializedName;
import com.misfit.syncsdk.BuildConfig;

import java.util.UUID;


public class LogSession {
    @SerializedName("id")
    public final String id = UUID.randomUUID().toString();

    @SerializedName("uid")
    public String uid = "53e93c91d096be352a05d75b";

    @SerializedName("serialNumber")
    public String serialNumber = "SH0AZ06WMG";

    @SerializedName("firmware")
    public String firmware = "SH0.1.3x";

    @SerializedName("os")
    public String os = "Android";

    @SerializedName("osVersion")
    public String osVersion = Build.VERSION.RELEASE;

    @SerializedName("deviceModel")
    public String deviceModel = Build.MODEL;

    @SerializedName("sdkVersion")
    public String sdkVersion = BuildConfig.VERSION_NAME;

    @SerializedName("appVersion")
    public String appVersion = "2.6.0";

    @SerializedName("calculationLibVersion")
    public String calculationLibVersion = "v1.7.0.0";

    @SerializedName("syncMode")
    public int syncMode = 1;

    @SerializedName("logVersion")
    public int logVersion = 2;

    @SerializedName("activityPoint")
    public long activityPoint = 1000;

    @SerializedName("postSyncActivityPoint")
    public long postSyncActivityPoint = 1500;

    @SerializedName("timezone")
    public long timezone = 3600;

    @SerializedName("postSyncTimezone")
    public long postSyncTimezone = 4800;

    @SerializedName("goal")
    public long goal = 1500;

    @SerializedName("postSyncGoal")
    public long postSyncGoal = 1600;

    @SerializedName("clockState")
    public int clockState = 12;

    @SerializedName("postClockState")
    public int postClockState = 22;

    @SerializedName("retries")
    public int retries = 3;

    @SerializedName("lastAction")
    public String lastAction = "connect";

    @SerializedName("failureReason")
    public int failureReason = 13;

    @SerializedName("deviceIdentifier")
    public String deviceIdentifier = "";

    @SerializedName("battery")
    public int battery = -1;

    @SerializedName("activityTaggingState")
    public int activityTaggingState = -1;

    @SerializedName("alarm")
    public String alarm = "";

    @SerializedName("inactiveNotificationState")
    public boolean inactiveNotificationState = false;

    @SerializedName("callNotificationState")
    public boolean callNotificationState = true;

    @SerializedName("userInfo")
    public String userInfo = "";

    @SerializedName("isDataloss")
    public boolean isDataloss = false;

    @SerializedName("isSuccess")
    public boolean isSuccess = false;


}
