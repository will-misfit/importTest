package com.misfit.syncsdk.model;


import com.misfit.cloud.algorithm.models.ActivityTypeShine;
import com.misfit.cloud.algorithm.models.SessionType;

/**
 * data model to reflect SessionShine, ActivitySessionShine, and GapSessionShine in algorithm library namespace
 * spawn class of com.misfitwearables.prometheus.model.ActivitySession
 */
public class SdkActivitySession {

    protected long startTime;

    protected int duration;

    private int activityTypeShine; // option is defined in SdkActivityType

    private int sessionType;

    private int points;

    private int rawPoints;

    private double calories;

    private int steps;

    private double distance;

    private boolean isGapSession;

    private int laps; // accumulated in Speedo Shine device

    /* below variables for lap counting which are saved in database of flagship app as Settings*/
    /*
    private boolean isLapCounting;
    private int poolLength;
    private int poolLengthUnit;
    */

    public SdkActivitySession() {
    }

    public void updateFrom(SdkActivitySession it) {
        this.startTime = it.startTime;
        this.duration = it.duration;
        this.activityTypeShine = it.activityTypeShine;
        this.sessionType = it.sessionType;
        this.points = it.points;
        this.rawPoints = it.rawPoints;
        this.calories = it.calories;
        this.steps = it.steps;
        this.distance = it.distance;
        this.isGapSession = it.isGapSession;
        this.laps = it.laps;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long sTime) {
        this.startTime = sTime;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int dura) {
        this.duration = dura;
    }

    public int getActivityType() {
        return this.activityTypeShine;
    }

    public void setActivityType(int aType) {
        this.activityTypeShine = aType;
    }

    public void setActivityType(ActivityTypeShine actTypeShine) {
        this.activityTypeShine = SdkActivityType.getActivityTypeShine(actTypeShine);
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sType) {
        this.sessionType = sType;
    }

    public void setSessionType(SessionType sessType) {
        this.sessionType = SdkSessionType.getSessionType(sessType);
    }

    public int getPoint() {
        return this.points;
    }

    public void setPoints(int pts) {
        this.points = pts;
    }

    public int getRawPoints() {
        return this.rawPoints;
    }

    public void setRawPoints(int rPoints) {
        this.rawPoints = rPoints;
    }

    public double getCalories() {
        return this.calories;
    }

    public void setCalories(double cals) {
        this.calories = cals;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isGapSession() {
        return isGapSession;
    }

    public void setIsGapSession(boolean isGap) {
        this.isGapSession = isGap;
    }

    public int getLaps() {
        return laps;
    }

    public void setLaps(int lps) {
        this.laps = lps;
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
