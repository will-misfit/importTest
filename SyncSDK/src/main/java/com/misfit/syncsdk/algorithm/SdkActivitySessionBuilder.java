package com.misfit.syncsdk.algorithm;

import android.util.Log;

import com.misfit.cloud.algorithm.algos.ActivitySessionsShineAlgorithm;
import com.misfit.cloud.algorithm.models.ACEEntryVect;
import com.misfit.cloud.algorithm.models.ActivityChangeTagShineVect;
import com.misfit.cloud.algorithm.models.ActivitySessionShine;
import com.misfit.cloud.algorithm.models.ActivitySessionShineVect;
import com.misfit.cloud.algorithm.models.ActivityShineVect;
import com.misfit.cloud.algorithm.models.GapSessionShine;
import com.misfit.cloud.algorithm.models.GapSessionShineVect;
import com.misfit.cloud.algorithm.models.ProfileShine;
import com.misfit.cloud.algorithm.models.SWLEntryVect;
import com.misfit.cloud.algorithm.models.SessionShine;
import com.misfit.cloud.algorithm.models.SessionShineVect;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.model.SdkActivityChangeTag;
import com.misfit.syncsdk.model.SdkActivitySession;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.utils.CheckUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * to build up SdkActivitySession which can be converted to ActivitySession in flagship app namespace
 * to make use of algorithm library API, some information needs to be queried from flagship app:
 *   ActivityChangeTag among a mDuration
 *   UserProfile
 *   Timezone change
 */
public class SdkActivitySessionBuilder {

    private final static String TAG = "SDKActSessionBuilder";

    public static List<SdkActivitySession> buildSdkActivitySessionForShine(ActivityShineVect activityShineVect,
                                                                           ACEEntryVect aceEntryVect,
                                                                           SWLEntryVect swlEntryVect,
                                                                           SyncCalculationCallback syncCalculationCallback) {
        Log.d(TAG, "buildSdkActivitySessionForShine()");
        List<SdkActivitySession> result = new ArrayList<>();
        ActivitySessionsShineAlgorithm activitySessionsShineAlgorithm = new ActivitySessionsShineAlgorithm();
        ActivitySessionShineVect activitySessionShineVect = new ActivitySessionShineVect();
        GapSessionShineVect gapSessionShineVect = new GapSessionShineVect();
        activitySessionsShineAlgorithm.buildActivitySession(activityShineVect, aceEntryVect, swlEntryVect,
                activitySessionShineVect, gapSessionShineVect);
        if (activitySessionShineVect.isEmpty() && gapSessionShineVect.isEmpty()) {
            Log.d(TAG, "buildSdkActivitySessionForShine(), no session to build");
            return result;
        }

        int[] sessionShineVectStartEndTime = getActivitySessionsStartEndTime(convertSubVect2SessionShineVect(activitySessionShineVect));
        int[] gapSessionShineVectStartEndTime = getActivitySessionsStartEndTime(convertSubVect2SessionShineVect(gapSessionShineVect));
        int[] wholeSessionStartEndTime = AlgorithmUtils.getStartEndTimeFromTwoSessions(sessionShineVectStartEndTime, gapSessionShineVectStartEndTime);

        // get ActivityTag change list and user profile from App via callback
        List<SdkActivityChangeTag> sdkChangeTags = syncCalculationCallback.getSdkActivityChangeTagList(
            wholeSessionStartEndTime[0], wholeSessionStartEndTime[1]);
        SdkProfile sdkProfile = syncCalculationCallback.getUserProfile();

        ActivitySessionShineVect resultActivitySessionShineVect = new ActivitySessionShineVect();
        GapSessionShineVect resultGapSessionShineVect = new GapSessionShineVect();
        activitySessionsShineAlgorithm.buildUserSessions(activitySessionShineVect, gapSessionShineVect,
                resultActivitySessionShineVect, resultGapSessionShineVect,
                buildActivityChangeTagShineVect(sdkChangeTags),
                buildProfileShine(sdkProfile));

        result = convertSessionShineVect2SdkActivitySessionList(resultActivitySessionShineVect, resultGapSessionShineVect);
        return result;
    }

    /**
     * as GapSession should stay inside ActivitySession list, does the output SdkActivitySession List need to be sorted in order?
     * */
    private static List<SdkActivitySession> convertSessionShineVect2SdkActivitySessionList(
        ActivitySessionShineVect activitySessionShineVect, GapSessionShineVect gapSessionShineVect) {

        Log.d(TAG, String.format("convertSessionShineVect2SdkActivitySessionList: activity sessions size: %d, gap sessions size: %d",
            activitySessionShineVect.size(), gapSessionShineVect.size()));
        
        List<SdkActivitySession> result = new ArrayList<>();
        for (int i = 0; i < activitySessionShineVect.size(); i++) {
            SdkActivitySession sdkActivitySession = convertActivitySessionShine2SdkActivitySession(activitySessionShineVect.get(i));
            result.add(sdkActivitySession);
            Log.d(TAG, String.format("SdkActivitySession: timestamp is %d, points is %d", sdkActivitySession.getStartTime(),
                sdkActivitySession.getPoints()));
        }
        for(int i = 0; i < gapSessionShineVect.size(); i++) {
            SdkActivitySession sdkActivitySession = convertGapSessionShine2SdkActivitySession(gapSessionShineVect.get(i));
            result.add(sdkActivitySession);
            Log.d(TAG, String.format("SdkActivitySession of Gap session: timestamp is %d, points is %d", sdkActivitySession.getStartTime(),
                sdkActivitySession.getPoints()));
        }
        return result;
    }

    private static SdkActivitySession convertActivitySessionShine2SdkActivitySession(ActivitySessionShine activitySessionShine) {
        SdkActivitySession result = buildBaseSdkActivitySession(activitySessionShine);
        result.setRawPoints(activitySessionShine.getRawPoint());
        result.setIsGapSession(false);
        result.setLaps(activitySessionShine.getLaps());
        result.setActivityType(activitySessionShine.getType());
        return result;
    }

    private static SdkActivitySession convertGapSessionShine2SdkActivitySession(GapSessionShine gapSessionShine) {
        SdkActivitySession result = buildBaseSdkActivitySession(gapSessionShine);
        result.setIsGapSession(true);
        return result;
    }

    private static SdkActivitySession buildBaseSdkActivitySession(SessionShine sessionShine) {
        SdkActivitySession result = new SdkActivitySession();
        result.setStartTime(sessionShine.getStartTime());
        result.setDuration(sessionShine.getDuration());
        result.setPoints(sessionShine.getPoint());
        result.setDistance(sessionShine.getDistance());
        result.setSteps(sessionShine.getStep());
        result.setCalories(sessionShine.getCalorie());
        result.setSessionType(sessionShine.getSType());
        return result;
    }

    public static ActivityChangeTagShineVect buildActivityChangeTagShineVect(List<SdkActivityChangeTag> changeTagList) {
        ActivityChangeTagShineVect result = new ActivityChangeTagShineVect();
        if (CheckUtils.isCollectionEmpty(changeTagList)) {
            return result;
        }
        for(SdkActivityChangeTag changeTag : changeTagList) {
            result.add(changeTag.convert2ActivityChangeTagShine());
        }
        return result;
    }

    public static ProfileShine buildProfileShine(SdkProfile sdkProfile) {
        if (sdkProfile == null) {
            return new ProfileShine();
        }
        return sdkProfile.convert2ProfileShine();
    }

    private static SessionShineVect convertSubVect2SessionShineVect(ActivitySessionShineVect activitySessionShineVect){
        SessionShineVect resultVect = new SessionShineVect();
        if (null == activitySessionShineVect) {
            return resultVect;
        }
        final int n = (int)activitySessionShineVect.size();
        for (int i = 0; i < n; i++){
            resultVect.add(activitySessionShineVect.get(i));
        }
        return resultVect;
    }

    private static SessionShineVect convertSubVect2SessionShineVect(GapSessionShineVect gapSessionShineVect){
        SessionShineVect resultVect = new SessionShineVect();
        if (null == gapSessionShineVect) {
            return resultVect;
        }
        final int n = (int)gapSessionShineVect.size();
        for (int i = 0; i < n; i++){
            resultVect.add(gapSessionShineVect.get(i));
        }
        return resultVect;
    }

    /**
     * actually the SessionShineVect should be sorted in ascending order already
     * so this method may be unnecessary to run to get the [start, end]
     * */
    private static int[] getActivitySessionsStartEndTime(SessionShineVect sessionShineVect) {
        if (sessionShineVect == null || sessionShineVect.isEmpty()) {
            return new int[]{0, 0};
        }

        int sessionsStartTime = sessionShineVect.get(0).getStartTime();
        int sessionsEndTime = sessionShineVect.get(0).getStartTime();
        for (int i = 1; i < sessionShineVect.size(); i++) {
            SessionShine sessionShine = sessionShineVect.get(i);
            if (sessionShine.getStartTime() < sessionsStartTime) {
                sessionsStartTime = sessionShine.getStartTime();
            } else if (sessionShine.getStartTime() > sessionsEndTime) {
                sessionsEndTime = sessionShine.getStartTime();
            }
        }
        int[] startEndTime = new int[2];
        startEndTime[0] = sessionsStartTime;
        startEndTime[1] = sessionsEndTime;
        return startEndTime;
    }
}
