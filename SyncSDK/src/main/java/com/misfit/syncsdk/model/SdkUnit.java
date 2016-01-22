package com.misfit.syncsdk.model;

import com.misfit.cloud.algorithm.models.UnitSystemShine;

/**
 * a data model to reflect ActivityChangeTagShine in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkUnit {

	public final static int WEIGHT_UNIT_US = 0;
	public final static int WEIGHT_UNIT_SI = 1;

	private SdkUnit(){}

	public static UnitSystemShine convert2UnitShine(int sdkUnit) {
		if (sdkUnit == WEIGHT_UNIT_SI) {
			return UnitSystemShine.SI;
		} else {
			return UnitSystemShine.US;
		}
	}
}
