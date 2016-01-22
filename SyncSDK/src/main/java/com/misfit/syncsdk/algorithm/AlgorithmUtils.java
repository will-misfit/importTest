package com.misfit.syncsdk.algorithm;

import android.support.annotation.NonNull;
import android.util.Log;

import com.misfit.ble.shine.result.TapEvent;
import com.misfit.cloud.algorithm.models.ActivityShine;
import com.misfit.cloud.algorithm.models.ActivityShineVect;
import com.misfit.cloud.algorithm.models.SWLEntry;
import com.misfit.cloud.algorithm.models.SWLEntryVect;
import com.misfit.cloud.algorithm.models.SWLLapInfoEntry;
import com.misfit.cloud.algorithm.models.SWLLapInfoEntryVect;
import com.misfit.cloud.algorithm.models.TagFlash;
import com.misfit.ble.shine.result.Activity;
import com.misfit.ble.shine.result.SessionEvent;
import com.misfit.ble.shine.result.SwimLap;
import com.misfit.ble.shine.result.SwimSession;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.ble.shine.result.TapEventSummary;
import com.misfit.ble.shine.result.Event;
import com.misfit.syncsdk.utils.CheckUtils;
import com.misfit.syncsdk.model.TagEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 */
public class AlgorithmUtils {

    private static final String TAG = "AlgorithmUtils";

    public static List<Long> bookmarkTimestamps;
    public static List<TagEvent> tagEvents;

    private static final int VARIANCE_MISSING = -1;

    private static SyncResultComparator sSyncResultComparator = new SyncResultComparator();
    private static ActivityComparator sActivityComparator = new ActivityComparator();
    private static TapEventSumComparator sTapEventSumComparator = new TapEventSumComparator();
    private static SessionEventComparator sSessionEventComparator = new SessionEventComparator();
    private static SwimSessionComparator sSwimSessionComparator = new SwimSessionComparator();
    private static EventComparator sEventComparator = new EventComparator();

    public static ActivityShineVect convertSdkActivityToShineActivityForShine(@NonNull List<Activity> activities,
                                                                              @NonNull List<TapEventSummary> tapEventSummaries) {
        ActivityShineVect activityShineVect = new ActivityShineVect();
        if (CheckUtils.isCollectionEmpty(activities)) {
            return activityShineVect;
        }

        List<Long> tripleTapTimestamps = importTripleTapBookmarks(tapEventSummaries);
        Collections.sort(tripleTapTimestamps);
        Log.d(TAG, String.format("convertSdkActivityToShineActivityForShine: activity size %d, tripleTapTimestamps size %d",
            activities.size(), tripleTapTimestamps.size()));

        final int COUNT = tripleTapTimestamps.size();
        int tripleIndex = 0;
        for (Activity activity : activities) {
            ActivityShine activityShine = buildBaseActivityShine(activity);
            if (tripleIndex < COUNT) {
                long tripleTapTimeStamp = tripleTapTimestamps.get(tripleIndex);
                if (tripleTapTimeStamp >= activity.mStartTimestamp && tripleTapTimeStamp <= activity.mEndTimestamp) {
                    activityShine.setTrippleTapCount(1);
                    tripleIndex++;
                }
            }
            activityShineVect.add(activityShine);
        }
        return activityShineVect;
    }

    public static ActivityShineVect convertSdkActivityToShineActivityForFlash(List<Activity> activities, List<SessionEvent> sessionEvents) {
        ActivityShineVect activityShineVect = new ActivityShineVect();
        List<TagEvent> tagEvents = importTaginTagoutBookmarks(sessionEvents, activities);
        Collections.sort(tagEvents, new TagEventComparator());

        final int COUNT = tagEvents.size();
        int tagIndex = 0;
        for (Activity activity : activities) {
            ActivityShine activityShine = buildBaseActivityShine(activity);
            if (tagIndex < COUNT) {
                TagEvent tagEvent = tagEvents.get(tagIndex);
                if (tagEvent.getTaggedTimestamp() >= activity.mStartTimestamp && tagEvent.getTaggedTimestamp() <= activity.mEndTimestamp) {
                    switch (tagEvent.getTagType()) {
                        case TagEvent.TAG_IN:
                            activityShine.setTag(TagFlash.TAG_IN);
                            break;
                        case TagEvent.TAG_OUT:
                            activityShine.setTag(TagFlash.TAG_OUT);
                            break;
                        default:
                            activityShine.setTag(TagFlash.TAG_NON);
                            break;
                    }
                    tagIndex++;
                }
            }
            activityShineVect.add(activityShine);
        }
        return activityShineVect;
    }

    public static SWLEntryVect convertSwimSessionsToSWLEntry(List<SwimSession> swimSessions){
        SWLEntryVect result = new SWLEntryVect();
        if(null == swimSessions || swimSessions.isEmpty()){
            return result;
        }

        for(SwimSession ss : swimSessions){
            result.add(buildSWLEntryFromSwimSession(ss));
        }
        return result;
    }

    public static List<TagEvent> importTaginTagoutBookmarks(List<SessionEvent> sessionEvents, List<Activity> activities) {
        Log.d(TAG, "importTaginTagoutBookmarks");
        if (sessionEvents == null || sessionEvents.size() == 0) {
            return null;
        }
        tagEvents = new ArrayList<TagEvent>();
        for (SessionEvent sessionEvent : sessionEvents) {
            if (sessionEvent.mType == SessionEvent.SESSION_EVENT_TYPE_START) {
                TagEvent tagEvent = new TagEvent(sessionEvent.mTimestamp, TagEvent.TAG_IN);
                tagEvents.add(tagEvent);
                Log.d(TAG, "Add tag in event: " + sessionEvent.mTimestamp);
            } else if (sessionEvent.mType == SessionEvent.SESSION_EVENT_TYPE_END) {
                TagEvent tagEvent = new TagEvent(sessionEvent.mTimestamp, TagEvent.TAG_OUT);
                tagEvents.add(tagEvent);
                Log.d(TAG, "Add tag out event " + sessionEvent.mTimestamp);
            }
        }

        tagEvents = reOrganizeTagPairs(tagEvents);
        if (tagEvents != null && tagEvents.size() != 0) {
            fillTagOut(tagEvents, activities);
            splitTagsCrossTwoDays(tagEvents);
        }
        return tagEvents;
    }


    /**
     * Re-organize the Tag in & out pairs
     * @param tagEventsUnOrganized
     * @return
     */
    private static List<TagEvent> reOrganizeTagPairs(List<TagEvent> tagEventsUnOrganized) {
        if (tagEventsUnOrganized == null || tagEventsUnOrganized.size() == 0) {
            return null;
        }

        List<TagEvent> tagEventOrganized = new ArrayList<TagEvent>();

        sortTagEventsByAsc(tagEventsUnOrganized);
        //tag out, tag in, tag out, tag in, tag in
        int currentRespectTagEvent = TagEvent.TAG_IN;
        long previousTagInTimestamp = 0l;
        for (TagEvent tagEvent : tagEventsUnOrganized) {
            if (tagEvent.getTagType() != currentRespectTagEvent) {
                if (currentRespectTagEvent == TagEvent.TAG_OUT) {
                    //need tag out but meet tag in again.
                    // long midNight = DateUtil.getMidNight(previousTagInTimestamp);
                    long midNight = 0l;
                    TagEvent fixedTagoutEvent = new TagEvent(Math.min(midNight, tagEvent.getTaggedTimestamp() - 1), TagEvent.TAG_OUT);
                    fixedTagoutEvent.setFixed(true);
                    tagEventOrganized.add(fixedTagoutEvent);
                    tagEventOrganized.add(tagEvent);
                    currentRespectTagEvent = TagEvent.TAG_OUT;
                } else {
                    //User syncs, app sees tagout, but not tagin,Ignore that case
                    continue;
                }
            } else {
                if (currentRespectTagEvent == TagEvent.TAG_IN) {
                    previousTagInTimestamp = tagEvent.getTaggedTimestamp();
                }
                tagEventOrganized.add(tagEvent);
                currentRespectTagEvent = 1 + currentRespectTagEvent % 2;
            }
        }

        return tagEventOrganized;

    }

    public static void sortTagEventsByAsc(List<TagEvent> tagEventsUnOrganized) {
        Collections.sort(tagEventsUnOrganized, tagAscComparator);
    }

    /**
     * If user sync Flash in Tag In mode, we should Tag Out by code
     *
     * @param tagEvents
     * @param activities
     */

    private static void fillTagOut(List<TagEvent> tagEvents, List<Activity> activities) {
        if (tagEvents.get(tagEvents.size() - 1).getTagType() == TagEvent.TAG_IN) {
            if (activities.size() == 0) {
                //If no new activities, just remove the tag in event
                tagEvents.remove(tagEvents.size() - 1);
            } else {
                //If have new activities, tag out by the last activity timestamp.
                long lastActivityTimestamp = activities.get(activities.size() - 1).mEndTimestamp + 1;
                TagEvent tagOut = new TagEvent(lastActivityTimestamp, TagEvent.TAG_OUT);
                tagOut.setFixed(true);
                tagEvents.add(tagOut);
                Log.d(TAG, "fixed tag out " + lastActivityTimestamp);
            }
        }
    }

    /**
     * If a activity session across two days, we should add tag in and tag out event at midnight
     *
     * @param tagEvents
     */

    private static void splitTagsCrossTwoDays(List<TagEvent> tagEvents) {
        List<TagEvent> newTagEvents = new ArrayList<TagEvent>();
        for (int i = 0; i < tagEvents.size(); i = i + 2) {

            long startTime = tagEvents.get(i).getTaggedTimestamp();
            long endTime = tagEvents.get(i + 1).getTaggedTimestamp();
            long midNight = 0;
            // long midNight = DateUtil.getMidNight(mStartTime);

            if (endTime > midNight) {
                TagEvent tagEventYest = new TagEvent(midNight, TagEvent.TAG_OUT);
                TagEvent tagEventToday = new TagEvent(midNight + 1, TagEvent.TAG_IN);
                newTagEvents.add(tagEventYest);
                newTagEvents.add(tagEventToday);
            }
        }

        if (newTagEvents.size() > 0) {
            tagEvents.addAll(newTagEvents);
            sortTagEventsByAsc(tagEvents);
        }
    }

    private static Comparator<TagEvent> tagAscComparator = new Comparator<TagEvent>() {
        @Override
        public int compare(final TagEvent lhs, final TagEvent rhs) {
            if (lhs.getTaggedTimestamp() > rhs.getTaggedTimestamp()) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    /**
    * @InParameter: Activity in ShineSDK namespace
     *@Return: ActivityShine in Algorithm namespace
    * */
    private static ActivityShine buildBaseActivityShine(Activity activity) {
        ActivityShine activityShine = new ActivityShine();
        activityShine.setStartTime((int) activity.mStartTimestamp);
        activityShine.setBipedalCount(activity.mBipedalCount);
        activityShine.setEndTime((int) activity.mEndTimestamp);
        activityShine.setPoint(activity.mPoints);
        activityShine.setVariance(activity.mVariance);
        return activityShine;
    }

    private static SWLEntry buildSWLEntryFromSwimSession(SwimSession swimSession){
        SWLEntry swlEntry = new SWLEntry();
        swlEntry.setStartTime((int) swimSession.mStartTime);
        swlEntry.setEndTime((int) swimSession.mEndTime);
        swlEntry.setNbOflaps((int) swimSession.mNumberOfLaps);
        SWLLapInfoEntryVect swlLapsVect = new SWLLapInfoEntryVect();

        for(SwimLap lapSession : swimSession.mSwimLaps){
            SWLLapInfoEntry swlLapEntry = new SWLLapInfoEntry();
            swlLapEntry.setDuration(lapSession.mDuration);
            swlLapEntry.setEndTime(lapSession.mEndTime);
            swlLapEntry.setStrokes((int)lapSession.mStrokes);
            swlLapEntry.setSvm((int)lapSession.mSvm);
            swlLapsVect.add(swlLapEntry);
        }
        swlEntry.setLaps(swlLapsVect);
        return swlEntry;
    }

    public static List<Long> importTripleTapBookmarks(List<TapEventSummary> tapEvents) {
        bookmarkTimestamps = new ArrayList<>();
        if (CheckUtils.isCollectionEmpty(tapEvents)) {
            return bookmarkTimestamps;
        }
        for (TapEventSummary tapEvent : tapEvents) {
            if (tapEvent.mTapType == TapEventSummary.TAP_TYPE_TRIPLE) {
                bookmarkTimestamps.add(tapEvent.mTimestamp);
            }
        }
        return bookmarkTimestamps;
    }

    public static int[] getStartEndTimeFromTwoSessions(int[] session1, int[] session2) {
        int[] startEndTime = new int[2];
        if (session1[0] == 0) {
            startEndTime[0] = session2[0];
            startEndTime[1] = session2[1];
        } else if (session2[0] == 0){
            startEndTime[0] = session1[0];
            startEndTime[1] = session1[1];
        } else {
            startEndTime[0] = Math.min(session1[0], session2[0]);
            startEndTime[1] = Math.max(session1[1], session2[1]);
        }
        Log.d(TAG, "start time: " + startEndTime[0] + " end time: " + startEndTime[1]);
        return startEndTime;
    }

    /**
     * with latterSyncResult.mActivities[0].mStartTime as tag, remove overlapped sub list from tail of preSyncResult.mActivities
     * */
    public static void handleNotContinuousActivities(SyncResult preSyncResult, SyncResult latterSyncResult) {
        if (CheckUtils.isCollectionEmpty(latterSyncResult.mActivities) || CheckUtils.isCollectionEmpty(preSyncResult.mActivities)) {
            return;
        }
        Log.d(TAG, "activity size before filter " + preSyncResult.mActivities.size());
        long latterFirstStartTime = latterSyncResult.mActivities.get(0).mStartTimestamp;
        long expectedStartTime = latterFirstStartTime - 60;
        final int originSize = preSyncResult.mActivities.size();
        long preLastStartTime = preSyncResult.mActivities.get(originSize - 1).mStartTimestamp;
        //handle overlap activities
        if (preLastStartTime >= latterFirstStartTime) {
            filterOverlapActivities(preSyncResult, latterFirstStartTime);
        } else if (preLastStartTime < expectedStartTime) {
            // padding a placeholder activity is to keep the interval between activities inside a ActivitySession is less than 60s
            paddingGapActivities(preSyncResult, preLastStartTime, expectedStartTime);
        }
        Log.d(TAG, "activity size after filter " + preSyncResult.mActivities.size());
    }

    public static void filterOverlapActivities(SyncResult syncResult, long firstStartTime) {
        final int n = syncResult.mActivities.size();
        int idx = findIdxToInsert(syncResult.mActivities, firstStartTime, sActivityComparator, sActivityComparator);
        if (idx >= 0 && idx < n) {
            syncResult.mActivities.subList(idx, n).clear();
        }
    }

    private static void paddingGapActivities(SyncResult syncResult, long lastActivityStartTime, long expectActivityStartTime) {
        while (lastActivityStartTime < expectActivityStartTime) {
            long newActivityStartTime = lastActivityStartTime + 60;
            long newActivityEndTime = newActivityStartTime + 59;
            Activity activity =
                    new Activity(newActivityStartTime, newActivityEndTime, 0, 0, VARIANCE_MISSING);
            syncResult.mActivities.add(activity);
            lastActivityStartTime = newActivityStartTime;
        }
    }

    /**
     * convert K type tag to V type item
     * */
    public interface Converter<K, V> {
        V fakeFrom(K k);
    }

    public static void sortSyncResultList(List<SyncResult> syncResultList) {
        Collections.sort(syncResultList, sSyncResultComparator);
    }

    /**
     * compare two SyncResult by their head Activity.mStartTime
     * */
    static class SyncResultComparator implements Comparator<SyncResult> {
        @Override
        public int compare(SyncResult lResult, SyncResult rResult) {
            if (null == lResult || lResult.mActivities.isEmpty())    return -1;
            if (null == rResult || rResult.mActivities.isEmpty())    return -1;

            long lStartTime = lResult.mActivities.get(0).mStartTimestamp;
            long rStartTime = rResult.mActivities.get(0).mStartTimestamp;
            if (lStartTime < rStartTime) {
                return -1;
            } else if (lStartTime > rStartTime) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * for ascending sort
     * */
    static class ActivityComparator implements Comparator<Activity>, Converter<Long, Activity> {
        @Override
        public int compare(Activity lActivity, Activity rActivity) {
            if (lActivity.mStartTimestamp < rActivity.mStartTimestamp) {
                return -1;
            } else if (lActivity.mStartTimestamp > rActivity.mStartTimestamp) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public Activity fakeFrom(Long startTime) {
            return new Activity(startTime, startTime, 0, 0, 0); // only startTimeStamp will be used to compare
        }
    }

    /**
     * for ascending sort
     * both of TapEventSummary and SessionEvent are subclass of Event
     * */
    static class EventComparator implements Comparator<Event> {
        @Override
        public int compare(Event lEvent, Event rEvent) {
            if (lEvent.mTimestamp < rEvent.mTimestamp) {
                return -1;
            } else if (lEvent.mTimestamp > rEvent.mTimestamp) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    static class TapEventSumComparator implements Comparator<TapEventSummary>, Converter<Long, TapEventSummary> {
        @Override
        public int compare(TapEventSummary lEvent, TapEventSummary rEvent) {
            return sEventComparator.compare(lEvent, rEvent);
        }

        @Override
        public TapEventSummary fakeFrom(Long timestamp) {
            return new TapEventSummary(timestamp, TapEvent.TAP_TYPE_TRIPLE, 0); // only timeStamp will be used to compare
        }
    }

    static class SessionEventComparator implements Comparator<SessionEvent>, Converter<Long, SessionEvent> {
        @Override
        public int compare(SessionEvent lEvent, SessionEvent rEvent) {
            return sEventComparator.compare(lEvent, rEvent);
        }

        @Override
        public SessionEvent fakeFrom(Long timestamp) {
            return new SessionEvent(timestamp, SessionEvent.SESSION_EVENT_TYPE_END); // only timStamp will be used to compare
        }
    }

    /**
     * comparator of TagEvent for ascending sort
     * */
    static class TagEventComparator implements Comparator<TagEvent> {
        @Override
        public int compare(TagEvent lTagEvent, TagEvent rTagEvent) {
            long lTime = lTagEvent.getTaggedTimestamp();
            long rTime = rTagEvent.getTaggedTimestamp();
            if (lTime < rTime) {
                return -1;
            } else if (lTime > rTime) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * @return:
     * 0 if equals to, -1 if less than, 1 if greater than
     * */
    static class SwimSessionComparator implements Comparator<SwimSession>, Converter<Long, SwimSession> {
        @Override
        public int compare(SwimSession lSwimSession, SwimSession rSwimSession) {
            long lStartTime = (long)lSwimSession.mStartTime;
            long rStartTime = (long)rSwimSession.mStartTime;
            if (lStartTime < rStartTime) {
                return -1;
            } else if (lStartTime > rStartTime) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public SwimSession fakeFrom(Long timestamp) {
            SwimSession res = new SwimSession();
            res.mStartTime = timestamp;  // only mStartTime will be used to compare
            return res;
        }
    }

    public static <K, V> int findIdxToInsert(List<V> items, K tag, Comparator<V> comparator, Converter<K, V> converter) {
        V vTag = converter.fakeFrom(tag);
        return Collections.binarySearch(items, vTag, comparator);
    }

    public static <K, V> void removeItemsBeforeTag(List<V> items, K tag, Comparator<V> comparator, Converter<K, V> converter) {
        int idx = findIdxToInsert(items, tag, comparator, converter);
        if (idx > 0 && idx <= items.size()) {
            items.subList(0, idx).clear();
        }
    }

    public static SyncResult copySycnResult(@NonNull SyncResult syncData) {
        SyncResult syncRes = new SyncResult();
        syncRes.mActivities.addAll(syncData.mActivities);
        syncRes.mTapEventSummarys.addAll(syncData.mTapEventSummarys);
        syncRes.mSessionEvents.addAll(syncData.mSessionEvents);
        syncRes.mSwimSessions.addAll(syncData.mSwimSessions);
        return syncRes;
    }

    public static void filterSyncResultInternalData(@NonNull SyncResult syncResult, long lastSyncTime) {
        removeItemsBeforeTag(syncResult.mActivities, lastSyncTime, sActivityComparator, sActivityComparator);
        removeItemsBeforeTag(syncResult.mTapEventSummarys, lastSyncTime, sTapEventSumComparator, sTapEventSumComparator);
        removeItemsBeforeTag(syncResult.mSessionEvents, lastSyncTime, sSessionEventComparator, sSessionEventComparator);
        removeItemsBeforeTag(syncResult.mSwimSessions, lastSyncTime, sSwimSessionComparator, sSwimSessionComparator);
    }

    public static SyncResult mergeSyncResults(@NonNull List<SyncResult> rawSyncDataList) {
        SyncResult syncResult = new SyncResult();
        for(SyncResult syncData : rawSyncDataList) {
            syncResult.mActivities.addAll(syncData.mActivities);
            syncResult.mTapEventSummarys.addAll(syncData.mTapEventSummarys);
            syncResult.mSessionEvents.addAll(syncData.mSessionEvents);
            syncResult.mSwimSessions.addAll(syncData.mSwimSessions);
        }
        return syncResult;
    }
}
