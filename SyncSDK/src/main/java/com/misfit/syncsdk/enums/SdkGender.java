package com.misfit.syncsdk.enums;

import android.support.annotation.IntDef;

import com.misfit.cloud.algorithm.models.GenderShine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * a data model to reflect GenderShine in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkGender {
    public final static int MALE = 0;
    public final static int FEMALE = 1;

    @IntDef({MALE, FEMALE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gender {}

    private SdkGender(){}

    public static GenderShine convert2GenderShine(int sdkGenderValue) {
        if (sdkGenderValue == FEMALE) {
            return GenderShine.FEMALE;
        } else if (sdkGenderValue == MALE) {
            return GenderShine.MALE;
        } else {
            return GenderShine.MALE;
        }
    }
}
