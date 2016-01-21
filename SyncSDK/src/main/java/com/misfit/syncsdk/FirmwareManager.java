package com.misfit.syncsdk;

/**
 * Created by Will Hou on 1/20/16.
 */
public class FirmwareManager {

    //TODO:need a discussion
    public interface GetLatestFirmwareListener{
        void onReceive(String latestFirmware);
        void onFailed(String reason);
    }

    public interface DownloadLatestFirmwareListener{
        void onFinished(String filePath);
        void onFailed(String reason);
    }

    public static FirmwareManager getInstance() {
        return null;
    }

    public void checkLatestFirmware(int deviceType, GetLatestFirmwareListener listener){

    }

    public boolean shouldUpdate(int deviceType, String currentFirmware, String targetFirmware){
        return false;
    }

    public boolean isFirmwareReady(String firmware){
        return false;
    }

    /**
     * if target firmware has been downloaded, will invoke callback directly.
     * @param targetFirmware
     * @param listener
     */
    public void subscribeForDownloadEvent(String targetFirmware, DownloadLatestFirmwareListener listener){}

    public void unsubscriveForDownloadEvent(DownloadLatestFirmwareListener listener){}

}

