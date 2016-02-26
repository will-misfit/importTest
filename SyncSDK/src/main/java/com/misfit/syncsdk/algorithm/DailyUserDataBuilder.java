package com.misfit.syncsdk.algorithm;

import android.util.Log;
import android.util.SparseArray;

import com.misfit.ble.shine.result.SyncResult;
import com.misfit.cloud.algorithm.algos.ActivitySessionsFlashAlgorithm;
import com.misfit.cloud.algorithm.models.ACEEntryVect;
import com.misfit.cloud.algorithm.models.ActivitySessionShineVect;
import com.misfit.cloud.algorithm.models.ActivityShine;
import com.misfit.cloud.algorithm.models.ActivityShineVect;
import com.misfit.cloud.algorithm.models.GapSessionShineVect;
import com.misfit.cloud.algorithm.models.SWLEntryVect;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;
import com.misfit.syncsdk.model.SdkActivitySession;
import com.misfit.syncsdk.model.SdkDayRange;
import com.misfit.syncsdk.model.SdkGraphItem;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.model.SdkResourceSettings;
import com.misfit.syncsdk.model.SdkSleepSession;
import com.misfit.syncsdk.model.SdkTimezoneOffset;
import com.misfit.syncsdk.utils.DateUtils;
import com.misfit.syncsdk.utils.MLog;
import com.misfit.syncsdk.utils.TimeZoneUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * in flagship app, DailyUserDataBuilder#buildAndSaveDaysForGoogleFit() and groupDailyActivitiesForGoogleFit()
 * are added for Standalone feature, but not moved to SyncSDK yet
 */
public class DailyUserDataBuilder {

    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("MisfitAlgorithmLib");
    }

    private static final String TAG = "DailyUserDataBuilder";
    private static DailyUserDataBuilder sDefaultInstance;

    public synchronized static DailyUserDataBuilder getInstance() {
        if (sDefaultInstance == null) {
            sDefaultInstance = new DailyUserDataBuilder();
        }
        return sDefaultInstance;
    }

    protected DailyUserDataBuilder() {}

    public SdkActivitySessionGroup buildDailyUserDataForShine(SyncResult syncResult,
                                                              List<SdkResourceSettings> settingsChangesSinceLastSync,
                                                              SdkProfile userProfile) {
        ActivityShineVect activityShineVect = AlgorithmUtils.convertSdkActivityToShineActivityForShine(
            syncResult.mActivities, syncResult.mTapEventSummarys);
        SWLEntryVect swlEntryVec = AlgorithmUtils.convertSwimSessionsToSWLEntry(syncResult.mSwimSessions);
        ACEEntryVect aceEntryVect = new ACEEntryVect(); // placeholder for ACE algorithm result in future

        if (activityShineVect.size() == 0) {
            return new SdkActivitySessionGroup();
        }
        return buildUserSessionsForShine(activityShineVect, aceEntryVect, swlEntryVec, settingsChangesSinceLastSync, userProfile);
    }

    /**
     * build up ActivitySessions, SleepSessions, GraphItems
     * */
    private SdkActivitySessionGroup buildUserSessionsForShine(ActivityShineVect activityShineVect,
                                                              ACEEntryVect aceEntryVect,
                                                              SWLEntryVect swlEntryVect,
                                                              List<SdkResourceSettings> settingsChangesSinceLasySync,
                                                              SdkProfile userProfile) {
        MLog.d(TAG, "buildUserSessionsForShine");
        SdkActivitySessionGroup result = new SdkActivitySessionGroup();

        List<SdkSleepSession> sleepSessions = SdkSleepSessionBuilder.buildSdkSleepSessions(activityShineVect, settingsChangesSinceLasySync);
        result.sleepSessionList.addAll(sleepSessions);

        List<SdkActivitySession> sdkActivitySessions = SdkActivitySessionBuilder.buildSdkActivitySessionForShine(
            activityShineVect, aceEntryVect, swlEntryVect, settingsChangesSinceLasySync, userProfile);
        result.activitySessionList.addAll(sdkActivitySessions);

        List<SdkGraphItem> graphItems = SdkGraphItemBuilder.buildGraphItems(activityShineVect);
        result.graphItemList.addAll(graphItems);
        return result;
    }

    public SdkActivitySessionGroup buildUserSessionsForFlash(SyncResult syncResult, SyncCalculationCallback syncCalculationCallback) {
        SdkActivitySessionGroup result = new SdkActivitySessionGroup();
        ActivityShineVect activityShineVect = AlgorithmUtils.convertSdkActivityToShineActivityForFlash(syncResult.mActivities,
            syncResult.mSessionEvents);
        return result;
    }

    /**
     * group per minute ActivityShine to day by day list with consideration of timezone changes
     * */
    private Map<Long, DailyActivityGroup> groupDailyActivities(ActivityShineVect activities,
                                                               SdkTimezoneOffset timeZoneBefore,
                                                               List<SdkTimezoneOffset> timeZoneListAfter) {
        Log.d(TAG, String.format("groupDailyActivities(), @param ActivityShineVect size %d", activities.size()));
        Map<Long, DailyActivityGroup> result = new HashMap<>();
        if (activities == null || activities.size() <= 0) {
            return result;
        }

        SdkDayRange dayRange = null;
        long goalStartTime = 0L;
        long goalEndTime = 0L;
        // TODO: SparseArray is not necessary, List is OK
        SparseArray<TimeZone> getTimezoneChanges = TimeZoneUtils.getTimezoneChanges(timeZoneBefore, timeZoneListAfter);

        int timezoneChoseIndex = 0;
        int nextTimezoneIndex = 1;
        TimeZone currentTz;
        // TODO: below 2 layers loop can be refactored
        for (int i = 0; i < activities.size(); ++i) {
            ActivityShine activity = activities.get(i);
            long activityStartTime = activity.getStartTime();
            if (activityStartTime < goalStartTime || activityStartTime > goalEndTime) {
                // ActivityShine start/end time exceed the duration of the one day range
                while (nextTimezoneIndex < getTimezoneChanges.size() && activityStartTime > getTimezoneChanges.keyAt(nextTimezoneIndex)) {
                    timezoneChoseIndex++;
                    nextTimezoneIndex++;
                }
                currentTz = getTimezoneChanges.valueAt(timezoneChoseIndex);
                dayRange = DateUtils.getSpecificDayRange(activityStartTime, currentTz);
                goalStartTime = dayRange.startTime;
                goalEndTime = dayRange.endTime;
                Log.d(TAG, String.format("groupDailyActivities(), day range between %d and %d", goalStartTime, goalEndTime));
            }

            if (dayRange != null) {
                Long key = dayRange.startTime;
                if (!result.containsKey(key)) {
                    Log.d(TAG, "groupDailyActivities(), Result map for key " + key + " is not existing, create new DailyActivityGroup");
                    result.put(key, new DailyActivityGroup(dayRange));
                }
                Log.d(TAG, "groupDailyActivities(), Put activity " + activity.getStartTime() + " to group " + key);
                result.get(key).activities.add(activity);
            }
        }
        return result;
    }

    /**
     * model class for per minute activity data and sleep sessions within one day range
     * */
    public static class DailyActivityGroup {
        public List<SdkSleepSession> sleepSessions = new ArrayList<>();
        public ActivityShineVect activities = new ActivityShineVect();
        public SdkDayRange sdkDayRange;
        public DailyActivityGroup(SdkDayRange dayRange) {
            sdkDayRange = dayRange;
        }
    }

    private void groupDailySleepSessions(DailyActivityGroup group, List<SdkSleepSession> sleepSessions) {
        int sleepSessionsSize = sleepSessions.size();
        int i = 0;
        while (i < sleepSessionsSize) {
            SdkSleepSession sleepSession = sleepSessions.get(i);
            if (sleepSession.getRealEndTime() >= group.sdkDayRange.startTime
                    && sleepSession.getRealEndTime() <= group.sdkDayRange.endTime) {
                group.sleepSessions.add(sleepSession);
            }
            i++;
        }
    }

}
