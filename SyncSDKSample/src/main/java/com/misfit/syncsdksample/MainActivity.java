package com.misfit.syncsdksample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;

import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.OtaType;
import com.misfit.syncsdk.SyncSdkAdapter;
import com.misfit.syncsdk.callback.SyncOtaCallback;
import com.misfit.syncsdk.callback.SyncScanCallback;
import com.misfit.syncsdk.callback.SyncSyncCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.model.SdkActivityChangeTag;
import com.misfit.syncsdk.model.SdkProfile;
import com.misfit.syncsdk.utils.LocalFileUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements SyncScanCallback, SyncOtaCallback, SyncSyncCallback {

    private final static String TAG = "MainActivity";

    @Bind(R.id.spinner_device_type)
    Spinner mSpinnerDeviceType;

    @Bind(R.id.switch_should_force_ota)
    Switch mSwitchShouldForceOta;

    SyncSdkAdapter mSyncSdkAdapter;

    int[] mDeviceTypes = new int[]{
            DeviceType.SHINE,
            DeviceType.FLASH,
            DeviceType.PLUTO,
            DeviceType.SWAROVSKI_SHINE
    };

    List<Integer> mSpinnerData;


    SyncCommonDevice mSyncCommonDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initSpinnerData();
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
        mSpinnerData.add(DeviceType.SHINE);
        mSpinnerData.add(DeviceType.FLASH);
        mSpinnerData.add(DeviceType.PLUTO);
        mSpinnerData.add(DeviceType.SWAROVSKI_SHINE);
    }

    @OnClick(R.id.btn_scan)
    void scan() {
        int selectedDeviceType = mDeviceTypes[mSpinnerDeviceType.getSelectedItemPosition()];
        Log.i(TAG, "start scan, device type=" + selectedDeviceType);
        mSyncSdkAdapter.startScanning(selectedDeviceType, this);
    }

    @OnClick(R.id.btn_stop_scan)
    void stopScan() {
        mSyncSdkAdapter.stopScanning();
    }

    @OnClick(R.id.btn_sync)
    void sync() {
        mSyncCommonDevice.startSync(false, this, this);
    }

    @Override
    public void onScanResultFiltered(SyncCommonDevice device, int rssi) {
        Log.i(TAG, "SyncSDK found device, serialNumber=" + device.getSerialNumber());
        if ("SC0CC0068M".equals(device.getSerialNumber())) {
            Log.d(TAG, "update device");
            mSyncCommonDevice = device;
        }
    }

    @Override
    public void onOtaProgress(float progress) {
        Log.i(TAG, "OTA progress=" + progress);
    }

    @Override
    public int getOtaSuggestion(boolean hasNewFirmware) {
        int type = OtaType.FORCE_OTA;
        Log.d(TAG, "OTA suggestion return " + type);
        return type;
    }

    @Override
    public void onSyncDataOutput() {
        Log.d(TAG, "sync data output");
    }

    @Override
    public void onFinished() {
        Log.d(TAG, "operation finished");
    }

    @Override
    public void onFailed(int reason) {
        Log.d(TAG, "operation failed, reason=" + reason);
    }

    @Override
    public void onOtaCompleted() {
        Log.d(TAG, "OTA Completed");
    }

    @Override
    public List<SdkActivityChangeTag> getSdkActivityChangeTagList(int[] startEndTime) {
        return new ArrayList<SdkActivityChangeTag>();
    }

    @Override
    public SdkProfile getProfileInDatabase() {
        return new SdkProfile();
    }

}
