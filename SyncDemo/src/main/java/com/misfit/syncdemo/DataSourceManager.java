package com.misfit.syncdemo;

import com.misfit.syncsdk.algorithm.SdkActivitySessionBuilder;
import com.misfit.syncsdk.enums.SdkActivityType;
import com.misfit.syncsdk.enums.SdkGender;
import com.misfit.syncsdk.enums.SdkUnit;
import com.misfit.syncsdk.model.SdkActivityChangeTag;
import com.misfit.syncsdk.model.SdkProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public static List<SdkActivityChangeTag> getSdkActivityChangeTagList(long startTime, long endTime) {
        List<SdkActivityChangeTag> result = new ArrayList<>();
        if (endTime <= startTime) {
            SdkActivityChangeTag walkingActivityTag = new SdkActivityChangeTag(endTime, SdkActivityType.WALKING_TYPE);
            result.add(walkingActivityTag);
            return result;
        }

        long duration = endTime - startTime;
        long intervalTime = duration / Activity_Tag_Size;
        for (int i = 0; i < Activity_Tag_Size; i++) {
            result.add(new SdkActivityChangeTag(startTime + i * intervalTime, SdkActivityType.WALKING_TYPE));
        }
        return result;
    }

    public static final int Timezone_Offset_East_Eight = 28800;

}
