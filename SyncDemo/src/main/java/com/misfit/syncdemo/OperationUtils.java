package com.misfit.syncdemo;

import com.misfit.ble.shine.result.Activity;
import com.misfit.ble.shine.result.SessionEvent;
import com.misfit.ble.shine.result.SwimSession;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.ble.shine.result.TapEventSummary;
import com.misfit.syncsdk.enums.SdkActivityType;
import com.misfit.syncsdk.model.SdkActivitySession;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;
import com.misfit.syncsdk.model.SdkSleepSession;

import java.util.List;

/**
 * support some utility methods for operations on SyncDemo
 */
public class OperationUtils {

    public static String buildShineSdkSyncResult(SyncResult syncResult) {
        StringBuilder stringBuilder = new StringBuilder();
        if (syncResult != null) {
            if (syncResult.mSwimSessions != null) {
                for (SwimSession swimSession : syncResult.mSwimSessions) {
                    stringBuilder.append(String.format("\nSwimSession - %s\n", swimSession.toString()));
                }
            }

            int totalPoint = 0;
            int totalSteps = 0;
            if (syncResult.mActivities != null) {
                for (Activity activity : syncResult.mActivities) {
                    totalPoint += activity.mPoints;
                    totalSteps += activity.mBipedalCount;
                }
            }
            stringBuilder.append(String.format("Activity - totalPoint: %d, totalSteps: %d\n", totalPoint, totalSteps));

            if (syncResult.mTapEventSummarys != null) {
                for (TapEventSummary tapEventSummary : syncResult.mTapEventSummarys) {
                    stringBuilder.append(String.format("TapEventSummary - timestamp: %d, tapType: %d, tapCount: %d\n",
                        tapEventSummary.mTimestamp,
                        tapEventSummary.mTapType,
                        tapEventSummary.mCount));
                }
            }

            if (syncResult.mSessionEvents != null) {
                for (SessionEvent sessionEvent : syncResult.mSessionEvents) {
                    stringBuilder.append(String.format("SessionEvent - timestamp: %d, eventType: %d\n",
                        sessionEvent.mTimestamp,
                        sessionEvent.mType));
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * one SdkActivitySessionGroup includes all activity session and all sleep session in one day,
     * */
    public static String buildSyncCalculationResult(SdkActivitySessionGroup sdkActivitySessionGroup) {
        if (sdkActivitySessionGroup == null) {
            return new String();
        }

        StringBuilder strBuilder = new StringBuilder();
        for (SdkActivitySession sdkActSession: sdkActivitySessionGroup.activitySessionList) {
            strBuilder.append(String.format("Activity Session, activity type %s, Is Gap Session: %s, starts at %d, duration seconds %d, points %d, steps %d, calories %.2f\n",
                SdkActivityType.getActivityTypeString(sdkActSession.getActivityType()),
                Boolean.toString(sdkActSession.isGapSession()),
                sdkActSession.getStartTime(),
                sdkActSession.getDuration(),
                sdkActSession.getPoints(),
                sdkActSession.getSteps(),
                sdkActSession.getCalories()));
        }

        for (SdkSleepSession sdkSleepSession: sdkActivitySessionGroup.sleepSessionList) {
            strBuilder.append(String.format("Sleep Session, real starts at %d, real ends at %d, sleep duration seconds %d, deep sleep seconds %d\n",
                sdkSleepSession.getRealStartTime(),
                sdkSleepSession.getRealEndTime(),
                sdkSleepSession.getSleepDuration(),
                sdkSleepSession.getDeepSleepSecs()));
        }
        strBuilder.append("==============================\n");

        return strBuilder.toString();
    }

    public static int getActivityPointSum(List<SdkActivitySession> sdkActivitySessions) {
        int points = 0;
        for (SdkActivitySession sdkActivitySession : sdkActivitySessions) {
            // FIXME: does GapSession count for daily ActivityPoints?
            points += sdkActivitySession.getPoints();
        }
        return points;
    }
}
