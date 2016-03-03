package com.misfit.syncdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.controller.ConfigurationSession;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncdemo.util.LogView;
import com.misfit.syncsdk.*;
import com.misfit.syncsdk.callback.ConnectionStateCallback;
import com.misfit.syncsdk.callback.ReadDataCallback;
import com.misfit.syncsdk.callback.SyncOnTagInStateListener;
import com.misfit.syncsdk.callback.SyncOnTagInUserInputListener;
import com.misfit.syncsdk.callback.SyncOperationResultCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.enums.SdkGender;
import com.misfit.syncsdk.model.PostCalculateData;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;
import com.misfit.syncsdk.model.SyncParams;
import com.misfit.syncsdk.utils.MLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;


public class MainActivity extends AppCompatActivity
        implements SyncOperationResultCallback, SyncOtaCallback, ReadDataCallback, ConnectionStateCallback {

    private final static String TAG = "MainActivity";

    private final static int REQ_SCAN = 1;

    @Bind(R.id.spinner_device_type)
    Spinner mSpinnerDeviceType;

    @Bind(R.id.text_serial_number)
    TextView mTextSerialNumber;

    @Bind(R.id.text_device_name)
    TextView mTextDeviceName;

    @Bind(R.id.btn_show_scan_panel)
    Button mShowScanPanelButton;

    @Bind(R.id.ll_scan)
    View mScanPanel;

    private long mLastTimeTapScanPanel;

    SyncSdkAdapter mSyncSdkAdapter;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    Integer[] mDeviceTypeInts = new Integer[]{
        DeviceType.UNKNOWN,  // UNKNOWN means no device type filter
        DeviceType.SHINE,
        DeviceType.FLASH,
        DeviceType.SWAROVSKI_SHINE,
        DeviceType.SPEEDO_SHINE,
        DeviceType.SHINE_MK_II,
        DeviceType.PLUTO,
        DeviceType.FLASH_LINK,
        DeviceType.SILVRETTA,
        DeviceType.BMW
    };

    List<Integer> mSpinnerData;

    SyncCommonDevice mSyncCommonDevice;

    @Bind(R.id.btn_sync)
    Button mSyncButton;

    @Bind(R.id.switch_should_force_ota)
    Switch mSwitchShouldForceOta;

    @Bind(R.id.switch_first_sync)
    Switch mSwitchFirstSync;

    @Bind(R.id.switch_tagging_response)
    Switch mSwitchTaggingResponse;

    // LogView implements MLog.LogNode
    @Bind(R.id.tv_log)
    LogView mLogTextView;

    @Bind(R.id.sdk_version)
    TextView mSdkVersionView;

    @Bind({R.id.btn_sync,
            R.id.switch_first_sync,
            R.id.switch_tagging_response,
            R.id.switch_should_force_ota})
    List<View> syncPanel;

    private SyncResult mShineSdkSyncResult = new SyncResult();

    SyncOnTagInStateListener mTagInStateListener = new SyncOnTagInStateListener() {
        @Override
        public void onDeviceTaggingIn(int deviceType, SyncOnTagInUserInputListener inputCallback) {
            inputCallback.onUserInputForTaggingIn(mSwitchTaggingResponse.isChecked());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setShouldShowScanPanel(true);
        setOperationPanelEnabled(false);

        initSpinnerData();
        SpinnerAdapter adapter = new SpinnerAdapter(this, mSpinnerData);
        mSpinnerDeviceType.setAdapter(adapter);
        mSpinnerDeviceType.setSelection(0);

        mSyncSdkAdapter = SyncSdkAdapter.getInstance();
        mSyncSdkAdapter.init(this.getApplicationContext(), "will-misfit", "5c203ef8-d62a-11e5-ab30-625662870761");

        String versionStr = String.format("SyncSDK-%s, SyncDemo-%s", getSdkVersion(), getDemoVersion());
        mSdkVersionView.setText(versionStr);
    }

    private void initSpinnerData() {
        mSpinnerData = new ArrayList<>();
        mSpinnerData.addAll(Arrays.asList(mDeviceTypeInts));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnTouch(R.id.ll_scan)
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - mLastTimeTapScanPanel < 1000) {
                setShouldShowScanPanel(false);
                return true;
            }
            mLastTimeTapScanPanel = System.currentTimeMillis();
        }
        return false;
    }

    @OnClick(R.id.btn_show_scan_panel)
    void showScanPanel() {
        setShouldShowScanPanel(true);
    }

    @OnClick(R.id.btn_scan)
    void scan() {
        Intent intent = new Intent(this, ScanListActivity.class);
        int selectedDeviceType = mDeviceTypeInts[mSpinnerDeviceType.getSelectedItemPosition()];
        intent.putExtra(Const.EXT_DEVICE_TYPE, selectedDeviceType);
        MLog.i(TAG, String.format("start scan, device type is %s ", DeviceType.getDeviceTypeText(selectedDeviceType)));
        startActivityForResult(intent, REQ_SCAN);
    }

    @OnClick(R.id.btn_sync)
    void sync() {
        SyncParams syncParams = createSyncParams();

        mSyncCommonDevice.startSync(this, this, this, this, syncParams);
        mLogTextView.clear();
        setOperationPanelEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            MLog.d(TAG, String.format("result not ok, req = %d, result = %d", requestCode, resultCode));
            return;
        }
        switch (requestCode) {
            case REQ_SCAN:
                updateDevice(data.getStringExtra(Const.EXT_SERIAL_NUNBER));
                setShouldShowScanPanel(false);
                setOperationPanelEnabled(true);
                break;
        }
    }

    private void setShouldShowScanPanel(boolean shouldShow) {
        if (shouldShow) {
            mShowScanPanelButton.setVisibility(View.GONE);
            mScanPanel.setVisibility(View.VISIBLE);
        } else {
            mShowScanPanelButton.setVisibility(View.VISIBLE);
            mScanPanel.setVisibility(View.GONE);
        }
    }

    private void updateDevice(String serialNumber) {
        Log.d(TAG, "updated device, serialNumber=" + serialNumber);
        mSyncCommonDevice = SyncSdkAdapter.getInstance().getDevice(serialNumber);
        mTextSerialNumber.setText(serialNumber);
        mTextDeviceName.setText(DeviceType.getDeviceTypeText(serialNumber));
        mSwitchTaggingResponse.setVisibility(DeviceType.getDeviceType(serialNumber) == DeviceType.FLASH ? View.VISIBLE : View.GONE);
    }

    private void setOperationPanelEnabled(boolean enabled) {
        for (View view : syncPanel) {
            view.setEnabled(enabled);
        }
    }

    /* interface methods of SyncOtaCallback */
    @Override
    public void onOtaProgress(float progress) {
        MLog.d(TAG, "OTA progress = " + progress);
    }

    @Override
    public void onOtaCompleted() {
        MLog.d(TAG, "OTA Completed");
    }

    @Override
    public boolean isForceOta(boolean hasNewFirmware) {
        return mSwitchShouldForceOta.isChecked();
    }

    /* interface methods of SyncOperationResultCallback */
    @Override
    public void onSucceed() {
        MLog.d(TAG, "operation finished");
    }

    @Override
    public void onFailed(int reason) {
        MLog.d(TAG, "operation failed, reason=" + reason);
    }

    /* interface methods of ReadDataCallback */
    /**
     * separate the thread where SyncSDK callback comes from and the main UI thread
     */
    @Override
    public void onRawDataReadCompleted(final List<SyncResult> syncResultList) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                handleOnShineProfileSyncReadDataCompleted(syncResultList);
            }
        });
    }

    @Override
    public PostCalculateData onDataCalculateCompleted(final SdkActivitySessionGroup sdkActivitySessionGroup) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                handleOnSyncAndCalculationCompleted(sdkActivitySessionGroup);
            }
        });

        PostCalculateData result = new PostCalculateData();
        int newPoints = OperationUtils.getActivityPointSum(sdkActivitySessionGroup.activitySessionList);
        result.todayPoints = newPoints;
        return result;
    }

    @Override
    public void onGetShineConfigurationCompleted(ConfigurationSession configSession) {

    }

    /* interface methods of ConnectionStateCallback */
    @Override
    public void onConnectionStateChanged(ShineProfile.State newState) {
        MLog.d(TAG, "connection state changes to " + newState);
    }

    private void handleOnShineProfileSyncReadDataCompleted(List<SyncResult> syncResultList) {
        if (syncResultList == null || syncResultList.isEmpty()) {
            MLog.d(TAG, "ShineSDK sync data is null");
            return;
        }

        for (SyncResult syncResult : syncResultList) {
            mShineSdkSyncResult.mActivities.addAll(syncResult.mActivities);
            mShineSdkSyncResult.mSwimSessions.addAll(syncResult.mSwimSessions);
            mShineSdkSyncResult.mTapEventSummarys.addAll(syncResult.mTapEventSummarys);
            mShineSdkSyncResult.mSessionEvents.addAll(syncResult.mSessionEvents);
        }
        String shineSdkSyncResultStr = OperationUtils.buildShineSdkSyncResult(mShineSdkSyncResult);
        MLog.d(TAG, shineSdkSyncResultStr);
    }

    private void handleOnSyncAndCalculationCompleted(SdkActivitySessionGroup sdkActivitySessionGroup) {
        String syncAndCalculationResult = OperationUtils.buildSyncCalculationResult(sdkActivitySessionGroup);
        MLog.d(TAG, syncAndCalculationResult);
        mSyncButton.setEnabled(true);
    }

    static class SpinnerAdapter extends SimpleListAdapter<Integer, SpinnerAdapter.ViewHolder> {

        public SpinnerAdapter(Context context, List<Integer> data) {
            super(context, data, R.layout.row_simple_text);
        }

        @Override
        protected ViewHolder createViewHolder(View itemView, int type) {
            return new ViewHolder(itemView);
        }

        @Override
        protected void bindData(ViewHolder holder, Integer item, int position) {
            holder.text.setText(DeviceType.getDeviceTypeText(item));
        }

        static class ViewHolder extends SimpleListAdapter.ViewHolder {
            @Bind(R.id.text)
            TextView text;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /**
     * parameter model class of startSync()
     * NOTE: SyncParams instance can be created in different methods for different device types
     * */
    private SyncParams createSyncParams() {
        SyncParams syncParams = new SyncParams();

        syncParams.firstSync = true;
        syncParams.lastSyncTime = DataSourceManager.createLastSyncTime();
        syncParams.userProfile = DataSourceManager.getSdkProfile(SdkGender.MALE);
        syncParams.firstSync = mSwitchFirstSync.isChecked();
        syncParams.tagInStateListener = mTagInStateListener;
        syncParams.userId = "6d0dbf70-de8a-11e5-b86d-9a79f06e9478";
        syncParams.appVersion = "v2.8.0";
        syncParams.settingsChangeListSinceLastSync = DataSourceManager.createSdkResourceSettings(4, 10);

        return syncParams;
    }

    private SyncParams createSyncParamsForFlashButton() {
        SyncParams syncParams = new SyncParams();

        return syncParams;
    }

    private String getSdkVersion() {
        return com.misfit.syncsdk.BuildConfig.VERSION_NAME;
    }

    private String getDemoVersion() {
        return com.misfit.syncdemo.BuildConfig.VERSION_NAME;
    }
}
