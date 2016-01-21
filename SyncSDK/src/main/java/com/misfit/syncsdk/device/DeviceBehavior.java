package com.misfit.syncsdk.device;

import com.misfit.syncsdk.model.SettingsElement;

/**
 * some device dependent behaviors of each specified device
 */
public interface DeviceBehavior {
	public boolean isStreamingSupported();

	public boolean supportSettingsElement(SettingsElement element);
}
