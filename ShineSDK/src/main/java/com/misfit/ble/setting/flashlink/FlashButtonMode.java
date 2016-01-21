package com.misfit.ble.setting.flashlink;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for Flash Button Mode
 * Created by Quoc-Hung Le on 9/18/15.
 */
public enum FlashButtonMode {
	SELFIE((short) 1),
	MUSIC((short) 2),
	PRESCO((short) 3),
	APPLICATION((short) 4),
	TRACKER((short) 5),
	BOLT_CONTROL((short) 6),
	CUSTOM_MODE((short) 7);

	private static final Map<Short, FlashButtonMode> lookup
			= new HashMap<Short, FlashButtonMode>();

	static {
		for (FlashButtonMode s : EnumSet.allOf(FlashButtonMode.class))
			lookup.put(s.getId(), s);
	}

	private short mId;

	private FlashButtonMode(short id) {
		this.mId = id;
	}

	public static FlashButtonMode get(short id) {
		return lookup.get(id);
	}

	public short getId() {
		return mId;
	}
}
