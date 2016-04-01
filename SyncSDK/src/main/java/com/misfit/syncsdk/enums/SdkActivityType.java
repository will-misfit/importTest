package com.misfit.syncsdk.enums;

import android.support.annotation.IntDef;

import com.misfit.cloud.algorithm.models.ActivityTypeShine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * a data model to reflect ActivityTypeShine in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkActivityType {

    public final static int UNKNOWN = 0;
    public final static int RUNNING = 1;
    public final static int CYCLING = 2;
    public final static int SWIMMING = 3;
    public final static int WALKING = 4;
    public final static int TENNIS = 5;
    public final static int BASKETBALL = 6;
    public final static int FOOTBALL = 7;
    public final static int YOGA = 8;
    public final static int DANCING = 9;
    public static final int WEIGHT = 10;
    public static final int DRIVE = 11;
    public static final int SLEEPING = 100; // special one
    public static final int GAP = 1000; // special one
    public static final int OTHER = 2000; // other third part type

    @IntDef({UNKNOWN, RUNNING, CYCLING, SWIMMING, WALKING, TENNIS, BASKETBALL, FOOTBALL, YOGA, DANCING, WEIGHT, DRIVE, SLEEPING, GAP, OTHER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActivityType {
    }

    private SdkActivityType(){}

    public static String getActivityTypeString(int sdkActivityTypeInt) {
        switch (sdkActivityTypeInt) {
            case RUNNING:
                return "Running";
            case CYCLING:
                return "Cycling";
            case SWIMMING:
                return "Swimming";
            case WALKING:
                return "Walking";
            case TENNIS:
                return "Tennis";
            case BASKETBALL:
                return "Basketball";
            case FOOTBALL:
                return "Football";
            case YOGA:
                return "Yoga";
            case DANCING:
                return "Dancing";
            case SLEEPING:
                return "Sleeping";
            case UNKNOWN:
            default:
                return "Unknown";
        }
    }

    /**
     * convert ActivityTypeShine in algorithm namespace to the one in local namespace
     * */
    public static int getActivityTypeShine(ActivityTypeShine activityTypeShine) {
        int result = UNKNOWN;
        if (activityTypeShine == ActivityTypeShine.WALKING_TYPE) {
            result = WALKING;
        } else if (activityTypeShine == ActivityTypeShine.RUNNING_TYPE) {
            result = RUNNING;
        } else if (activityTypeShine == ActivityTypeShine.CYCLING_TYPE) {
            result = CYCLING;
        } else if (activityTypeShine == ActivityTypeShine.SLEEP_TYPE) {
            result = SLEEPING;
        } else if (activityTypeShine == ActivityTypeShine.SWIMMING_TYPE) {
            result = SWIMMING;
        } else if (activityTypeShine == ActivityTypeShine.BASKETBALL_TYPE) {
            result = BASKETBALL;
        } else if (activityTypeShine == ActivityTypeShine.FOOTBALL_TYPE) {
            result = FOOTBALL;
        } else if (activityTypeShine == ActivityTypeShine.TENNIS_TYPE) {
            result = TENNIS;
        } else if (activityTypeShine == ActivityTypeShine.DANCING_TYPE) {
            result = DANCING;
        } else if (activityTypeShine == ActivityTypeShine.YOGA_TYPE) {
            result = YOGA;
        }
        return result;
    }

    /**
     * convert to ActivityTypeShine in algorithm namespace
     * */
    public static ActivityTypeShine convert2ActivityTypeShine(int sdkActivityTypeInt) {
        if (sdkActivityTypeInt == SdkActivityType.WALKING) {
            return ActivityTypeShine.WALKING_TYPE;
        } else if (sdkActivityTypeInt == SdkActivityType.RUNNING) {
            return ActivityTypeShine.RUNNING_TYPE;
        } else if (sdkActivityTypeInt == SdkActivityType.CYCLING) {
            return ActivityTypeShine.CYCLING_TYPE;
        } else if (sdkActivityTypeInt == SdkActivityType.SLEEPING) {
            return ActivityTypeShine.SLEEP_TYPE;
        } else if (sdkActivityTypeInt == SdkActivityType.SWIMMING) {
            return ActivityTypeShine.SWIMMING_TYPE;
        } else if (sdkActivityTypeInt == SdkActivityType.BASKETBALL) {
            return ActivityTypeShine.BASKETBALL_TYPE;
        } else if (sdkActivityTypeInt == SdkActivityType.FOOTBALL) {
            return ActivityTypeShine.FOOTBALL_TYPE;
        } else if (sdkActivityTypeInt == SdkActivityType.TENNIS) {
            return ActivityTypeShine.TENNIS_TYPE;
        } else if (sdkActivityTypeInt == SdkActivityType.DANCING) {
            return ActivityTypeShine.DANCING_TYPE;
        } else if (sdkActivityTypeInt == SdkActivityType.YOGA) {
            return ActivityTypeShine.YOGA_TYPE;
        } else {
            return ActivityTypeShine.UNKNOWN_TYPE;
        }
    }
}
