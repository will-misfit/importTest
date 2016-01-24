package com.misfit.syncsdk.task;

import android.os.Environment;
import android.util.Log;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.OtaType;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.callback.SyncOtaCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Created by Will Hou on 1/13/16.
 */
public class OtaTask extends Task {

    private final static String TAG = "OtaTask";

    String mLatestFirmwareVersion;
    State mCurrState;

    abstract class State {
        abstract void execute();

        abstract void stop();
    }

    class GetLatestFirmwareState extends State implements FirmwareManager.GetLatestFirmwareListener {

        boolean mShouldStop = false;

        @Override
        void execute() {
            mShouldStop = false;
            String modelName = mTaskSharedData.getModelName();
            String firmwareVersion = mTaskSharedData.getFirmwareVersion();
            //TODO:check modelName & firmwareVersion ready
            FirmwareManager.getInstance().shouldOta(modelName, firmwareVersion, this);
        }

        @Override
        void stop() {
            mShouldStop = true;
        }

        @Override
        public void onSucceed(boolean shouldOta, String firmwareVersion) {
            Log.d(TAG, String.format("shouldOta=%s, firmware version=%s", shouldOta, firmwareVersion));
            if (mShouldStop) {
                return;
            }
            if (shouldOta) {
                mLatestFirmwareVersion = firmwareVersion;
                gotoState(new AskAppSuggestionState());
            } else {
                taskSucceed();
            }
        }

        @Override
        public void onFailed(int errorReason) {
            if (mShouldStop) {
                return;
            }
            taskFailed("get Latest firmware fail");
        }
    }

    class AskAppSuggestionState extends State {

        @Override
        void execute() {
            if (mTaskSharedData.getSyncOtaCallback() != null) {
                int otaType = mTaskSharedData.getSyncOtaCallback().getOtaSuggestion(true);
                gotoState(new PrepareOtaState(otaType));
            } else {
                taskSucceed();
            }
        }

        @Override
        void stop() {

        }
    }

    class PrepareOtaState extends State implements FirmwareManager.DownloadLatestFirmwareListener {

        int mOtaType;

        public PrepareOtaState(int otaType) {
            this.mOtaType = otaType;
        }

        @Override
        void execute() {
            Log.d(TAG, "OtaType=" + mOtaType);
            FirmwareManager firmwareManager = FirmwareManager.getInstance();
            switch (mOtaType) {
                case OtaType.NO_NEED_TO_OTA:
                    taskSucceed();
                    break;
                case OtaType.NEED_OTA:
                    if (firmwareManager.isNewFirmwareReadyNow(mLatestFirmwareVersion)) {
                        firmwareManager.isNewFirmwareReady(mLatestFirmwareVersion, this);
                    } else {
                        taskSucceed();  //skip this time
                    }
                    break;
                case OtaType.FORCE_OTA:
                    firmwareManager.isNewFirmwareReady(mLatestFirmwareVersion, this);
                    break;
                default:
                    //TODO:Log as unexpected event
                    break;
            }
        }

        @Override
        void stop() {

        }

        @Override
        public void onFinished(String fileName) {
            Log.d(TAG, "firmware ready, fileName=" + fileName);
            gotoState(new OtaState(fileName));
        }

        @Override
        public void onFailed(int errorReason) {
            taskFailed("download not ok");
        }
    }

    class OtaState extends State implements ShineProfile.OTACallback {

        String mOtaFileName;
        SyncOtaCallback mSyncOtaCallback;

        public OtaState(String otaFileName) {
            this.mOtaFileName = otaFileName;
            mSyncOtaCallback = mTaskSharedData.getSyncOtaCallback();
        }

        @Override
        void execute() {
            //TODO: check file name
            Log.d(TAG, "binary file name=" + mOtaFileName);
            //FIXME:use local read read method when firmwareManager not completed.
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mOtaFileName);
            byte[] firmwareData = read(file);
            if (firmwareData == null) {
                taskFailed("file not ready");
                return;
            }
            Log.d(TAG, "start ota, name=" + mOtaFileName + ", len=" + firmwareData.length);
            ShineSdkProfileProxy profileProxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
            if (profileProxy != null && profileProxy.isConnected()) {
                profileProxy.startOTA(firmwareData, this);
            }
        }

        //TODO: delete while firmwareManager completed.
        private byte[] read(File file) {
            try {
                if (file == null || !file.isFile()) {
                    return null;
                }
                long fileSize = file.length();
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) fileSize];
                fis.read(data);
                fis.close();
                return data;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        void stop() {

        }

        @Override
        public void onOTACompleted(ShineProfile.ActionResult resultCode) {
            Log.d(TAG, "ota completed");
            if (mSyncOtaCallback != null) {
                mSyncOtaCallback.onOtaCompleted();
            }
            taskSucceed();
        }

        @Override
        public void onOTAProgressChanged(float progress) {
            Log.d(TAG, "ota progress=" + progress);
            if (mSyncOtaCallback != null) {
                mSyncOtaCallback.onOtaProgress(progress);
            }
        }
    }

    @Override
    protected void prepare() {

    }

    @Override
    protected void execute() {
        gotoState(new GetLatestFirmwareState());
    }

    private void gotoState(State state) {
        if (mIsFinished) {
            return;
        }
        Log.d(TAG, "go to state=" + state.getClass().getSimpleName());
        mCurrState = state;
        state.execute();
    }

    @Override
    public void stop() {
        mCurrState.stop();
        super.stop();
        ShineSdkProfileProxy profileProxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
        if (profileProxy != null) {
            profileProxy.interruptCurrentAction();
        } else {
            //TODO:log
        }
    }

    @Override
    public void onStop() {

    }

    @Override
    protected void cleanup() {

    }
}
