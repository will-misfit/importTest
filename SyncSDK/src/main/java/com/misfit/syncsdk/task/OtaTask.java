package com.misfit.syncsdk.task;

import android.text.TextUtils;

import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.syncsdk.ConnectionManager;
import com.misfit.syncsdk.FirmwareManager;
import com.misfit.syncsdk.OtaType;
import com.misfit.syncsdk.ShineSdkProfileProxy;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.utils.ContextUtils;
import com.misfit.syncsdk.utils.LocalFileUtils;
import com.misfit.syncsdk.utils.MLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TimerTask;

public class OtaTask extends Task {

    private final static String TAG = "OtaTask";

    String mLatestFirmwareVersion;
    State mCurrState;

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
            MLog.d(TAG, String.format("shouldOta=%s, firmware version=%s", shouldOta, firmwareVersion));
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
            MLog.d(TAG, "OtaType=" + mOtaType);
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
                    //TODO:MLog as unexpected event
                    break;
            }
        }

        @Override
        void stop() {

        }

        @Override
        public void onFinished(String fileName) {
            MLog.d(TAG, "firmware ready, fileName=" + fileName);
            gotoState(new OtaState(fileName));
        }

        @Override
        public void onFailed(int errorReason) {
            taskFailed("download not ok");
        }
    }

    class OtaState extends State implements ShineProfile.OTACallback {
        private String mOtaFileName;
        private SyncOtaCallback mSyncOtaCallback;
        //TODO:we can not unsubscribe the ota callback. that is why we use this variable.
        private boolean mIsStateFinished = false;

        public OtaState(String otaFileName) {
            this.mOtaFileName = otaFileName;
            mSyncOtaCallback = mTaskSharedData.getSyncOtaCallback();
        }

        @Override
        void execute() {
            MLog.d(TAG, "binary file name=" + mOtaFileName);
            if (TextUtils.isEmpty(mOtaFileName)) {
                taskFailed("firmware file name is empty");
                return;
            }
//            FIXME:use local read read method when firmwareManager not completed.
//            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mOtaFileName);
//            byte[] firmwareData = read(file);
            byte[] firmwareData = LocalFileUtils.read(mOtaFileName);
            if (firmwareData == null) {
                taskFailed("file not ready");
                return;
            }
            MLog.d(TAG, "start ota, name=" + mOtaFileName + ", len=" + firmwareData.length);
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
            //TODO: handle ota failed
            MLog.d(TAG, "ota completed");
            if (mIsStateFinished) {
                MLog.d(TAG, "state already finished");
                return;
            }
            mIsStateFinished = true;
            if (mSyncOtaCallback != null) {
                mSyncOtaCallback.onOtaCompleted();
            }
            gotoState(new WaitForConnectState());
        }

        @Override
        public void onOTAProgressChanged(float progress) {
            MLog.d(TAG, "ota progress=" + progress);
            if(mIsStateFinished){
                MLog.d(TAG, "state already finished");
                return;
            }
            if (mSyncOtaCallback != null) {
                mSyncOtaCallback.onOtaProgress(progress);
            }
        }
    }

    class WaitForConnectState extends State {
        private final static int DELAY_BEFORE_CONNECT = 5000;

        @Override
        void execute() {
            setNewTimeOutTask(new TimerTask() {
                @Override
                public void run() {
                    gotoState(new ReconnectState());
                }
            }, DELAY_BEFORE_CONNECT);
        }

        @Override
        void stop() {
            cancelCurrentTimeoutTask();
        }
    }

    class ReconnectState extends State implements ConnectionManager.ConnectionStateCallback {

        private int mRemainingRetry = 2;
        private final static int TIMEOUT_CONNECT = 45000;

        @Override
        void execute() {
            final ConnectionManager connectionManager = ConnectionManager.getInstance();
            ShineSdkProfileProxy proxy = ConnectionManager.getInstance().getShineSDKProfileProxy(mTaskSharedData.getSerialNumber());
            if (proxy == null) {
                taskFailed("proxy is null for unknown reason");
                return;
            }
            ShineDevice device = connectionManager.getShineDevice(mTaskSharedData.getSerialNumber());
            if (device == null) {
                taskFailed("device not ready");
                return;
            }
            if (proxy.isConnected()) {
                taskSucceed();
                return;
            }

            //set timeout
            setNewTimeOutTask(new TimerTask() {
                @Override
                public void run() {
                    mRemainingRetry--;
                    if (mRemainingRetry > 0) {
                        cancelCurrentTimerTask();
                        execute();
                    } else {
                        cleanup();
                        taskFailed("reconnect failed");
                    }
                }
            }, TIMEOUT_CONNECT);

            //connect
            connectionManager.subscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), this);
            proxy.connectProfile(device, ContextUtils.getInstance().getContext());
        }

        private void cleanup() {
            cancelCurrentTimeoutTask();
            ConnectionManager.getInstance().unsubscribeConnectionStateChanged(mTaskSharedData.getSerialNumber(), this);
        }

        @Override
        void stop() {
            cleanup();
        }

        @Override
        public void onConnectionStateChanged(ShineProfile.State newState) {
            if (newState == ShineProfile.State.CONNECTED) {
                cleanup();
                taskSucceed();
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
        MLog.d(TAG, "go to state=" + state.getClass().getSimpleName());
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
            //TODO:MLog
        }
    }

    @Override
    public void onStop() {

    }

    @Override
    protected void cleanup() {

    }
}
