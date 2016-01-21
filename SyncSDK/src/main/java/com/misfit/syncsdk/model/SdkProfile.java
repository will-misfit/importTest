package com.misfit.syncsdk.model;

import com.misfit.cloud.algorithm.models.ProfileShine;

/**
 * a data model to reflect ProfileShine in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkProfile {

	private int age;
	private float height;
	private float weight;
	private int sdkGender;

	private int sdkUnit;

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public int getSdkGender() {
		return sdkGender;
	}

	public void setSdkGender(int sdkGender) {
		this.sdkGender = sdkGender;
	}

	public int getSdkUnit() {
		return sdkUnit;
	}

	public void setSdkUnit(int sdkUnit) {
		this.sdkUnit = sdkUnit;
	}

	public ProfileShine convert2ProfileShine() {
		ProfileShine result = new ProfileShine();
		result.setAge(age);
		result.setHeight(height);
		result.setWeight(weight);
		result.setGender(SdkGender.convert2GenderShine(sdkGender));
		result.setDisplayUnit(SdkUnit.convert2UnitShine(sdkUnit));
		return result;
	}

}
