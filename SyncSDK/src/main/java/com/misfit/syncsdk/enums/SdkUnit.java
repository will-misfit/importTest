package com.misfit.syncsdk.enums;

import android.support.annotation.IntDef;

import com.misfit.cloud.algorithm.models.UnitSystemShine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * a data model to reflect ActivityChangeTagShine in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkUnit {

	public final static int WEIGHT_UNIT_US = 0;
	public final static int WEIGHT_UNIT_SI = 1;

	@IntDef({WEIGHT_UNIT_US, WEIGHT_UNIT_SI})
	@Retention(RetentionPolicy.SOURCE)
	public @interface WeightUnit {}

	private SdkUnit(){}

	public static UnitSystemShine convert2UnitShine(int sdkUnit) {
		if (sdkUnit == WEIGHT_UNIT_SI) {
			return UnitSystemShine.SI;
		} else {
			return UnitSystemShine.US;
		}
	}
}
