package com.misfit.syncdemo.reproduce;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.misfit.ble.shine.controller.SyncPhaseController;
import com.misfit.ble.shine.parser.TimestampCorrectorNew;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncdemo.util.FileUtils;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.MisfitDeviceManager;
import com.misfit.syncsdk.enums.SdkActivityType;
import com.misfit.syncsdk.enums.SdkGender;
import com.misfit.syncsdk.enums.SdkUnit;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.model.SdkResourceSettings;
import com.misfit.syncsdk.model.SyncParams;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;
import com.misfit.syncsdk.utils.ContextManager;
import com.misfit.syncsdk.utils.MLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class SyncReproducer {

    private final static String TAG = "SyncReproducer";

    private Gson mGson;
    private GetFileResponse[] mFileResponses;
    private long mSyncTime;
    private SyncParams mSyncParams;
    private String mSerialNumber;

    public SyncReproducer(Context context) {
        ContextManager.getInstance().setContext(context);
        System.loadLibrary("CRCCalculator");
        System.loadLibrary("SwimLap");
        mGson = new Gson();
    }

    private void setUpSyncParam(SyncInfo syncInfo, String settingsPath) throws Exception {
        mSerialNumber = syncInfo.serialNumber;
        mSyncTime = syncInfo.syncTime;
        mSyncParams = new SyncParams();
        mSyncParams.lastSyncTime = syncInfo.lastSyncTime;
        mSyncParams.userProfile = new SdkProfile(SdkGender.MALE, 31, 68.3f, 157.64f, SdkUnit.WEIGHT_UNIT_US);
        mSyncParams.firstSync = false;
        mSyncParams.userId = syncInfo.uid;
        mSyncParams.appVersion = "v2.8.0";
        mSyncParams.settingsChangeListSinceLastSync = TextUtils.isEmpty(settingsPath) ?
                getDefaultSettings() : SettingsParser.parse(settingsPath);
    }

    public void startFromSdk(String reproduceDataPath, String settingsPath) throws Exception {
        MLog.i(TAG, "read reproduce data, path=" + reproduceDataPath);
        ReproduceData reproduceData = mGson.fromJson(FileUtils.readFile(reproduceDataPath), ReproduceData.class);
        MLog.i(TAG, reproduceData.syncInfo.toString());
        mFileResponses = reproduceData.getFileResponses;
        setUpSyncParam(reproduceData.syncInfo, settingsPath);
        List<SyncResult> syncResults = getSyncResultsFromSdk();
        buildSessionInApp(syncResults);
    }

    private List<SyncResult> getSyncResultsFromSdk() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        SyncPhaseController phaseController = new SyncPhaseController(null, null, null);
        Method resetMethod = SyncPhaseController.class.getDeclaredMethod("reset", null);
        resetMethod.setAccessible(true);
        resetMethod.invoke(phaseController, null);
        Method parseShineDataMethod = SyncPhaseController.class.getDeclaredMethod("parseShineData", int.class, long.class, byte[].class, boolean.class);
        parseShineDataMethod.setAccessible(true);
        List<SyncResult> syncResults = new ArrayList<>();
        for (int i = 0; i < mFileResponses.length; i++) {
            GetFileResponse response = mFileResponses[i];
            SyncResult syncResult = (SyncResult) parseShineDataMethod.invoke(phaseController, response.fileFormat, response.fileTimestamp, response.getRawData(), i == mFileResponses.length - 1);
            syncResults.add(syncResult);
        }

        Field timestampCorrectorField = SyncPhaseController.class.getDeclaredField("mTimestampCorrector");
        timestampCorrectorField.setAccessible(true);
        TimestampCorrectorNew timestampCorrector = (TimestampCorrectorNew) timestampCorrectorField.get(phaseController);

        timestampCorrector.correctTimestamp(syncResults, mSyncTime);
        MLog.i(TAG, "from Sdk, syncResults size=" + syncResults.size());
        return syncResults;
    }

    private void buildSessionInApp(List<SyncResult> syncResults) throws Exception {
        TaskSharedData sharedData = new TaskSharedData(mSerialNumber, DeviceType.getDeviceType(mSerialNumber));
        sharedData.setDeviceBehavior(MisfitDeviceManager.getInstance().getSpecificDevice(mSerialNumber));
        sharedData.setSyncParams(mSyncParams);

        SyncAndCalculateTask taskObj = new SyncAndCalculateTask();
        Method prepareMethod = SyncAndCalculateTask.class.getDeclaredMethod("prepare", null);
        prepareMethod.setAccessible(true);
        prepareMethod.invoke(taskObj, null);

        Field sharedDataField = Task.class.getDeclaredField("mTaskSharedData");
        sharedDataField.setAccessible(true);
        sharedDataField.set(taskObj, sharedData);


        Class[] classes = SyncAndCalculateTask.class.getDeclaredClasses();
        Class asyncTaskCls = null;
        for (Class cls : classes) {
            if ("SyncedDataCalculationTask".equals(cls.getSimpleName())) {
                asyncTaskCls = cls;
                break;
            }
        }
        if (asyncTaskCls == null) {
            return;
        }
        Constructor constructor = asyncTaskCls.getDeclaredConstructor(taskObj.getClass(), List.class);
        constructor.setAccessible(true);
        Object asyncTaskObj = constructor.newInstance(taskObj, syncResults);
        Method doInBackgroundMethod = asyncTaskCls.getDeclaredMethod("doInBackground", Void[].class);
        doInBackgroundMethod.setAccessible(true);
        doInBackgroundMethod.invoke(asyncTaskObj, new Void[1]);
    }


    private List<SdkResourceSettings> getDefaultSettings() {
        List<SdkResourceSettings> userSettingsChangedList = new ArrayList<>();
        long nowInMills = System.currentTimeMillis();
        userSettingsChangedList.add(new SdkResourceSettings(nowInMills / 1000, true, SdkActivityType.RUNNING, TimeZone.getDefault().getOffset(nowInMills) / 1000));
        return userSettingsChangedList;
    }
}
