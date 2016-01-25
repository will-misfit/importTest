package com.misfit.syncsdk.algorithm;

import android.util.Log;
import android.util.SparseArray;

import com.misfit.ble.shine.result.SyncResult;
import com.misfit.cloud.algorithm.models.ACEEntryVect;
import com.misfit.cloud.algorithm.models.ActivityShine;
import com.misfit.cloud.algorithm.models.ActivityShineVect;
import com.misfit.cloud.algorithm.models.SWLEntryVect;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;
import com.misfit.syncsdk.model.SdkActivitySession;
import com.misfit.syncsdk.model.SdkDayRange;
import com.misfit.syncsdk.model.SdkSleepSession;
import com.misfit.syncsdk.model.SdkTimeZoneOffset;
import com.misfit.syncsdk.utils.DateUtils;
import com.misfit.syncsdk.utils.TimeZoneUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 *
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

    public List<SdkActivitySessionGroup> buildDailyUserDataForShine(SyncResult syncResult, SyncSyncCallback syncSyncCallback) {
        ActivityShineVect activityShineVect = AlgorithmUtils.convertSdkActivityToShineActivityForShine(
            syncResult.mActivities, syncResult.mTapEventSummarys);
        SWLEntryVect swlEntryVec = AlgorithmUtils.convertSwimSessionsToSWLEntry(syncResult.mSwimSessions);
        ACEEntryVect aceEntryVect = new ACEEntryVect(); // placeholder for ACE algorithm result in future

        if (activityShineVect.size() == 0) {
            return new ArrayList<>();
        }
        return buildDaysForShine(activityShineVect, aceEntryVect, swlEntryVec, syncSyncCallback);
    }

    /**
     * build up ActivitySessions, SleepSessions, GraphItems
     * */
    public List<SdkActivitySessionGroup> buildDaysForShine(ActivityShineVect activityShineVect, ACEEntryVect aceEntryVect, SWLEntryVect swlEntryVect, SyncSyncCallback syncCallback) {
        Log.d(TAG, "buildDaysForShine");
        List<SdkActivitySessionGroup> groupsResult = new ArrayList<>();

        List<SdkSleepSession> sleepSessions = SdkSleepSessionBuilder.buildSdkSleepSessions(activityShineVect, syncCallback);

        long startTime = activityShineVect.get(0).getStartTime();
        Map<Long, DailyActivityGroup> dailyActivityGroupMap = groupDailyActivities(activityShineVect,
            syncCallback.getSdkTimeZoneOffsetBefore(startTime),
            syncCallback.getSdkTimeZoneOffsetListAfter(startTime));

        // SdkActivitySessions, SdkSleepSessions, GraphItems are built up inside each group
        for (Long dailyStartTime : dailyActivityGroupMap.keySet()) {
            SdkActivitySessionGroup res = new SdkActivitySessionGroup();
            DailyActivityGroup group = dailyActivityGroupMap.get(dailyStartTime);
            groupDailySleepSessions(group, sleepSessions);

            res.sleepSessionList.addAll(group.sleepSessions);

            List<SdkActivitySession> sdkActivitySessions = SdkActivitySessionBuilder.buildSdkActivitySessionForShine(
                group.activities, aceEntryVect, swlEntryVect, syncCallback);
            res.activitySessionyList.addAll(sdkActivitySessions);
            // List<GraphItem> graphItems = GraphItemBuilder.buildGraphItems(group.activities, graphDayQueryManager, group.dayRange);

            groupsResult.add(res);
        }
        return groupsResult;
    }

    public void buildDailyUserDataForFlash(SyncResult syncResult, SyncSyncCallback syncSyncCallback) {
        ActivityShineVect activityShineVect = AlgorithmUtils.convertSdkActivityToShineActivityForFlash(
            syncResult.mActivities, syncResult.mSessionEvents);
    }

    /**
     * group per minute ActivityShine list by timezone changes
     * */
    private Map<Long, DailyActivityGroup> groupDailyActivities(ActivityShineVect activities,
                                                               SdkTimeZoneOffset timeZoneBefore,
                                                               List<SdkTimeZoneOffset> timeZoneListAfter) {
        Map<Long, DailyActivityGroup> result = new HashMap<>();
        if (activities == null || activities.size() <= 0) {
            return result;
        }

        SdkDayRange dayRange = null;
        long goalStartTime = 0L;
        long goalEndTime = 0L;
        SparseArray<TimeZone> getTimezoneChanges = TimeZoneUtils.getTimezoneChanges(timeZoneBefore, timeZoneListAfter);

        int timezoneChoseIndex = 0;
        int nextTimezoneIndex = 1;
        TimeZone currentTz = null;
        // TODO: below 2 layers loop can be refactored
        for (int i = 0; i < activities.size(); i ++) {
            ActivityShine activity = activities.get(i);
            long activityStartTime = activity.getStartTime();
            if (activityStartTime < goalStartTime || activityStartTime > goalEndTime) {
                while (nextTimezoneIndex < getTimezoneChanges.size() && activityStartTime > getTimezoneChanges.keyAt(nextTimezoneIndex)) {
                    timezoneChoseIndex++;
                    nextTimezoneIndex++;
                }
                currentTz = getTimezoneChanges.valueAt(timezoneChoseIndex);
                dayRange = DateUtils.getSpecificDayRange(activityStartTime, currentTz);
                goalStartTime = dayRange.startTime;
                goalEndTime = dayRange.endTime;
            }

            if (dayRange != null) {
                Long key = dayRange.startTime;
                if (!result.containsKey(key)) {
                    Log.d(TAG, "Result map for key " + key + " is not existing, create new DailyActivityGroup");
                    result.put(key, new DailyActivityGroup(dayRange));
                }
                Log.d(TAG, "Put activity " + activity.getStartTime() + " to group " + key);
                result.get(key).activities.add(activity);
            }
        }
        return result;
    }

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
