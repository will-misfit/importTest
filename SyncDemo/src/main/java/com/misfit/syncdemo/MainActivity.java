package com.misfit.syncdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.OtaType;
import com.misfit.syncsdk.SyncSdkAdapter;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.callback.SyncScanCallback;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.model.BaseResponse;
import com.misfit.syncsdk.model.LogSession;
import com.misfit.syncsdk.model.SdkActivityChangeTag;
import com.misfit.syncsdk.model.SdkActivitySessionGroup;
import com.misfit.syncsdk.model.SdkAutoSleepStateChangeTag;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.model.SdkTimeZoneOffset;
import com.misfit.syncsdk.network.APIClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity
    implements SyncScanCallback, SyncOtaCallback, SyncCalculationCallback, SyncSyncCallback {

    private final static String TAG = "MainActivity";

    @Bind(R.id.spinner_device_type)
    Spinner mSpinnerDeviceType;

    @Bind(R.id.switch_should_force_ota)
    Switch mSwitchShouldForceOta;

    SyncSdkAdapter mSyncSdkAdapter;

    Integer[] mDeviceTypes = new Integer[]{
            DeviceType.SHINE,
            DeviceType.FLASH,
            DeviceType.PLUTO,
            DeviceType.SWAROVSKI_SHINE,
            DeviceType.SPEEDO_SHINE
    };

    List<Integer> mSpinnerData;

    SyncCommonDevice mSyncCommonDevice;

    Button mScanButton;
    Button mStopScanButton;
    TextView mTextSerialNumber;
    TextView mSyncOutputTextView;

    private SyncResult mShineSdkSyncResult = new SyncResult();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initSpinnerData();

        mScanButton = (Button)findViewById(R.id.btn_scan);
        mScanButton.setEnabled(true);
        mStopScanButton = (Button)findViewById(R.id.btn_stop_scan);
        mStopScanButton.setEnabled(false);

        mSyncOutputTextView = (TextView)findViewById(R.id.sync_output_msg);
        mTextSerialNumber = (TextView)findViewById(R.id.text_serial_number);

        SpinnerAdapter adapter = new SimpleListAdapter<Integer>(this, mSpinnerData, R.layout.row_simple_text, new int[]{R.id.text}) {
            @Override
            protected void bindData(ViewHolder holder, Integer item, int position) {
                holder.getTextView(R.id.text).setText(DeviceType.getDeviceTypeText(item));
            }
        };
        mSpinnerDeviceType.setAdapter(adapter);
        mSpinnerDeviceType.setSelection(0);

        mSyncSdkAdapter = SyncSdkAdapter.getInstance();
        mSyncSdkAdapter.init(this.getApplicationContext(), "will-misfit");
    }

    private void initSpinnerData() {
        mSpinnerData = new ArrayList<>();
        mSpinnerData.addAll(Arrays.asList(mDeviceTypes));
    }

    @OnClick(R.id.btn_test)
    void test() {
        LogSession session = new LogSession();
        Gson gson = new Gson();
        Log.w("will", "json="+ gson.toJson(session));
        Call<BaseResponse> call = APIClient.getInstance().getLogAPI().uploadSession(session);
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Response<BaseResponse> response) {
                Log.w("will", "rcv=" + response.body().code);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.w("will", "error=", t);
            }
        });
    }

    @OnClick(R.id.btn_scan)
    void scan() {
        int selectedDeviceType = mDeviceTypes[mSpinnerDeviceType.getSelectedItemPosition()];
        Log.i(TAG, String.format("start scan, device type is %s ", DeviceType.getDeviceTypeText(selectedDeviceType)));
        mSyncSdkAdapter.startScanning(selectedDeviceType, this);
        mScanButton.setEnabled(false);
        mStopScanButton.setEnabled(true);
        mSyncOutputTextView.setText("");
    }

    @OnClick(R.id.btn_stop_scan)
    void stopScan() {
        mSyncSdkAdapter.stopScanning();
        mScanButton.setEnabled(true);
        mStopScanButton.setEnabled(false);
    }

    @OnClick(R.id.btn_sync)
    void sync() {
        mSyncCommonDevice.setSyncSyncCallback(this);
        mSyncCommonDevice.startSync(false, this, this, this);
        mSyncOutputTextView.setText(new String());
    }

    @Override
    public void onScanResultFiltered(SyncCommonDevice device, int rssi) {
        String serialNumber = device.getSerialNumber();
        Log.d(TAG, "SyncSDK found device, serialNumber = " + serialNumber);
        if ("SV0EZZZZ3D".equals(serialNumber)) {
            Log.d(TAG, "find my device!");
            mSyncCommonDevice = device;
            mTextSerialNumber.setText(serialNumber);
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
    public int getOtaSuggestion(boolean hasNewFirmware) {
        int type = OtaType.FORCE_OTA;
        Log.d(TAG, "OTA suggestion return " + type);
        return type;
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
    public List<SdkActivityChangeTag> getSdkActivityChangeTagList(int[] startEndTime) {
        return new ArrayList<SdkActivityChangeTag>();
    }

    @Override
    public List<SdkAutoSleepStateChangeTag> getSdkAutoSleepStateChangeTagList(int[] startEndTime) {
        return null;
    }

    @Override
    public SdkTimeZoneOffset getSdkTimeZoneOffsetInCurrentSettings() {
        return null;
    }

    @Override
    public SdkTimeZoneOffset getSdkTimeZoneOffsetBefore(long timestamp) {
        return null;
    }

    @Override
    public List<SdkTimeZoneOffset> getSdkTimeZoneOffsetListAfter(long timestamp) {
        return null;
    }

    @Override
    public SdkProfile getProfileInDatabase() {
        return new SdkProfile();
    }

    /* interface methods of SyncSyncCallback */
    @Override
    public void onShineProfileSyncReadDataCompleted(List<SyncResult> syncResultList) {
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

    @Override
    public void onSyncAndCalculationCompleted(List<SdkActivitySessionGroup> sdkActivitySessionGroupList) {
        String syncAndCalculationResult = OperationUtils.buildSyncCalculationResult(sdkActivitySessionGroupList);
        mSyncOutputTextView.setText(syncAndCalculationResult);
    }
}
