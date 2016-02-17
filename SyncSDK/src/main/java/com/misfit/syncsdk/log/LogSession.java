package com.misfit.syncsdk.log;

import android.os.Build;
import android.provider.Settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.misfit.syncsdk.BuildConfig;
import com.misfit.syncsdk.enums.FailedReason;
import com.misfit.syncsdk.utils.ContextUtils;

import java.util.UUID;


//FIXME: how to know the int field was modified or not?
public class LogSession {
    private final static int NO_SYNC_MODE = Integer.MIN_VALUE;

    @Expose
    @SerializedName("id")
    private final String id = UUID.randomUUID().toString();

    @Expose
    @SerializedName("uid")
    private final String mUid;

    @Expose
    @SerializedName("serialNumber")
    private String mSerialNumber;

    /**
     * this field represents the current firmware in use.
     * for OTA, the new firmware version will be recorded in next sync session
     * */
    @Expose
    @SerializedName("firmware")
    private String mFirmwareVersion;

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
    @SerializedName("syncMode")
    private int mSyncMode = -1;

    @Expose
    @SerializedName("activityPoint")
    private long mPreActivityPoint = -1;

    @Expose
    @SerializedName("postSyncActivityPoint")
    private long mPostSyncActivityPoint = -1;

    @Expose
    @SerializedName("timezone")
    private long mPreTimezone = -1;

    @Expose
    @SerializedName("postSyncTimezone")
    private long mPostSyncTimezone = -1;

    @Expose
    @SerializedName("goal")
    private long mPreGoal = -1;

    @Expose
    @SerializedName("postSyncGoal")
    private long mPostSyncGoal = -1;

    @Expose
    @SerializedName("clockState")
    private int mPreClockState = -1;

    @Expose
    @SerializedName("postClockState")
    private int mPostClockState = -1;

    @Expose
    @SerializedName("retries")
    private int mRetries = -1;

    @Expose
    @SerializedName("failureReason")
    private int mFailureReason = FailedReason.DEFAULT;

    @Expose
    @SerializedName("deviceIdentifier")
    private final String mDeviceIdentifier;

    @Expose
    @SerializedName("battery")
    private int mBattery = -1;

    @Expose
    @SerializedName("activityTaggingState")
    private int mActivityTaggingState = -1;

    @Expose
    @SerializedName("alarm")
    private String mAlarmParmeters = "";

    @Expose
    @SerializedName("inactiveNotificationState")
    private boolean inactiveNotificationState = false;

    @Expose
    @SerializedName("callNotificationState")
    private boolean callNotificationState = false;

    @Expose
    @SerializedName("isDataloss")
    private boolean isDataloss = false;

    @Expose
    @SerializedName("isSuccess")
    private boolean isSuccess = false;

    private int currEventSequence;

    public LogSession(String clientName, String clientVersion, String uid) {
        this(NO_SYNC_MODE, clientName, clientVersion, uid);
    }

    public LogSession(int syncMode, String clientName, String clientVersion, String uid) {
        mDeviceIdentifier = Settings.Secure.getString(ContextUtils.getInstance().getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        mSyncMode = syncMode;
        mUid = uid;
        currEventSequence = 0;
    }

    public String getId() {
        return id;
    }

    public void setSerialNumber(String serialNumber) {
        mSerialNumber = serialNumber;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        mFirmwareVersion = firmwareVersion;
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
        mAlarmParmeters = alarm;
    }

    public void setInactiveNotificationState(boolean inactiveNotificationState) {
        this.inactiveNotificationState = inactiveNotificationState;
    }

    public void setCallNotificationState(boolean callNotificationState) {
        this.callNotificationState = callNotificationState;
    }

    public void setDataloss(boolean dataloss) {
        isDataloss = dataloss;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public void save() {
        LogManager.getInstance().saveSession(this);
    }

    public void appendEvent(LogEvent event) {
        currEventSequence++;
        event.sequence = currEventSequence;
        event.sessionId = id;
        LogManager.getInstance().appendEvent(event);
    }

    public void upload() {
        LogManager.getInstance().uploadAllLog();
    }

}
