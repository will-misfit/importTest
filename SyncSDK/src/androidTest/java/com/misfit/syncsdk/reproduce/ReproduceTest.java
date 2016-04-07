package com.misfit.syncsdk.reproduce;

import android.content.Context;
import android.os.Environment;
import android.test.InstrumentationTestCase;

import com.google.gson.Gson;
import com.misfit.ble.shine.controller.SyncPhaseController;
import com.misfit.ble.shine.parser.TimestampCorrectorNew;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReproduceTest extends InstrumentationTestCase {

    Gson gson;
    GetFileResponse[] fileResponses;
    long syncTime;
    SyncParams syncParams;
    String serialNumber;
    Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getInstrumentation().getContext();
        ContextManager.getInstance().setContext(context);
        System.loadLibrary("CRCCalculator");
        System.loadLibrary("SwimLap");
        gson = new Gson();
        InputStream inputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/kenvin_sleep.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        while (reader.ready()) {
            String lineStr = reader.readLine();
            builder.append(lineStr);
        }
        fileResponses = gson.fromJson(builder.toString(), GetFileResponse[].class);

        serialNumber = "SV0EZ01DZA";
        syncTime = 1459558534;
        syncParams = new SyncParams();
        syncParams.lastSyncTime = 1459524760;
        syncParams.userProfile = recoverUserProfile();
        syncParams.firstSync = false;
        syncParams.userId = "548bb641e470b2680e5f6d66";
        syncParams.appVersion = "v2.8.0";
        syncParams.settingsChangeListSinceLastSync = recoverSettingsChangedList();
    }

    public void testParseSyncResult() throws Exception {

        SyncPhaseController phaseController = new SyncPhaseController(null, null, null);
        Method resetMethod = SyncPhaseController.class.getDeclaredMethod("reset", null);
        resetMethod.setAccessible(true);
        resetMethod.invoke(phaseController, null);
        Method parseShineDataMethod = SyncPhaseController.class.getDeclaredMethod("parseShineData", int.class, long.class, byte[].class, boolean.class);
        parseShineDataMethod.setAccessible(true);
        List<SyncResult> syncResults = new ArrayList<>();
        for (int i = 0; i < fileResponses.length; i++) {
            GetFileResponse response = fileResponses[i];
            SyncResult syncResult = (SyncResult) parseShineDataMethod.invoke(phaseController, response.fileFormat, response.fileTimestamp, response.getRawData(), i == fileResponses.length - 1);
            syncResults.add(syncResult);
        }

        Field timestampCorrectorField = SyncPhaseController.class.getDeclaredField("mTimestampCorrector");
        timestampCorrectorField.setAccessible(true);
        TimestampCorrectorNew timestampCorrector = (TimestampCorrectorNew) timestampCorrectorField.get(phaseController);

        timestampCorrector.correctTimestamp(syncResults, syncTime);

        System.out.printf("%s", gson.toJson(syncResults));

        buildSessionInApp(syncResults);
    }

    public void buildSessionInApp(List<SyncResult> syncResults) throws Exception {

        TaskSharedData sharedData = new TaskSharedData(serialNumber, DeviceType.getDeviceType(serialNumber));
        sharedData.setDeviceBehavior(MisfitDeviceManager.getInstance().getSpecificDevice(serialNumber));
        sharedData.setSyncParams(syncParams);


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

//        userSettingsChangedList.add(new SdkResourceSettings(1429790081, true, SdkActivityType.UNKNOWN, 25200));
//        userSettingsChangedList.add(new SdkResourceSettings(1433840176, true, SdkActivityType.CYCLING, 25200));
//        userSettingsChangedList.add(new SdkResourceSettings(1433840178, true, SdkActivityType.SLEEPING, 25200));
//        userSettingsChangedList.add(new SdkResourceSettings(1434390102, true, SdkActivityType.SWIMMING, 25200));
//        userSettingsChangedList.add(new SdkResourceSettings(1455008913, true, SdkActivityType.SWIMMING, 25200));
//        userSettingsChangedList.add(new SdkResourceSettings(1455009222, true, SdkActivityType.SWIMMING, 25200));
//        userSettingsChangedList.add(new SdkResourceSettings(1455580295, true, SdkActivityType.SWIMMING, -28800));
//        userSettingsChangedList.add(new SdkResourceSettings(1455884911, true, SdkActivityType.SWIMMING, -21600));
//        userSettingsChangedList.add(new SdkResourceSettings(1456417932, true, SdkActivityType.SWIMMING, -28800));

        userSettingsChangedList.add(new SdkResourceSettings(System.currentTimeMillis() / 1000, true, SdkActivityType.RUNNING, 28800));
        return userSettingsChangedList;
    }

    private SdkProfile recoverUserProfile() {
        return new SdkProfile(SdkGender.MALE, 31, 68.3f, 157.64f, SdkUnit.WEIGHT_UNIT_US);
    }
}
