package com.misfit.syncsdk.algorithm;

import android.util.Log;

import com.misfit.cloud.algorithm.algos.SleepSessionsShineAlgorithm;
import com.misfit.cloud.algorithm.models.ActivityShineVect;
import com.misfit.cloud.algorithm.models.AutoSleepStatChangeShine;
import com.misfit.cloud.algorithm.models.AutoSleepStatChangeShineVect;
import com.misfit.cloud.algorithm.models.AutoSleepStateShine;
import com.misfit.cloud.algorithm.models.SleepSessionShine;
import com.misfit.cloud.algorithm.models.SleepSessionShineVect;
import com.misfit.cloud.algorithm.models.SleepStateChangeShine;
import com.misfit.cloud.algorithm.models.SleepStateShineVect;
import com.misfit.cloud.algorithm.models.TimezoneChangeShine;
import com.misfit.cloud.algorithm.models.TimezoneChangeShineVect;
import com.misfit.cloud.algorithm.models.UserSleepSessionShine;
import com.misfit.cloud.algorithm.models.UserSleepSessionShineVect;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.model.SdkAutoSleepStateChangeTag;
import com.misfit.syncsdk.model.SdkSleepSession;
import com.misfit.syncsdk.enums.SdkSleepState;
import com.misfit.syncsdk.model.SdkTimeZoneOffset;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to build up SdkSleepSession
 */
public class SdkSleepSessionBuilder {

    private static final String TAG = "SdkSleepSessionBuilder";

    public static List<SdkSleepSession> buildSdkSleepSessions(ActivityShineVect activityShineVect,
                                                              SyncCalculationCallback syncCalculationCallback) {
        return convertUserSleepSessionShineVectToList(
                buildUserSleepSessionShineVect(activityShineVect, syncCalculationCallback));
    }

    private static UserSleepSessionShineVect buildUserSleepSessionShineVect(ActivityShineVect activityShineVect,
                                                                            SyncCalculationCallback syncCalculationCallback) {
        Log.d(TAG, "buildUserSleepSessionShineVect");
        UserSleepSessionShineVect userSleepSessionShineVect = new UserSleepSessionShineVect();
        SleepSessionsShineAlgorithm sleepSessionsShineAlgorithm = new SleepSessionsShineAlgorithm();
        SleepSessionShineVect autoSleepSessions = new SleepSessionShineVect();
        SleepSessionShineVect manualSleepSessions = new SleepSessionShineVect();
        sleepSessionsShineAlgorithm.buildSleepSession(activityShineVect, autoSleepSessions, manualSleepSessions);
        if (autoSleepSessions == null && manualSleepSessions == null) {
            return userSleepSessionShineVect;
        }

        int[] startEndAutoSleepSessions = getSleepSessionsStartEndTime(autoSleepSessions);
        int[] startEndManualSleepSessions = getSleepSessionsStartEndTime(manualSleepSessions);
        int[] startEndTime = AlgorithmUtils.getStartEndTimeFromTwoSessions(startEndAutoSleepSessions, startEndManualSleepSessions);
        List<SdkAutoSleepStateChangeTag> autoSleepChangeTags = syncCalculationCallback.getSdkAutoSleepStateChangeTagList(startEndTime);
        SdkTimeZoneOffset timezoneOffset = syncCalculationCallback.getSdkTimeZoneOffsetInCurrentSettings();

        sleepSessionsShineAlgorithm.buildUserSleepSession(autoSleepSessions,
            manualSleepSessions,
            buildTimezoneChangeShineVectByCurrentTimezone(timezoneOffset),
            buildAutoSleepStatChangeShineVect(autoSleepChangeTags),
            userSleepSessionShineVect);
        Log.d(TAG, "buildUserSleepSessionShineVect sleepSession size " + userSleepSessionShineVect.size());
        return userSleepSessionShineVect;
    }

    private static List<SdkSleepSession> convertUserSleepSessionShineVectToList(UserSleepSessionShineVect userSleepSessionShineVect) {
        List<SdkSleepSession> sleepSessions = new ArrayList<>();
        for (int i = 0; i < userSleepSessionShineVect.size(); i++ ) {
            UserSleepSessionShine userSleepSessionShine = userSleepSessionShineVect.get(i);
            SdkSleepSession sleepSession = new SdkSleepSession();
            sleepSession.setTimestamp(userSleepSessionShine.getStartTime());
            // sleepSession.setSource(SessionSource.MISFIT);
            sleepSession.setRealStartTime(userSleepSessionShine.getStartTime());
            sleepSession.setRealEndTime(userSleepSessionShine.getStartTime() + userSleepSessionShine.getDuration());
            if(userSleepSessionShine.getIsAutoDetected()== AutoSleepStateShine.AUTO_SLEEP){
                sleepSession.setIsAutoDetected(true);
            }else {
                sleepSession.setIsAutoDetected(false);
            }
            sleepSession.setDeepSleepSecs(userSleepSessionShine.getDeepSleepMinute() * 60);
            sleepSession.setSleepSecs(userSleepSessionShine.getSleepMinute() * 60);
            sleepSession.setSleepDuration(userSleepSessionShine.getDuration());
            sleepSession.setBookmarkTime(userSleepSessionShine.getBookmarkTime());
            sleepSession.setNormalizedSleepQuality((int) userSleepSessionShine.getNormalizedSleepQuality());
            sleepSession.setSleepStateChanges(
                convertSleepStateShineVectToSleepStateChanges(userSleepSessionShine.getStateChanges(), userSleepSessionShine.getStartTime()));
            sleepSessions.add(sleepSession);
        }
        return sleepSessions;
    }

    private static List<long[]> convertSleepStateShineVectToSleepStateChanges(SleepStateShineVect sleepStateShineVect, int startTime) {
        if (sleepStateShineVect == null || sleepStateShineVect.isEmpty()) {
            return null;
        }
        List<long[]> sleepStateChanges = new ArrayList<>();
        for (int i = 0; i < sleepStateShineVect.size(); i++) {
            long[] change = new long[2];
            SleepStateChangeShine sleepStateChangeShine = sleepStateShineVect.get(i);
            change[0] = sleepStateChangeShine.getIndex() * 60 + startTime;
            change[1] = SdkSleepState.convertSleepStateShine2SdkSleepState(sleepStateChangeShine.getState());
            sleepStateChanges.add(change);
        }
        return sleepStateChanges;
    }

    private static int[] getSleepSessionsStartEndTime(SleepSessionShineVect sessionShineVect) {
        if (sessionShineVect == null || sessionShineVect.isEmpty()) {
            Log.d(TAG, "session empty");
            return new int[]{0, 0};
        }
        int sessionsStartTime = sessionShineVect.get(0).getStartTime();
        int sessionsEndTime = sessionShineVect.get(0).getStartTime();
        for (int i = 1; i < sessionShineVect.size(); i++) {
            SleepSessionShine sleepSessionShine = sessionShineVect.get(i);
            if (sleepSessionShine.getStartTime() < sessionsStartTime) {
                sessionsStartTime = sleepSessionShine.getStartTime();
            } else if (sleepSessionShine.getStartTime() > sessionsEndTime) {
                sessionsEndTime = sleepSessionShine.getStartTime();
            }
        }
        int[] startEndTime = new int[2];
        startEndTime[0] = sessionsStartTime;
        startEndTime[1] = sessionsEndTime;
        Log.d(TAG, "getSleepSessionsStartEndTime " + startEndTime[0] + " " + startEndTime[1]);
        return startEndTime;
    }

    private static TimezoneChangeShineVect buildTimezoneChangeShineVectByCurrentTimezone(SdkTimeZoneOffset sdkTimeZoneOffset) {
        TimezoneChangeShineVect timezoneChangeShineVect = new TimezoneChangeShineVect();
        TimezoneChangeShine timezoneChangeShine = getTimezoneChangeShineFromSettings(sdkTimeZoneOffset);
        timezoneChangeShineVect.add(timezoneChangeShine);
        return timezoneChangeShineVect;
    }

    private static TimezoneChangeShine getTimezoneChangeShineFromSettings(SdkTimeZoneOffset sdkTimeZoneOffset) {
        TimezoneChangeShine timezoneChangeShine = new TimezoneChangeShine();
        timezoneChangeShine.setTimestamp((int) sdkTimeZoneOffset.getTimestamp());
        timezoneChangeShine.setTimezoneOffsetInSecond(sdkTimeZoneOffset.getTimezoneOffsetInSecond());
        return timezoneChangeShine;
    }

    private static AutoSleepStatChangeShineVect buildAutoSleepStatChangeShineVect(List<SdkAutoSleepStateChangeTag> sdkAutoSleepStateChangeTags) {
        Log.d(TAG, "buildAutoSleepStatChangeShineVect");
        AutoSleepStatChangeShineVect autoSleepStatChangeShineVect = new AutoSleepStatChangeShineVect();
        for (SdkAutoSleepStateChangeTag autoSleepChangeTag : sdkAutoSleepStateChangeTags) {
            AutoSleepStatChangeShine autoSleepStatChangeShine = getAutoSleepStatChangeShineFromSettings(autoSleepChangeTag);
            autoSleepStatChangeShineVect.add(autoSleepStatChangeShine);
        }
        return autoSleepStatChangeShineVect;
    }

    private static AutoSleepStatChangeShine getAutoSleepStatChangeShineFromSettings(SdkAutoSleepStateChangeTag autoSleepChangeTag) {
        AutoSleepStatChangeShine autoSleepStatChangeShine = new AutoSleepStatChangeShine();
        autoSleepStatChangeShine.setTimestamp((int)autoSleepChangeTag.getTimestamp());
        autoSleepStatChangeShine.setState(autoSleepChangeTag.isAutoSleepState() ?
            AutoSleepStateShine.AUTO_SLEEP : AutoSleepStateShine.MANUAL_SLEEP);
        return autoSleepStatChangeShine;
    }
}
