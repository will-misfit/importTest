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
    private final static int INTERVAL_DURATION = 300;
    private final static long MAGIC_TIME = 1369008000L;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        timestampCorrector = new TimestampCorrectorNew();
    }

    public void testOnlyOneOver() throws Exception {
        long syncTime = 1448209300;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime + INTERVAL_DURATION, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(rslt, 0);
        assertEquals(data.get(0).getTailEndTime(), syncTime);
    }

    public void testOnlyCorrect() throws Exception {
        long syncTime = 1448209300;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime, data.get(0).getTailEndTime());
    }

    public void testOnlyUnderTheEdge() throws Exception {
        long syncTime = 1448209300;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime - INTERVAL_DURATION, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - INTERVAL_DURATION, data.get(0).getTailEndTime());
    }

    public void testTwoWithFirstCorrectSecondOver() throws Exception {
        long syncTime = 1448209400;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime + INTERVAL_DURATION - 60 * 2 + 1, 2));
        data.add(getSyncResultByEnd(syncTime, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - 60 * 2, data.get(0).getTailEndTime());
        assertEquals(syncTime, data.get(1).getTailEndTime());
    }

    public void test14() throws Exception {
        long tailTime = 1448209400;
        long syncTime = 1448209580;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(tailTime + INTERVAL_DURATION - 60 * 2 + 1, 2));
        data.add(getSyncResultByEnd(tailTime, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime - 60 * 2, data.get(0).getTailEndTime());
        assertEquals(syncTime, data.get(1).getTailEndTime());
    }

    public void testTwoWithFirstCorrectSecondLessThanMAGIC() throws Exception {
        long syncTime = 1448209400;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByStart(MAGIC_TIME - 1, 2));
        data.add(getSyncResultByEnd(syncTime, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertHasCorrectSecond(syncTime, data, rslt);
    }

    public void testTwoWithFirstOverSecondLess() throws Exception {
        long syncTime = 1448209400;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime - INTERVAL_DURATION - 60 * 2, 2));
        data.add(getSyncResultByEnd(syncTime + 1, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertHasCorrectSecond(syncTime, data, rslt);
    }

    /**
     * do nothing
     *
     * @throws Exception
     */
    public void testTwoWithFirstCorrectSecondLess() throws Exception {
        long syncTime = 1448209400;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime - INTERVAL_DURATION - 60 * 2 - 1, 2));
        data.add(getSyncResultByEnd(syncTime, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertHasCorrectSecond(syncTime, data, rslt);
    }

    public void testTwoWithFirstLessSecondOver() throws Exception {
        long syncTime = 1448209400;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime + INTERVAL_DURATION - 60 * 2 + 3, 2));
        data.add(getSyncResultByEnd(syncTime - 1, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime + INTERVAL_DURATION - 60 * 2 + 3, data.get(0).getTailEndTime());
        assertEquals(syncTime, data.get(1).getTailEndTime());
    }

    public void testTwoWithFirstOverSecondOver() throws Exception {
        long syncTime = 1448209400;
        List<SyncResult> data = new ArrayList<>();
        data.add(getSyncResultByEnd(syncTime + INTERVAL_DURATION, 2));
        data.add(getSyncResultByEnd(syncTime + 1, 2));

        int rslt = timestampCorrector.correctTimestamp(data, syncTime);

        assertEquals(TimestampCorrectorNew.OK, rslt);
        assertEquals(syncTime + INTERVAL_DURATION, data.get(0).getTailEndTime());
        assertEquals(syncTime, data.get(1).getTailEndTime());
    }

    public void testCaseOne() throws Exception {
        long syncTime = 1448018395;
        List<SyncResult> caseOne = new ArrayList<>();
        caseOne.add(getSyncResultByFront(new long[]{1448009389L}));
        caseOne.add(getSyncResultByFront(new long[]{1368010889L}));
        int result = timestampCorrector.correctTimestamp(caseOne, syncTime);
        assertEquals(result, 0);
    }

    public void testRealCase() throws Exception {
        long[][] data = new long[][]{{1363768747, 1363829406, 1011}, {1363829880, 1363890599, 1012}, {1363890547, 1363951266, 1012}, {1363951140, 1364011859, 1012}, {1364011800, 1364072519, 1012}, {1364072466, 1364132405, 999}};

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
            result.mActivities.add(0, getActivity(startTime, end - i * 60));
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
