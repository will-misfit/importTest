package com.misfit.ble.sdk;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.misfit.ble.shine.parser.TimestampCorrectorNew;
import com.misfit.ble.shine.result.Activity;
import com.misfit.ble.shine.result.SyncResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will-Hou on 12/16/15.
 */
public class TimestampCorrectorTest extends InstrumentationTestCase {

    TimestampCorrectorNew timestampCorrector;
    private final static String TAG = "TimestampCorrectorTest";
    private final static int INTERVAL_DURATION = 300; // 5 min
    private final static long MAGIC_TIME = 1369008000L;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        timestampCorrector = new TimestampCorrectorNew();
    }

    /**
     * Case 1, one SyncResult with two Activity
     * */
    public void testOnlyOneOver() throws Exception {
        final long syncTime = 1448209300;
        final int actCount = 2;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime + INTERVAL_DURATION, actCount));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(rslt, 0);
        assertEquals(syncTime - actCount * 60, data.get(0).getHeadStartTime());
    }

    /**
     * Case 2, one input SyncResult timestamp is correct, no need to adjust
     * */
    public void testOnlyOneCorrect() throws Exception {
        final long syncTime = 1448209300;
        final int actCount = 2;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime - 1, actCount));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - actCount * 60, data.get(0).getHeadStartTime());
    }

    /**
     * Case 3, one input SyncResult timestamp is not 300s less than SyncTime, so that no need to adjust
     * */
    public void testOnlyUnderTheEdge() throws Exception {
        final long syncTime = 1448209300;
        final int actCount = 2;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime - INTERVAL_DURATION, actCount));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - INTERVAL_DURATION, data.get(0).getTailEndTime());
    }

    /**
     * Case 4,two SyncResult, the 1st is later than SyncTime, which needs to adjust
     * */
    public void testTwoWithFirstLate() throws Exception {
        final long syncTime = 1448209400;
        final int actCount = 2;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime + INTERVAL_DURATION - 60 * actCount + 1, actCount)); // the whole group need timestamp correct
        data.add(getSyncResultByEnd(syncTime - 10, actCount));

        printTimeStamp(data);
        int rslt = timestampCorrector.correctTimestamp(data, syncTime);
        printTimeStamp(data);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - 1, data.get(1).getTailEndTime());
        assertEquals(syncTime - actCount * 60, data.get(1).getHeadStartTime());
        assertEquals(syncTime - 1 - actCount * 60, data.get(0).getTailEndTime());
        assertEquals(syncTime - actCount * 2 * 60, data.get(0).getHeadStartTime());
    }

    /**
     * Case 5, two SyncResult, both need to correct
     * */
    public void testTwoWithFirstEarly() throws Exception {
        final long syncTime = 1448209580;
        final int actCount = 2;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime - actCount * 60 + 1, actCount));
        data.add(getSyncResultByEnd(syncTime + 1, actCount)); // 2nd SyncResult needs to correct as well

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - 1, data.get(1).getTailEndTime());
        assertEquals(syncTime - actCount * 60, data.get(1).getHeadStartTime());
        assertEquals(syncTime - actCount * 60 - 1, data.get(0).getTailEndTime()); // time interval between SyncResult is 1 sec
        assertEquals(syncTime - actCount * 2 * 60, data.get(0).getHeadStartTime());
    }

    /**
     * Case 6, two SyncResult, the 1st is earlier than MagicTime, the 2nd does not need adjust
     * */
    public void testTwoWithFirstCorrectSecondLessThanMAGIC() throws Exception {
        final long syncTime = 1448209400;
        final int actCount = 2;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByStart(MAGIC_TIME - 1, actCount));
        data.add(getSyncResultByEnd(syncTime - 1, actCount));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - actCount * 60, data.get(1).getHeadStartTime());
        assertEquals(syncTime - actCount * 60 - 1, data.get(0).getTailEndTime());
        assertEquals(syncTime - actCount * 2 * 60, data.get(0).getHeadStartTime());
    }

    /**
     * Case 7, two SyncResult, the 2nd is later than SyncTime
     * */
    public void testTwoWithSecondOverSyncTime() throws Exception {
        final long syncTime = 1448209400;
        final int actCount = 2;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime - INTERVAL_DURATION - actCount * 60, actCount));
        data.add(getSyncResultByEnd(syncTime + 1, actCount)); // in correcting, the endTime needs to move the SyncTime - 1

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - actCount * 60, data.get(1).getHeadStartTime());
        assertEquals(syncTime - actCount * 60 - INTERVAL_DURATION, data.get(0).getTailEndTime()); // the 1st SyncResult does not need adjust
    }

    /**
     * Case 8, two SyncResult, divided to 2 groups, the 1st one needs to adjust
     * */
    public void testTwoCorrect() throws Exception {
        final long syncTime = 1448209400;
        final int actCount = 2;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime - 1 - actCount * 60 - INTERVAL_DURATION - 1, actCount));
        data.add(getSyncResultByEnd(syncTime - 1, actCount));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - 1, data.get(1).getTailEndTime());
        assertEquals(syncTime - actCount * 60, data.get(1).getHeadStartTime());
        assertEquals(syncTime - 1 - actCount * 60, data.get(0).getTailEndTime());
    }

    /**
     * Case 9, two SyncResult, both are later than expected, both need to adjust timestamp
     * */
    public void testTwoWithFirstOverSecondOver() throws Exception {
        final long syncTime = 1448209400;
        final int actCount = 2;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime + INTERVAL_DURATION, actCount));
        data.add(getSyncResultByEnd(syncTime + 1, actCount));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - 1, data.get(1).getTailEndTime());
        assertEquals(syncTime - actCount * 60, data.get(1).getHeadStartTime());
        assertEquals(syncTime - actCount * 2 * 60, data.get(0).getHeadStartTime());
        assertEquals(syncTime - actCount * 60 - 1, data.get(0).getTailEndTime());
    }

    /**
     * Case 10, two SyncResult, each with 1 Activity, both need to adjust timestamp
     * */
    public void testTwoEachOfOneActivity() throws Exception {
        final long syncTime = 1448018395;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByFront(new long[]{1448009389L})); // [1448009389, 1448009448]
        data.add(getSyncResultByFront(new long[]{1368010889L})); // [1368010889, 1368010948], much earlier than syncTime
        int result = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(result, 0);
        assertEquals(syncTime - 1, data.get(1).getTailEndTime());
        assertEquals(syncTime - 60, data.get(1).getHeadStartTime());
        assertEquals(syncTime - 61, data.get(0).getTailEndTime());
        assertEquals(syncTime - 120, data.get(0).getHeadStartTime());
    }

    /**
     * Case 11, with some real raw data
     * */
    public void testRealCase() throws Exception {
        long[][] data = new long[][]{
            {1363768747, 1363829406, 1011},
            {1363829880, 1363890599, 1012},
            {1363890547, 1363951266, 1012},
            {1363951140, 1364011859, 1012},
            {1364011800, 1364072519, 1012},
            {1364072466, 1364132405, 999}};

        List<SyncResult> testData = new ArrayList<>();
        for (int i = data.length - 1; i >= 0; i--) {
            testData.add(new FakeSyncResult(data[i][0], data[i][1], (int) data[i][2]));
        }
        long syncTime = System.currentTimeMillis() / 1000;
        int result = timestampCorrector.correctTimestamp(testData, syncTime);

        assertEquals(result, 0);
    }

    class FakeSyncResult extends SyncResult {
        public FakeSyncResult(long start, long end, int mins) {
            super();
            mActivities.add(getActivity(start, start + 1));
            for (int i = 2; i < mins; i++) {
                mActivities.add(getActivity(start + i, start + i + 1));
            }
            mActivities.add(getActivity(end - 1, end));
        }
    }

    private SyncResult getSyncResultByEnd(long end, int cnt) {
        SyncResult result = new SyncResult();
        for (int i = 0; i < cnt; i++) {
            long startTime = end;
            startTime -= (60 * i + 59);
            result.mActivities.add(0, getActivity(startTime, startTime + 59));
        }
        return result;
    }

    private SyncResult getSyncResultByStart(long start, int cnt) {
        SyncResult result = new SyncResult();
        for (int i = 0; i < cnt; i++) {
            long endTime = start;
            endTime += i == 0 ? 59 : 60 * i + 59;
            result.mActivities.add(getActivity(start + 60 * i, endTime));
        }
        return result;
    }

    private Activity getActivityByFront(long start) {
        return new Activity(start, start + 59, 0, 0, 0);
    }

    private SyncResult getSyncResultByFront(long[] timeStamps) {
        SyncResult result = new SyncResult();
        for (long timeStamp : timeStamps) {
            result.mActivities.add(getActivityByFront(timeStamp));
        }
        return result;
    }

    private Activity getActivity(long start, long end) {
        return new Activity(start, end, 0, 0, 0);
    }

    private void printTimeStamp(List<SyncResult> data) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < data.size(); i++) {
            SyncResult result = data.get(i);
            builder.append(result.getHeadStartTime())
                    .append(" - ")
                    .append(result.getTailEndTime())
                    .append("(").append(result.getTotalMinutes()).append(")")
                    .append(" || ");
        }
        Log.w(TAG, builder.toString());
    }


    private void printTimeStampDetail(List<SyncResult> data) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < data.size(); i++) {
            SyncResult result = data.get(i);
            for (Activity activity : result.mActivities) {
                builder.append(activity.mStartTimestamp)
                        .append("-")
                        .append(activity.mEndTimestamp)
                        .append(String.format("(%d)", activity.mEndTimestamp - activity.mStartTimestamp + 1))
                        .append(", ");
            }
            builder.append("[").append(result.getTotalMinutes()).append("]")
                    .append(" || ");
        }
        Log.w(TAG, builder.toString());
    }

    private void assertHasCorrectSecond(long syncTime, List<SyncResult> data, int rslt) {
        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - 60 * 2, data.get(0).getTailEndTime());
        assertEquals(syncTime, data.get(1).getTailEndTime());
    }
}
