/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.misfit.ble.sample;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.misfit.ble.shine.ShineDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceListActivity extends Activity {
	public static final String TAG = DeviceListActivity.class.getSimpleName();
	
    private TextView mEmptyList;
    private List<ShineDevice> deviceList;
    
    private DeviceAdapter deviceAdapter;
    private MisfitShineService mService;

    private Map<String, Integer> devRssiValues;
    private Map<String, String> devSerialValues;

    private void bindShineService() {
        Intent bindIntent = new Intent(this, com.misfit.ble.sample.MisfitShineService.class);
        startService(bindIntent);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((MisfitShineService.LocalBinder) rawBinder).getService();
            mService.setDeviceDiscoveringHandler(mHandler);
            if (mService.startScanning() == false) {
                Toast.makeText(DeviceListActivity.this, "Start Scanning Failed.", Toast.LENGTH_LONG).show();
                finish();
            }

            if (mService.getConnectedShines() == false) {
                Toast.makeText(DeviceListActivity.this, "Retrieve Connected Shines Failed.", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
            case com.misfit.ble.sample.MisfitShineService.SHINE_SERVICE_DISCOVERED:
                Bundle data = message.getData();
                final ShineDevice device = data.getParcelable(com.misfit.ble.sample.MisfitShineService.EXTRA_DEVICE);
                final String serialString = data.getString(com.misfit.ble.sample.MisfitShineService.EXTRA_SERIAL_STRING);
                final int rssi = data.getInt(com.misfit.ble.sample.MisfitShineService.EXTRA_RSSI);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addDevice(device, serialString, rssi);
                    }
                });
                break;
            default:
                super.handleMessage(message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        setContentView(R.layout.device_list);
        
        mEmptyList = (TextView) findViewById(R.id.empty);
        populateList();
        
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bindShineService();
    }

    private void populateList() {
        /* Initialize device list container */
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<>();
        devSerialValues = new HashMap<>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
    }

    private void addDevice(ShineDevice device, String serialString, int rssi) {
        boolean deviceFound = false;

        for (ShineDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }

        String deviceAddrr = device.getAddress();
        devRssiValues.put(deviceAddrr, rssi);
        if (serialString != null) {
            devSerialValues.put(deviceAddrr, serialString);
        }

        if (!deviceFound) {
            mEmptyList.setVisibility(View.GONE);
            deviceList.add(device);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ShineDevice device = deviceList.get(position);
            
            Bundle bundle = new Bundle();
            bundle.putParcelable(com.misfit.ble.sample.MisfitShineService.EXTRA_DEVICE, device);

            Intent result = new Intent();
            result.putExtras(bundle);

            setResult(Activity.RESULT_OK, result);
            finish();
        }
    };

    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<ShineDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<ShineDevice> devices) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            ShineDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            
            String deviceAddr = device.getAddress();
            
            byte rssival = (byte) devRssiValues.get(deviceAddr).intValue();
            if (rssival != 0) {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }
            tvname.setText(device.getName() + "-" + devSerialValues.get(deviceAddr));
            tvadd.setText(deviceAddr);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                tvname.setTextColor(Color.GRAY);
                tvadd.setTextColor(Color.GRAY);
                tvrssi.setVisibility(View.GONE);
            } else {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }
            return vg;
        }
    }
}
