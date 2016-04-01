package com.misfit.syncdemo;

import com.misfit.syncsdk.enums.SdkActivityType;
import com.misfit.syncsdk.enums.SdkGender;
import com.misfit.syncsdk.enums.SdkUnit;
import com.misfit.syncsdk.model.SdkActivityTagChange;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.model.SdkResourceSettings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Provide data query to SyncDemo, including user profile, activity type change history,
 * timezone offset change history etc.
 */
public class DataSourceManager {

    /* for SynCalculationCallback.getUserProfile() */
    public final static int Male_Recommended_Age = 31;
    public final static float Male_Recommended_Height = 68.3f;
    public final static float Male_Recommended_Weight = 140.0f;

    public final static int Female_Recommended_Age = 29;
    public final static float Female_Recommended_Height = 65.5f;
    public final static float Female_Recommended_Weight = 100.0f;

    public static SdkProfile getSdkProfile(int genderInt) {
        if (genderInt == SdkGender.MALE) {
            return new SdkProfile(SdkGender.MALE, Male_Recommended_Age, Male_Recommended_Height,
                Male_Recommended_Weight, SdkUnit.WEIGHT_UNIT_US);
        } else {
            return new SdkProfile(SdkGender.FEMALE, Female_Recommended_Age, Female_Recommended_Height,
                Female_Recommended_Weight, SdkUnit.WEIGHT_UNIT_US);
        }
    }

    /* for SynCalculationCallback.getSdkActivityChangeTagList() */
    public final static int Activity_Tag_Size = 2;

    public static List<SdkActivityTagChange> getSdkActivityChangeTagList(long startTime, long endTime) {
        List<SdkActivityTagChange> result = new ArrayList<>();
        if (endTime <= startTime) {
            SdkActivityTagChange walkingActivityTag = new SdkActivityTagChange(endTime, SdkActivityType.WALKING);
            result.add(walkingActivityTag);
            return result;
        }

        long duration = endTime - startTime;
        long intervalTime = duration / Activity_Tag_Size;
        for (int i = 0; i < Activity_Tag_Size; i++) {
            result.add(new SdkActivityTagChange(startTime + i * intervalTime, SdkActivityType.WALKING));
        }
        return result;
    }

    public static final int Timezone_Offset_East_Eight = 28800;

    public static List<SdkResourceSettings> createSdkResourceSettings(int count, int durationHours) {
        List<SdkResourceSettings> result = new ArrayList<>();

        if (count == 0) {
            count = 1;
        }

        if (durationHours <= 0) {
            durationHours = 2;
        }

        int duration = 60 * 60 * durationHours;  // review backwards 10 hours
        int delta = duration / count;

        long currTime = Calendar.getInstance().getTimeInMillis() / 1000;
        long timestamp = currTime - duration;
        for(int i = 0; i < count; i++) {
            SdkResourceSettings settings = new SdkResourceSettings(timestamp, true, SdkActivityType.RUNNING,
                Timezone_Offset_East_Eight);
            result.add(settings);
            timestamp += delta;
        }
        return result;
    }

    public static long createLastSyncTime() {
        long currTime = Calendar.getInstance().getTimeInMillis() / 1000;
        int pastHours = 2;
        return currTime - 60 * 60 * pastHours;
    }
}
