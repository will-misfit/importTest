package com.misfit.ble.shine.parser;

import com.misfit.ble.shine.result.SyncResult;

import java.util.ArrayList;
import java.util.List;

/**
 * updated TimestampCorrector, to fix some time shift error which may cause data loss
 */
public class TimestampCorrectorNew {

    public static final String TAG = "TimestampCorrectorNew";

    public final static int OK = 0;
    public final static int NO_ACTIVITY_IN_FILE = -1;

    private final static int BEGIN = 0;
    private final static int END = 1;
    private final static int INTERVAL_DURATION = 300;   // 5 min
    private final static long MAGIC_TIME = 1369008000L; // May 20th, 2013

    public int correctTimestamp(List<SyncResult> syncResults, long syncTimestamp) {

        //calculate every file's timestamp offset
        long[] activityTimeOffsets;
        try {
            activityTimeOffsets = calculateNumMinSum(syncResults);
        } catch (Exception e) {
            e.printStackTrace();
            return NO_ACTIVITY_IN_FILE;
        }

        //group SyncResults
        List<int[]> groups = groupNearSyncResults(syncResults);

        long syncTimeOrNextGroupStartTime = syncTimestamp;    //init with sync time
        long prevGroupEndTime = 0;

        for (int i = groups.size() - 1; i >= 0; i--) {
            if (i > 0) {
                prevGroupEndTime = syncResults.get(groups.get(i - 1)[END]).getTailEndTime(); // here 'prev' means the previous in collection order as well
            } else {
                //for last processed one
                prevGroupEndTime = 0;
            }

            long currGroupStartTime = getMinimumHeadStartTime(syncResults, groups.get(i)[BEGIN], groups.get(i)[END]);
            long currGroupEndTime = getMaxmumTailEndTime(syncResults, groups.get(i)[BEGIN], groups.get(i)[END]);
            boolean isSyncTime = i == (groups.size() - 1);
            if (shouldCorrectGroupTimestamp(currGroupStartTime, currGroupEndTime, syncTimeOrNextGroupStartTime, prevGroupEndTime, isSyncTime)) {
                for (int j = groups.get(i)[BEGIN]; j <= groups.get(i)[END]; j++) {
                    long correctTimeStamp = syncTimestamp - activityTimeOffsets[j] * 60 + 1;
                    long delta = correctTimeStamp - syncResults.get(j).getHeadStartTime();
                    syncResults.get(j).correctSyncDataTimestamp(delta);
                }
            }
            syncTimeOrNextGroupStartTime = syncResults.get(groups.get(i)[BEGIN]).getHeadStartTime();
        }
        return OK;
    }

    /**
     * When to correct:
     * - startTime earlier than magic time
     * - (startTime + INTERVAL) earlier than previous group end time
     * - (endTime + INTERVAL) later than next group start time
     * - for sync time, endTime is later than syncTime
     * NOTE: in time line, previous group is earlier than current group/item, and next group is later
    */
    public boolean shouldCorrectGroupTimestamp(long startTime, long endTime,
                                               long syncTimeOrNextGroupStartTime, long prevGroupEndTime, boolean isSyncTime) {
        if (startTime < MAGIC_TIME
                || (startTime + INTERVAL_DURATION) < prevGroupEndTime
                || (endTime + INTERVAL_DURATION) < syncTimeOrNextGroupStartTime
                || (isSyncTime && endTime > syncTimeOrNextGroupStartTime)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * group given SyncResult collection by the group interval more than 300 sec
     * @return: each group is described as int[2]{} of index in SyncResult collection
     * NOTE: the SyncResult order remains stable
     * */
    public List<int[]> groupNearSyncResults(List<SyncResult> syncResults) {
        if (syncResults == null || syncResults.isEmpty()) {
            return new ArrayList<>();
        }
        List<int[]> groups = new ArrayList<>();

        long prevStartTime = -1; // 'prev' means previous in process order. For collection order, it is 'post' indeed
        //[0]:begin  [1]:end
        final int size = syncResults.size();
        int[] currGroup = new int[]{size - 1, size - 1};

        for (int i = size - 1; i >= 0; i--) {
            if (prevStartTime != -1) {
                if (Math.abs(prevStartTime - syncResults.get(i).getTailEndTime()) > INTERVAL_DURATION) {
                    groups.add(0, currGroup);
                    currGroup = new int[]{i, i};
                } else {
                    currGroup[BEGIN] = i; // move beginIndex backwards
                }
            }
            prevStartTime = syncResults.get(i).getHeadStartTime();
        }
        groups.add(0, currGroup);
        return groups;
    }

    /**
     * for each SyncResult, the time offset is the minutes from its head to tail of entire SyncResult collection
     * */
    public long[] calculateNumMinSum(List<SyncResult> syncResults) throws Exception {
        long sum = 0;
        long[] syncRsltTimeOffsets = new long[syncResults.size()];
        for (int i = syncResults.size() - 1; i >= 0; i--) {
            long minutes = syncResults.get(i).getTotalMinutes();
            if (minutes == -1) {
                throw new IllegalStateException("no activity in file");
            }
            sum += minutes;
            syncRsltTimeOffsets[i] = sum;
        }
        return syncRsltTimeOffsets;
    }

    /*
    * @param beginIndex and endIndex are both inclusive
    * NOTE: ensure beginIndex <= endIndex
    * */
    private long getMinimumHeadStartTime(List<SyncResult> syncResults, int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex >= syncResults.size()) {
            return 0;
        }
        if (endIndex < 0 || endIndex >= syncResults.size()) {
            return 0;
        }
        long result = syncResults.get(beginIndex).getHeadStartTime();
        for (int i = beginIndex + 1; i <= endIndex; i++) {
            result = Math.min(result, syncResults.get(i).getHeadStartTime());
        }
        return result;
    }

    /*
    * NOTE: ensure beginIndex <= endIndex
    * */
    private long getMaxmumTailEndTime(List<SyncResult> syncResults, int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex >= syncResults.size()) {
            return 0;
        }
        if (endIndex < 0 || endIndex >= syncResults.size()) {
            return 0;
        }
        long result = syncResults.get(beginIndex).getTailEndTime();
        for (int i = beginIndex + 1; i <= endIndex; i++) {
            result = Math.max(result, syncResults.get(i).getTailEndTime());
        }
        return result;
    }
}
