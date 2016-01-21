package com.misfit.ble.shine;

public interface ShineEventMappingSystemControl {
	public static final byte MASK_DEFAULT = (byte)0xE0;
	public static final byte MASK_UNPAUSE_COMMAND = 1 << 0;
	public static final byte MASK_SET_BEGINNING_OF_SEQUENCE = 1 << 1;
}
