package com.misfit.syncdemo.reproduce;

import android.content.Context;

import com.google.gson.Gson;
import com.misfit.ble.shine.result.SyncResult;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    private void setUp(Context context, String path) throws Exception {
        MLog.i(TAG, "setup, path=" + path);
        ContextManager.getInstance().setContext(context);
        System.loadLibrary("CRCCalculator");
        System.loadLibrary("SwimLap");
        mGson = new Gson();
        InputStream inputStream = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        while (reader.ready()) {
            String lineStr = reader.readLine();
            builder.append(lineStr);
        }
        mFileResponses = mGson.fromJson(builder.toString(), GetFileResponse[].class);

        mSerialNumber = "SV0EZ01DZA";
        mSyncTime = 1459558534;
        mSyncParams = new SyncParams();
        mSyncParams.lastSyncTime = 1459524760;
        mSyncParams.userProfile = recoverUserProfile();
        mSyncParams.firstSync = false;
        mSyncParams.userId = "548bb641e470b2680e5f6d66";
        mSyncParams.appVersion = "v2.8.0";
        mSyncParams.settingsChangeListSinceLastSync = recoverSettingsChangedList();
    }

    public void startFromSdk(Context context, String path) throws Exception {
        setUp(context, path);
//        SyncPhaseController phaseController = new SyncPhaseController(null, null, null);
//        Method resetMethod = SyncPhaseController.class.getDeclaredMethod("reset", null);
//        resetMethod.setAccessible(true);
//        resetMethod.invoke(phaseController, null);
//        Method parseShineDataMethod = SyncPhaseController.class.getDeclaredMethod("parseShineData", int.class, long.class, byte[].class, boolean.class);
//        parseShineDataMethod.setAccessible(true);
//        List<SyncResult> syncResults = new ArrayList<>();
//        for (int i = 0; i < mFileResponses.length; i++) {
//            GetFileResponse response = mFileResponses[i];
//            SyncResult syncResult = (SyncResult) parseShineDataMethod.invoke(phaseController, response.fileFormat, response.fileTimestamp, response.getRawData(), i == mFileResponses.length - 1);
//            syncResults.add(syncResult);
//        }
//
//        Field timestampCorrectorField = SyncPhaseController.class.getDeclaredField("mTimestampCorrector");
//        timestampCorrectorField.setAccessible(true);
//        TimestampCorrectorNew timestampCorrector = (TimestampCorrectorNew) timestampCorrectorField.get(phaseController);
//
//        timestampCorrector.correctTimestamp(syncResults, mSyncTime);
//
//        System.out.printf("%s", mGson.toJson(syncResults));
//
//        buildSessionInApp(syncResults);
    }

    public void buildSessionInApp(List<SyncResult> syncResults) throws Exception {

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


    private List<SdkResourceSettings> recoverSettingsChangedList() {
        List<SdkResourceSettings> userSettingsChangedList = new ArrayList<>();
        long nowInMills = System.currentTimeMillis();
        userSettingsChangedList.add(new SdkResourceSettings(nowInMills / 1000, true, SdkActivityType.RUNNING, TimeZone.getDefault().getOffset(nowInMills) / 1000));
        return userSettingsChangedList;
    }

    private SdkProfile recoverUserProfile() {
        return new SdkProfile(SdkGender.MALE, 31, 68.3f, 157.64f, SdkUnit.WEIGHT_UNIT_US);
    }
}
