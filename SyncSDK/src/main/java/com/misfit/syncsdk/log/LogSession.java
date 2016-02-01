package com.misfit.syncsdk.log;

import android.os.Build;
import android.provider.Settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.misfit.syncsdk.BuildConfig;
import com.misfit.syncsdk.utils.ContextUtils;

import java.util.UUID;


public class LogSession {
    private final static int NO_SYNC_MODE = Integer.MIN_VALUE;

    @Expose
    @SerializedName("id")
    private final String id = UUID.randomUUID().toString();

    @Expose
    @SerializedName("uid")
    private String mUid;

    @Expose
    @SerializedName("serialNumber")
    private String mSerialNumber;

    @Expose
    @SerializedName("firmware")
    private String mFirmware;

    @Expose
    @SerializedName("os")
    private final static String mOs = "android";

    @Expose
    @SerializedName("osVersion")
    private final String mOsVersion = Build.VERSION.RELEASE;

    @Expose
    @SerializedName("deviceModel")
    private final String mDeviceModel = Build.MODEL;

    @Expose
    @SerializedName("sdkVersion")
    private final String mSdkVersion = BuildConfig.BUILD_TYPE + BuildConfig.VERSION_NAME;

    @Expose
    @SerializedName("calculationLibVersion")
    private final String mCalculationLibVersion = BuildConfig.ALGORITHM_LIB_VERSION;

    @Expose
    @SerializedName("appVersion")
    private String mClientVersion;

    private String mClientName;

    @Expose
    @SerializedName("syncMode")
    private int mSyncMode;

    @Expose
    @SerializedName("activityPoint")
    private long mPreActivityPoint;

    @Expose
    @SerializedName("postSyncActivityPoint")
    private long mPostSyncActivityPoint;

    @Expose
    @SerializedName("timezone")
    private long mPreTimezone;

    @Expose
    @SerializedName("postSyncTimezone")
    private long mPostSyncTimezone;

    @Expose
    @SerializedName("goal")
    private long mPreGoal;

    @Expose
    @SerializedName("postSyncGoal")
    private long mPostSyncGoal;

    @Expose
    @SerializedName("clockState")
    private int mPreClockState;

    @Expose
    @SerializedName("postClockState")
    private int mPostClockState;

    @Expose
    @SerializedName("retries")
    private int mRetries;

    @Expose
    @SerializedName("failureReason")
    private int mFailureReason = 13;

    @Expose
    @SerializedName("deviceIdentifier")
    private final String mDeviceIdentifier;

    @Expose
    @SerializedName("battery")
    private int mBattery;

    @Expose
    @SerializedName("activityTaggingState")
    private int mActivityTaggingState;

    @Expose
    @SerializedName("alarm")
    private String mAlarm;

    @Expose
    @SerializedName("inactiveNotificationState")
    private boolean inactiveNotificationState = false;

    @Expose
    @SerializedName("callNotificationState")
    private boolean callNotificationState = true;

    @Expose
    @SerializedName("userInfo")
    private String userInfo = "";

    @Expose
    @SerializedName("isDataloss")
    private boolean isDataloss = false;

    @Expose
    @SerializedName("isSuccess")
    private boolean isSuccess = false;

    private int currSeq;

    public LogSession(String clientName, String clientVersion, String uid) {
        this(NO_SYNC_MODE, clientName, clientVersion, uid);
    }

    public LogSession(int syncMode,String clientName, String clientVersion, String uid) {
        mDeviceIdentifier = Settings.Secure.getString(ContextUtils.getInstance().getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        mSyncMode = syncMode;
        mClientName = clientName;
        mClientVersion = clientVersion;
        mUid = uid;
        currSeq = 0;
    }

    public String getId() {
        return id;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public void setSerialNumber(String serialNumber) {
        mSerialNumber = serialNumber;
    }

    public void setFirmware(String firmware) {
        mFirmware = firmware;
    }

    public void setAppVersion(String appVersion) {
        mClientVersion = appVersion;
    }

    public void setSyncMode(int syncMode) {
        mSyncMode = syncMode;
    }

    public void setPreActivityPoint(long activityPoint) {
        mPreActivityPoint = activityPoint;
    }

    public void setPostSyncActivityPoint(long postSyncActivityPoint) {
        mPostSyncActivityPoint = postSyncActivityPoint;
    }

    public void setPreTimezone(long timezone) {
        mPreTimezone = timezone;
    }

    public void setPostSyncTimezone(long postSyncTimezone) {
        mPostSyncTimezone = postSyncTimezone;
    }

    public void setPreGoal(long goal) {
        mPreGoal = goal;
    }

    public void setPostSyncGoal(long postSyncGoal) {
        mPostSyncGoal = postSyncGoal;
    }

    public void setPreClockState(int clockState) {
        mPreClockState = clockState;
    }

    public void setPostClockState(int postClockState) {
        mPostClockState = postClockState;
    }

    public void setRetries(int retries) {
        mRetries = retries;
    }

    public void setFailureReason(int failureReason) {
        mFailureReason = failureReason;
    }

    public void setBattery(int battery) {
        mBattery = battery;
    }

    public void setActivityTaggingState(int activityTaggingState) {
        mActivityTaggingState = activityTaggingState;
    }

    public void setAlarm(String alarm) {
        mAlarm = alarm;
    }

    public void setInactiveNotificationState(boolean inactiveNotificationState) {
        this.inactiveNotificationState = inactiveNotificationState;
    }

    public void setCallNotificationState(boolean callNotificationState) {
        this.callNotificationState = callNotificationState;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public void setDataloss(boolean dataloss) {
        isDataloss = dataloss;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }


    public void appendEvent(LogEvent event) {
        currSeq++;
        event.sequence = currSeq;
        event.sessionId = id;
        LogManager.getInstance().appendEvent(id, event);
    }

    public void upload() {
        LogManager.getInstance().uploadLog(id);
    }

}
