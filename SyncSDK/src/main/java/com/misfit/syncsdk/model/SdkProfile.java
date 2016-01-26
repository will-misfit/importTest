package com.misfit.syncsdk.model;

import com.misfit.cloud.algorithm.models.ProfileShine;
import com.misfit.syncsdk.enums.SdkGender;
import com.misfit.syncsdk.enums.SdkUnit;

/**
 * a data model to reflect ProfileShine in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkProfile {

	private int mAge;
	private float mHeight;
	private float mWeight;
	private int mSdkGender;

	private int mSdkUnit;

	public int getAge() {
		return mAge;
	}

	public void setAge(int age) {
		this.mAge = age;
	}

	public float getHeight() {
		return mHeight;
	}

	public void setHeight(float height) {
		this.mHeight = height;
	}

	public float getWeight() {
		return mWeight;
	}

	public void setWeight(float weight) {
		this.mWeight = weight;
	}

	public int getmSdkGender() {
		return mSdkGender;
	}

	public void setmSdkGender(int mSdkGender) {
		this.mSdkGender = mSdkGender;
	}

	public int getSdkUnit() {
		return mSdkUnit;
	}

	public void setSdkUnit(int sdkUnit) {
		this.mSdkUnit = sdkUnit;
	}

	public ProfileShine convert2ProfileShine() {
		ProfileShine result = new ProfileShine();
		result.setAge(mAge);
		result.setHeight(mHeight);
		result.setWeight(mWeight);
		result.setGender(SdkGender.convert2GenderShine(mSdkGender));
		result.setDisplayUnit(SdkUnit.convert2UnitShine(mSdkUnit));
		return result;
	}

}
