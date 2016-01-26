package com.misfit.syncsdk.model;


import com.misfit.cloud.algorithm.models.ActivityTypeShine;
import com.misfit.cloud.algorithm.models.SessionType;
import com.misfit.syncsdk.enums.SdkActivityType;
import com.misfit.syncsdk.enums.SdkSessionType;

/**
 * data model to reflect SessionShine, ActivitySessionShine, and GapSessionShine in algorithm library namespace
 * spawn class of com.misfitwearables.prometheus.model.ActivitySession
 */
public class SdkActivitySession {

    protected long mStartTime;

    protected int mDuration;

    private int mActivityTypeShine; // option is defined in SdkActivityType

    private int mSessionType;

    private int mPoints;

    private int mRawPoints;

    private double mCalories;

    private int mSteps;

    private double mDistance;

    private boolean mIsGapSession;

    private int mLaps; // accumulated in Speedo Shine device

    public SdkActivitySession() {
    }

    public void updateFrom(SdkActivitySession it) {
        this.mStartTime = it.mStartTime;
        this.mDuration = it.mDuration;
        this.mActivityTypeShine = it.mActivityTypeShine;
        this.mSessionType = it.mSessionType;
        this.mPoints = it.mPoints;
        this.mRawPoints = it.mRawPoints;
        this.mCalories = it.mCalories;
        this.mSteps = it.mSteps;
        this.mDistance = it.mDistance;
        this.mIsGapSession = it.mIsGapSession;
        this.mLaps = it.mLaps;
    }

    public long getStartTime() {
        return this.mStartTime;
    }

    public void setStartTime(long sTime) {
        this.mStartTime = sTime;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public void setDuration(int dura) {
        this.mDuration = dura;
    }

    public int getActivityType() {
        return this.mActivityTypeShine;
    }

    public void setActivityType(int aType) {
        this.mActivityTypeShine = aType;
    }

    public void setActivityType(ActivityTypeShine actTypeShine) {
        this.mActivityTypeShine = SdkActivityType.getActivityTypeShine(actTypeShine);
    }

    public int getSessionType() {
        return mSessionType;
    }

    public void setSessionType(int sType) {
        this.mSessionType = sType;
    }

    public void setSessionType(SessionType sessType) {
        this.mSessionType = SdkSessionType.getSessionType(sessType);
    }

    public int getPoint() {
        return this.mPoints;
    }

    public void setPoints(int pts) {
        this.mPoints = pts;
    }

    public int getRawPoints() {
        return this.mRawPoints;
    }

    public void setRawPoints(int rPoints) {
        this.mRawPoints = rPoints;
    }

    public double getCalories() {
        return this.mCalories;
    }

    public void setCalories(double cals) {
        this.mCalories = cals;
    }

    public int getSteps() {
        return mSteps;
    }

    public void setSteps(int mSteps) {
        this.mSteps = mSteps;
    }

    public double getDistance() {
        return mDistance;
    }

    public void setDistance(double mDistance) {
        this.mDistance = mDistance;
    }

    public boolean isGapSession() {
        return mIsGapSession;
    }

    public void setIsGapSession(boolean isGap) {
        this.mIsGapSession = isGap;
    }

    public int getLaps() {
        return mLaps;
    }

    public void setLaps(int lps) {
        this.mLaps = lps;
    }

    public static SdkActivitySession buildEmptyData(long taggedTimeStamp) {
        SdkActivitySession result = new SdkActivitySession();
        result.setStartTime(taggedTimeStamp);
        result.setActivityType(0);
        result.setDuration(0);
        result.setActivityType(0);
        result.setSessionType(0);
        result.setPoints(0);
        result.setRawPoints(0);
        result.setCalories(0);
        result.setSteps(0);
        result.setDistance(0);
        result.setIsGapSession(false);
        result.setLaps(0);
        return result;
    }
}
