package com.misfit.syncsdk.device;

import com.misfit.syncsdk.model.SettingsElement;

/**
 * some device dependent behaviors of each specified device
 */
public interface DeviceBehavior {

    boolean isStreamingSupported();

    boolean supportSettingsElement(SettingsElement element);
}
