package com.misfit.syncsdk.model;

import com.misfit.cloud.algorithm.models.ActivityTypeShine;

/**
 * a data model to reflect ActivityTypeShine in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkActivityType {

    public final static int UNKNOWN_TYPE = 0;
    public final static int RUNNING_TYPE = 1;
    public final static int CYCLING_TYPE = 2;
    public final static int SWIMMING_TYPE = 3;
    public final static int WALKING_TYPE = 4;
    public final static int TENNIS_TYPE = 5;
    public final static int BASKETBALL_TYPE = 6;
    public final static int FOOTBALL_TYPE = 7;
    public final static int YOGA_TYPE = 8;
    public final static int DANCING_TYPE = 9;
    public final static int SLEEP_TYPE = 10;

    private SdkActivityType(){}

    /**
     * convert ActivityTypeShine in algorithm namespace to the one in local namespace
     * */
    public static int getActivityTypeShine(ActivityTypeShine activityTypeShine) {
        int result = UNKNOWN_TYPE;
        if (activityTypeShine == ActivityTypeShine.WALKING_TYPE) {
            result = WALKING_TYPE;
        } else if (activityTypeShine == ActivityTypeShine.RUNNING_TYPE) {
            result = RUNNING_TYPE;
        } else if (activityTypeShine == ActivityTypeShine.CYCLING_TYPE) {
            result = CYCLING_TYPE;
        } else if (activityTypeShine == ActivityTypeShine.SLEEP_TYPE) {
            result = SLEEP_TYPE;
        } else if (activityTypeShine == ActivityTypeShine.SWIMMING_TYPE) {
            result = SWIMMING_TYPE;
        } else if (activityTypeShine == ActivityTypeShine.BASKETBALL_TYPE) {
            result = BASKETBALL_TYPE;
        } else if (activityTypeShine == ActivityTypeShine.FOOTBALL_TYPE) {
            result = FOOTBALL_TYPE;
        } else if (activityTypeShine == ActivityTypeShine.TENNIS_TYPE) {
            result = TENNIS_TYPE;
        } else if (activityTypeShine == ActivityTypeShine.DANCING_TYPE) {
            result = DANCING_TYPE;
        } else if (activityTypeShine == ActivityTypeShine.YOGA_TYPE) {
            result = YOGA_TYPE;
        }
        return result;
    }

    public static ActivityTypeShine convert2ActivityTypeShine(int misfitActivityTypeValue) {
        if (misfitActivityTypeValue == SdkActivityType.WALKING_TYPE) {
            return ActivityTypeShine.WALKING_TYPE;
        } else if (misfitActivityTypeValue == SdkActivityType.RUNNING_TYPE) {
            return ActivityTypeShine.RUNNING_TYPE;
        } else if (misfitActivityTypeValue == SdkActivityType.CYCLING_TYPE) {
            return ActivityTypeShine.CYCLING_TYPE;
        } else if (misfitActivityTypeValue == SdkActivityType.SLEEP_TYPE) {
            return ActivityTypeShine.SLEEP_TYPE;
        } else if (misfitActivityTypeValue == SdkActivityType.SWIMMING_TYPE) {
            return ActivityTypeShine.SWIMMING_TYPE;
        } else if (misfitActivityTypeValue == SdkActivityType.BASKETBALL_TYPE) {
            return ActivityTypeShine.BASKETBALL_TYPE;
        } else if (misfitActivityTypeValue == SdkActivityType.FOOTBALL_TYPE) {
            return ActivityTypeShine.FOOTBALL_TYPE;
        } else if (misfitActivityTypeValue == SdkActivityType.TENNIS_TYPE) {
            return ActivityTypeShine.TENNIS_TYPE;
        } else if (misfitActivityTypeValue == SdkActivityType.DANCING_TYPE) {
            return ActivityTypeShine.DANCING_TYPE;
        } else if (misfitActivityTypeValue == SdkActivityType.YOGA_TYPE) {
            return ActivityTypeShine.YOGA_TYPE;
        } else {
            return ActivityTypeShine.UNKNOWN_TYPE;
        }
    }
}
