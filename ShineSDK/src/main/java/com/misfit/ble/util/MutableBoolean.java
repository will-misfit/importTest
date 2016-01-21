/**
 * MutableBoolean.java
 * Misfit-BLE-Android-SDK
 * Created by hieupham on Aug 13, 2014
 */
package com.misfit.ble.util;

/**
 * @author hieupham
 *
 */
public class MutableBoolean extends MutableWrapper<Boolean> {

	public MutableBoolean(Boolean value) {
		super(value);
	}
	
	public boolean getValue() {
		return mValue;
	}
	
	public void setValue(boolean value) {
		mValue = value;
	}
}
