package com.misfit.syncdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.SyncSdkAdapter;
import com.misfit.syncsdk.callback.SyncScanCallback;
import com.misfit.syncsdk.device.SyncCommonDevice;
import com.misfit.syncsdk.enums.ScanFailedReason;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;


public class ScanListActivity extends AppCompatActivity implements SyncScanCallback {

    DeviceAdapter mAdapter;

    @Bind(R.id.list_device)
    ListView mListDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_list);
        ButterKnife.bind(this);

        setResult(RESULT_CANCELED);

        int deviceTpe = getIntent().getIntExtra(Const.EXT_DEVICE_TYPE, DeviceType.UNKNOWN);
        mAdapter = new DeviceAdapter(this);
        mListDevice.setAdapter(mAdapter);

        SyncSdkAdapter.getInstance().startScanning(deviceTpe, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SyncSdkAdapter.getInstance().stopScanning();
    }

    @Override
    public void onScanResultFiltered(SyncCommonDevice device, int rssi) {
        Log.w("will", "found dev=" + device.getSerialNumber());
        mAdapter.updateDevice(device, rssi);
    }

    @Override
    public void onScanFailed(@ScanFailedReason.ScanFailedReasonValue int reason) {
        Log.w("will", "scan failed, reason : " + reason);
    }

    @OnItemClick(R.id.list_device)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceAdapter.DeviceItem item = mAdapter.mData.get(position);
        Intent intent = new Intent();
        intent.putExtra(Const.EXT_SERIAL_NUNBER, item.device.getSerialNumber());
        setResult(RESULT_OK, intent);
        finish();
    }

    static class DeviceAdapter extends SimpleListAdapter<DeviceAdapter.DeviceItem, DeviceAdapter.ViewHolder> {

        public DeviceAdapter(Context context) {
            super(context, new ArrayList<DeviceItem>(), R.layout.row_device);
        }

        @Override
        protected ViewHolder createViewHolder(View itemView, int type) {
            return new ViewHolder(itemView);
        }

        @Override
        protected void bindData(ViewHolder holder, DeviceItem item, int position) {
            holder.name.setText(DeviceType.getDeviceTypeText(item.device.getSerialNumber()));
            holder.serial.setText(item.device.getSerialNumber());
            holder.rssi.setText("-" + item.rssi);
        }

        public void updateDevice(SyncCommonDevice device, int rssi) {
            for (DeviceItem item : mData) {
                if (item.device.getSerialNumber().equals(device.getSerialNumber())) {
                    item.device = device;
                    item.rssi = rssi;
                    Log.w("will", "update");
                    notifyDataSetChanged();
                    return;
                }
            }
            mData.add(new DeviceItem(device, rssi));
            Log.w("will", "add");
            notifyDataSetChanged();
        }

        static class ViewHolder extends SimpleListAdapter.ViewHolder {

            @Bind(R.id.text_name)
            TextView name;

            @Bind(R.id.text_serial_number)
            TextView serial;

            @Bind(R.id.text_rssi)
            TextView rssi;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        static class DeviceItem {
            SyncCommonDevice device;
            int rssi;

            public DeviceItem(SyncCommonDevice device, int rssi) {
                this.device = device;
                this.rssi = rssi;
            }
        }
    }
}
