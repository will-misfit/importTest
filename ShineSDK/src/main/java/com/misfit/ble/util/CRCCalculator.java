package com.misfit.ble.util;

public class CRCCalculator {
	// A native method that receives nothing and returns void
	private native int crc(long length, byte[] bytes);
	
	public static long calculateCRC(long length, byte[] bytes) {
		return new CRCCalculator().crc(length, bytes);
	}
}
