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

import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.SyncSdkAdapter;
import com.misfit.syncsdk.callback.ReadDataCallback;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.callback.SyncOnTagInStateListener;
import com.misfit.syncsdk.callback.SyncOnTagInUserInputListener;
import com.misfit.syncsdk.callback.SyncOperationResultCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.enums.SdkGender;
import com.misfit.syncsdk.model.SdkActivityChangeTag;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;
import com.misfit.syncsdk.model.SdkAutoSleepStateChangeTag;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.model.SdkTimeZoneOffset;
import com.misfit.syncsdk.model.SyncSyncParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;


public class MainActivity extends AppCompatActivity
        implements SyncOperationResultCallback, SyncOtaCallback, SyncCalculationCallback, ReadDataCallback {

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

    Integer[] mDeviceTypes = new Integer[]{
            DeviceType.SHINE,
            DeviceType.FLASH,
            DeviceType.PLUTO,
            DeviceType.SWAROVSKI_SHINE,
            DeviceType.SPEEDO_SHINE
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
    @Bind(R.id.sync_output_msg)
    TextView mSyncOutputTextView;

    @Bind({R.id.btn_sync,
            R.id.switch_first_sync,
            R.id.switch_tagging_response,
            R.id.switch_should_force_ota})
    List<View> syncPanel;

    private SyncResult mShineSdkSyncResult = new SyncResult();

    SyncOnTagInStateListener tagInStateListener = new SyncOnTagInStateListener() {
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
        initSpinnerData();

        setShouldShowScanPanel(true);
        setOperationPanelEnabled(false);

        SpinnerAdapter adapter = new SpinnerAdapter(this, mSpinnerData);
        mSpinnerDeviceType.setAdapter(adapter);
        mSpinnerDeviceType.setSelection(0);

        mSyncSdkAdapter = SyncSdkAdapter.getInstance();
        mSyncSdkAdapter.init(this.getApplicationContext(), "will-misfit");
    }

    private void initSpinnerData() {
        mSpinnerData = new ArrayList<>();
        mSpinnerData.addAll(Arrays.asList(mDeviceTypes));
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

    @OnClick(R.id.btn_test)
    void test() {
    }

    @OnClick(R.id.btn_scan)
    void scan() {
        Intent intent = new Intent(this, ScanListActivity.class);
        int selectedDeviceType = mDeviceTypes[mSpinnerDeviceType.getSelectedItemPosition()];
        intent.putExtra(Const.EXT_DEVICE_TYPE, selectedDeviceType);
        Log.i(TAG, String.format("start scan, device type is %s ", DeviceType.getDeviceTypeText(selectedDeviceType)));
        startActivityForResult(intent, REQ_SCAN);
    }

    @OnClick(R.id.btn_sync)
    void sync() {
        SyncSyncParams syncParams = new SyncSyncParams();
        syncParams.firstSync = mSwitchFirstSync.isChecked();
        mSyncCommonDevice.startSync(this, this, this, this, syncParams);
        mSyncOutputTextView.setText("");
        setOperationPanelEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Log.d(TAG, String.format("result not ok, req=%d, result=%d", requestCode, resultCode));
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
    }

    private void setOperationPanelEnabled(boolean enabled) {
        for (View view : syncPanel) {
            view.setEnabled(enabled);
        }
    }

    /* interface methods of SyncOtaCallback */
    @Override
    public void onOtaProgress(float progress) {
        Log.d(TAG, "OTA progress = " + progress);
    }

    @Override
    public void onOtaCompleted() {
        Log.d(TAG, "OTA Completed");
    }

    @Override
    public boolean isForceOta(boolean hasNewFirmware) {
        return mSwitchShouldForceOta.isChecked();
    }

    /* interface methods of SyncOperationResultCallback */
    @Override
    public void onSucceed() {
        Log.d(TAG, "operation finished");
    }

    @Override
    public void onFailed(int reason) {
        Log.d(TAG, "operation failed, reason=" + reason);
    }

    /* interface methods of SyncCalculationCallback */
    @Override
    public SdkProfile getUserProfile() {
        return DataSourceManager.getSdkProfile(SdkGender.MALE);
    }

    @Override
    public List<SdkActivityChangeTag> getSdkActivityChangeTagList(long startTime, long endTime) {
        return DataSourceManager.getSdkActivityChangeTagList(startTime, endTime);
    }

    @Override
    public List<SdkAutoSleepStateChangeTag> getSdkAutoSleepStateChangeTagList(long startTime, long endTime) {
        List<SdkAutoSleepStateChangeTag> result = new ArrayList<>();
        result.add(new SdkAutoSleepStateChangeTag(startTime, true));
        result.add(new SdkAutoSleepStateChangeTag(endTime, true));
        return result;
    }

    @Override
    public SdkTimeZoneOffset getSdkTimeZoneOffsetInCurrentSettings() {
        return new SdkTimeZoneOffset(Calendar.getInstance().getTimeInMillis() / 1000, DataSourceManager.Timezone_Offset_East_Eight);
    }

    @Override
    public SdkTimeZoneOffset getSdkTimeZoneOffsetBefore(long timestamp) {
        return new SdkTimeZoneOffset(timestamp - 100, DataSourceManager.Timezone_Offset_East_Eight);
    }

    @Override
    public List<SdkTimeZoneOffset> getSdkTimeZoneOffsetListAfter(long timestamp) {
        List<SdkTimeZoneOffset> result = new ArrayList<>();
        result.add(new SdkTimeZoneOffset(timestamp + 10, DataSourceManager.Timezone_Offset_East_Eight));
        return result;
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
    public void onDataCalculateCompleted(final List<SdkActivitySessionGroup> sdkActivitySessionGroupList) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                handleOnSyncAndCalculationCompleted(sdkActivitySessionGroupList);
            }
        });
    }


    private void handleOnShineProfileSyncReadDataCompleted(List<SyncResult> syncResultList) {
        if (syncResultList == null || syncResultList.isEmpty()) {
            mSyncOutputTextView.setText("ShineSDK sync data is null");
            return;
        }

        for (SyncResult syncResult : syncResultList) {
            mShineSdkSyncResult.mActivities.addAll(syncResult.mActivities);
            mShineSdkSyncResult.mSwimSessions.addAll(syncResult.mSwimSessions);
            mShineSdkSyncResult.mTapEventSummarys.addAll(syncResult.mTapEventSummarys);
            mShineSdkSyncResult.mSessionEvents.addAll(syncResult.mSessionEvents);
        }
        String shineSdkSyncResultStr = OperationUtils.buildShineSdkSyncResult(mShineSdkSyncResult);
        mSyncOutputTextView.setText(shineSdkSyncResultStr);
    }

    private void handleOnSyncAndCalculationCompleted(List<SdkActivitySessionGroup> sdkActivitySessionGroupList) {
        String syncAndCalculationResult = OperationUtils.buildSyncCalculationResult(sdkActivitySessionGroupList);
        mSyncOutputTextView.setText(syncAndCalculationResult);
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
}
