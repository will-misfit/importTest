package com.misfit.ble.shine.parser;

import com.misfit.ble.shine.result.SyncResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will-Hou on 12/16/15.
 */
public class TimestampCorrectorNew {

    public final static int OK = 0;
    public final static int NO_ACTIVITY_IN_FILE = -1;

    private final static int BEGIN = 0;
    private final static int END = 1;
    private final static int INTERVAL_DURATION = 300;
    private final static long MAGIC_TIME = 1369008000L;

    public int correctTimestamp(List<SyncResult> syncResults, long prevCorrectTimestamp) {

        //calculate every file's timestamp offset
        long[] activityTimeOffsets;
        try {
            activityTimeOffsets = calculateTimeOffset(syncResults);
        } catch (Exception e) {
            e.printStackTrace();
            return NO_ACTIVITY_IN_FILE;
        }

        //group activities
        List<int[]> groups = groupNearActivities(syncResults);

        long syncTimeOrNextGroupStartTime = prevCorrectTimestamp;    //init with sync time
        long prevGroupEndTime = 0;

        for (int i = groups.size() - 1; i >= 0; i--) {
            if (i > 0) {
                prevGroupEndTime = syncResults.get(groups.get(i - 1)[END]).getLatestActEndTime();
            } else {
                //for last one
                prevGroupEndTime = 0;
            }

            long currGroupStartTime = syncResults.get(groups.get(i)[BEGIN]).getEarliestStartTime();
            long currGroupEndTime = syncResults.get(groups.get(i)[END]).getLatestActEndTime();
            boolean isSyncTime = i == groups.size() - 1;
            if (shouldCorrectGroupTimestamp(currGroupStartTime, currGroupEndTime, syncTimeOrNextGroupStartTime, prevGroupEndTime, isSyncTime)) {
                for (int j = groups.get(i)[END]; j <= groups.get(i)[BEGIN]; j++) {
                    long correctTimeStamp = prevCorrectTimestamp - activityTimeOffsets[j] * 60 + 1;
                    long delta = correctTimeStamp - syncResults.get(j).getEarliestStartTime();
                    syncResults.get(j).correctSyncDataTimestamp(delta);
                }
            }
            syncTimeOrNextGroupStartTime = syncResults.get(groups.get(i)[BEGIN]).getEarliestStartTime();
        }
        return OK;
    }

    public boolean shouldCorrectGroupTimestamp(long startTime, long endTime, long syncTimeOrNextGroupStartTime, long prevGroupEndTime, boolean isSyncTime) {
    /*
    When should correct:
    - startTime earlier than magic time
    - (startTime + INTERVAL) earlier than previous group end time
    - (endTime + INTERVAL) later than next group stat time
    - for sync time, endTime later than sync time
    */

        if (startTime < MAGIC_TIME
                || (startTime + INTERVAL_DURATION) < prevGroupEndTime
                || (endTime + INTERVAL_DURATION) < syncTimeOrNextGroupStartTime
                || (isSyncTime && endTime > syncTimeOrNextGroupStartTime)) {
            return true;
        } else {
            return false;
        }
    }

    public List<int[]> groupNearActivities(List<SyncResult> syncResults) {
        if (syncResults == null || syncResults.size() < 1) {
            return new ArrayList<>();
        }
        List<int[]> groups = new ArrayList<>();

        long prevStartTime = -1;
        //[0]:begin  [1]:end
        int[] currGroup = new int[]{syncResults.size() - 1, syncResults.size() - 1};

        for (int i = syncResults.size() - 1; i >= 0; i--) {
            if (prevStartTime != -1) {
                if (Math.abs(prevStartTime - syncResults.get(i).getLatestActEndTime()) > INTERVAL_DURATION) {
                    currGroup[END] = i + 1;
                    groups.add(0, currGroup);

                    currGroup = new int[]{i, i};
                } else {
                    currGroup[END] = i;
                }
            }
            prevStartTime = syncResults.get(i).getEarliestStartTime();
        }
        groups.add(0, currGroup);
        return groups;
    }

    public long[] calculateTimeOffset(List<SyncResult> syncResults) throws Exception {
        long sum = 0;
        long[] syncRsltTimeOffsets = new long[syncResults.size()];
        for (int i = syncResults.size() - 1; i >= 0; i--) {
            long minOfActivities = syncResults.get(i).getTotalMinOfActivities();
            if (minOfActivities == -1) {
                throw new IllegalStateException("no activity in file");
            }
            sum += minOfActivities;
            syncRsltTimeOffsets[i] = sum;
        }
        return syncRsltTimeOffsets;
    }
}
