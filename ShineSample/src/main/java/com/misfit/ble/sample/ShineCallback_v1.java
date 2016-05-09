package com.misfit.ble.sample;

import android.os.Bundle;

import com.misfit.ble.setting.flashlink.FlashButtonMode;
import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.ShineLapCountingStatus;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineStreamingConfiguration;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.ble.util.MutableBoolean;

import java.util.List;

/**
 * Created by minh on 9/4/15.
 */
public abstract class ShineCallback_v1 {
    public abstract void onConnectionStateChanged(ShineProfile shineProfile, boolean isConnected);

    public void onGettingDeviceConfigurationSucceeded(ConfigurationSession session) {
    }

    public void onGettingDeviceConfigurationFailed(ConfigurationSession session) {
    }

    public void onSettingDeviceConfigurationSucceeded() {
    }

    public void onSettingDeviceConfigurationFailed() {
    }

    public void onSyncSucceeded() {
    }

    public void onSyncFailed() {
    }

    public void onSyncDataReadProgress(Bundle extraInfo, MutableBoolean shouldStop) {
    }
    public void onSyncDataReadCompleted(List<SyncResult> results, MutableBoolean shouldStop) {
    }

    public void onOTAProgressChanged(float progress) {
    }

    public void onOTASucceeded() {
    }

    public void onOTAFailed() {
    }

    public void onReadRssiSucceeded(int rssi) {
    }

    public void onReadRssiFailed() {
    }

    public void onPlayAnimationSucceeded() {
    }

    public void onPlayAnimationFailed() {
    }

    public void onChangingSerialNumberSucceeded() {
    }

    public void onChangingSerialNumberFailed() {
    }

    public void onGettingConnectionParametersSucceeded(ShineConnectionParameters connectionParameters) {
    }

    public void onGettingConnectionParametersFailed(ShineConnectionParameters connectionParameters) {
    }

    public void onSettingConnectionParametersSucceeded(ShineConnectionParameters connectionParameters) {
    }

    public void onSettingConnectionParametersFailed(ShineConnectionParameters connectionParameters) {
    }

    public void onGettingFlashButtonModeSucceeded(FlashButtonMode flashButtonMode) {
    }

    public void onGettingFlashButtonModeFailed() {
    }

    public void onSettingFlashButtonModeSucceeded() {
    }

    public void onSettingFlashButtonModeFailed() {
    }

    public void onActivateSucceeded() {
    }

    public void onActivateFailed() {
    }

    public void onGettingActivationStateSucceeded(boolean wasActivated) {
    }

    public void onGettingActivationStateFailed() {
    }

    public void onStreamingUserInputEventsEnded() {
    }

    public void onStreamingUserInputEventsFailed() {
    }

    public void onStreamingUserInputEventsReceivedEvent(int eventID) {
    }

    public void onGettingStreamingConfigurationSucceeded(ShineStreamingConfiguration streamingConfiguration) {
    }

    public void onGettingStreamingConfigurationFailed() {
    }

    public void onSettingStreamingConfigurationSucceeded() {
    }

    public void onSettingStreamingConfigurationFailed() {
    }

    public void onStartButtonAnimationSucceeded() {
    }

    public void onStartButtonAnimationFailed() {
    }

    public void onMapEventAnimationSucceeded() {
    }

    public void onMapEventAnimationFailed() {
    }

    public void onUnmapAllEventAnimationSucceeded() {
    }

    public void onUnmapAllEventAnimationFailed() {
    }

    public void onSystemControlEventMappingSucceeded() {
    }

    public void onSystemControlEventMappingFailed() {
    }

    public void onSettingExtraAdvertisingDataStateRequestSucceeded() {
    }

    public void onSettingExtraAdvertisingDataStateRequestFailed() {
    }

    public void onGettingExtraAdvertisingDataStateRequestSucceeded(boolean enable) {
    }

    public void onGettingExtraAdvertisingDataStateRequestFailed() {
    }

    public void onGettingLapCountingStatusSucceeded(ShineLapCountingStatus status) {
    }
    public void onGettingLapCountingStatusFailed() {
    }
    public void onSettingLapCountingLicenseInfoSucceeded() {
    }
    public void onSettingLapCountingLicenseInfoFailed() {
    }
    public void onSettingLapCountingModeSucceeded() {
    }
    public void onSettingLapCountingModeFailed() {
    }
}
