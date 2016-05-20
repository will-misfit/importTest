package com.misfit.ble.sample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.misfit.ble.sample.ui.BmwActivity;
import com.misfit.ble.sample.ui.TestSyncAndConnectActivity;
import com.misfit.ble.sample.utils.FileDialog;
import com.misfit.ble.sample.utils.SharedPreferencesUtils;
import com.misfit.ble.sample.view.NumberPreferenceEditText;
import com.misfit.ble.setting.SDKSetting;
import com.misfit.ble.setting.flashlink.CustomModeEnum;
import com.misfit.ble.setting.speedo.ActivityType;
import com.misfit.ble.shine.ShineAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;

public class ShineActivity extends BaseActivity {

    public static final String TAG = "ShineActivity";

    public static final String CONFIG_LAST_OPEN_DIR = "last_open_dir";

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_FIRMWARE_SELECTION = 3;

    @Bind(R.id.btn_start_or_stop_scan)
    Button mScanButton;

    @Bind(R.id.btn_connect_or_disconnect)
    Button mConnectButton;

    @Bind(R.id.btn_close)
    Button mCloseButton;

    @Bind(R.id.edit_set_configuration)
    EditText mConfigurationEditText;

    @Bind(R.id.edit_set_serial_string)
    EditText mSerialNumberEditText;

    @Bind(R.id.edit_set_connection_parameters)
    EditText mConnectionParamsEditText;

    @Bind(R.id.edit_set_flash_button_mode)
    EditText mFlashButtonModeEditText;

    @Bind(R.id.edit_set_streaming_configuration)
    EditText mStreamingConfigurationEditText;

    @Bind(R.id.edit_event_animation_mapping)
    EditText mEventAnimationMappingEditText;

    @Bind(R.id.edit_event_mapping_system_control)
    EditText mSystemControlEditText;

    @Bind(R.id.edit_button_animation)
    EditText mButtonAnimationEditText;

    @Bind(R.id.deviceName)
    TextView mDeviceLabel;

    @Bind(R.id.textView)
    TextView mShineApiInfoTextView;

    @Bind(R.id.txtAdvFlag)
    EditText mAdvFlagEditText;

    @Bind(R.id.sdk_version)
    TextView mSDKVersion;

    @Bind(R.id.cb_auto_retry_ota)
    CheckBox mAutoRetryOtaCb;

    @Bind(R.id.pref_activity_type)
    NumberPreferenceEditText mActivityTypePref;

    @Bind(R.id.edit_lap_counting_mode)
    EditText mSetLapCountingModeEditText;

    @Bind(R.id.btn_stop_animation)
    Button mStopAnimationButton;

    @Bind(R.id.btn_interrupt)
    Button mInterruptButton;

    /**
     * Activity Events
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shine_layout);
        ButterKnife.bind(this);

        mShineApiInfoTextView = (TextView) findViewById(R.id.textView);
        mShineApiInfoTextView.setVerticalScrollBarEnabled(true);
        mShineApiInfoTextView.setHorizontallyScrolling(true);
        mShineApiInfoTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mDeviceLabel = (TextView) findViewById(R.id.deviceName);
        mSDKVersion.setText(SDKSetting.getSDKVersion());

        Timer rssiTimer = new Timer();
        rssiTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mState >= BTLE_STATE_CONNECTED) {
                    mService.readRssi();
                }
            }
        }, 1000, 1000);
    }

    @OnClick(R.id.btn_is_streaming)
    void isStreaming() {
        setMessage("isStreaming=" + mService.isUserEventStreaming());
    }

    @OnClick(R.id.btn_bmw)
    void gotoBmw() {
        Intent intent = new Intent(this, BmwActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_activity_type)
    void onClickedActivityType() {
        if (mActivityTypePref.getValue() == null) {
            mService.getActivityType();
        } else {
            ActivityType activityType = new ActivityType(mActivityTypePref.getValue().byteValue());
            mService.setActivityType(activityType);
        }
    }

    @OnClick(R.id.btn_animate)
    void playAnimateion() {
        setMessage("PLAY ANIMATION");
        mService.playAnimation();
    }

    @OnClick(R.id.btn_stop_animation)
    void stopAnimation() {
        setMessage("STOP PLAYING ANIMATION");
        mService.stopPlayingAnimation();
    }

    @OnClick(R.id.btn_start_or_stop_scan)
    void startOrStopScan() {
        if (mState == BTLE_STATE_IDLE) {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                Log.i(TAG, "onClick - BT not enabled yet");
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                Intent newIntent = new Intent(ShineActivity.this, DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);

                setState(BTLE_STATE_SCANNING);
                setMessage("SCANNING");
            }
        }
    }

    @OnClick(R.id.btn_connect_or_disconnect)
    void connectOrDisconnect() {
        if (mState == BTLE_STATE_IDLE || mState == BTLE_STATE_CLOSED) {
            if (mService.connect(mDevice, null)) {
                setState(BTLE_STATE_CONNECTING);
                setMessage("CONNECTING");
            } else {
                setMessage("CONNECTING FAILED.\nDEVICE WAS INVALIDATED, PLEASE SCAN AGAIN.");
            }
        }
    }

    @OnClick(R.id.btn_close)
    void close() {
        if (mState >= BTLE_STATE_CLOSED) {
            mService.close();
        }
    }

    @OnClick(R.id.btn_configuration)
    void configuration() {
        String paramsString = mConfigurationEditText.getText().toString();
        if (TextUtils.isEmpty(paramsString)) {
            setMessage("GETTING CONFIGURATION");
            mService.startGettingDeviceConfiguration();
        } else {
            setMessage("SETTING CONFIGURATION");
            mService.startSettingDeviceConfiguration(paramsString);
        }
    }

    @OnClick(R.id.btn_change_serial_number)
    void changeSerialNumber() {
        String serialNumber = mSerialNumberEditText.getText().toString();

        setMessage("CHANGING SERIAL NUMBER");
        mService.startChangingSerialNumber(serialNumber);
    }

    @OnClick(R.id.btn_set_connection_parameters)
    void connectionParameters() {
        String parameters = mConnectionParamsEditText.getText().toString().trim();

        if (TextUtils.isEmpty(parameters)) {
            setMessage("GETTING CONNECTION PARAMETERS");
            mService.startGettingConnectionParameters();
        } else {
            setMessage("SETTING CONNECTION PARAMETERS");
            mService.startSettingConnectionParameters(parameters);
        }
    }

    @OnClick(R.id.btn_set_flash_button_mode)
    void flashButtonMode() {
        String parameters = mFlashButtonModeEditText.getText().toString();

        if (TextUtils.isEmpty(parameters)) {
            setMessage("GETTING FLASH BUTTON MODE");
            mService.startGettingFlashButtonMode();
        } else {
            setMessage("SETTING FLASH BUTTON MODE");
            mService.startSettingFlashButtonMode(parameters);
        }
    }

    @OnClick(R.id.btn_create_bond)
    void createBond() {
        if (mDevice != null) {
            setMessage("CREATE BOND - success:" + mService.createBond(mDevice));
        }
    }

    @OnClick(R.id.btn_clear_bond)
    void clearBond() {
        if (mDevice != null) {
            setMessage("REMOVE BOND - success:" + mService.removeBond(mDevice));
        }
    }

    @OnClick(R.id.btn_sync)
    void sync() {
        setMessage("SYNCING");
        mService.startSync();
    }

    @OnClick(R.id.btn_get_streaming_config)
    void getStreamingConfig() {
        setMessage("GET STREAMING CONFIG");
        mService.startGettingStreamingConfig();
    }

    @OnClick(R.id.btn_set_streaming_configuration)
    void setStreamingConfig() {
        String parameters = mStreamingConfigurationEditText.getText().toString();

        setMessage("SET STREAMING CONFIG");
        mService.startSettingStreamingConfig(parameters);
    }

    @OnClick(R.id.btn_button_animation)
    void startButtonAnimation() {
        String parameters = mButtonAnimationEditText.getText().toString();

        setMessage("START BUTTON ANIMATION");
        mService.startButtonAnimation(parameters);
    }

    @OnClick(R.id.btn_map_event_animation)
    void mapEventAnimation() {
        String parameters = mEventAnimationMappingEditText.getText().toString();

        setMessage("MAPPING EVENT ANIMATION");
        mService.mapEventAnimation(parameters);
    }

    @OnClick(R.id.btn_unmap_event_animation)
    void unmapEventAnimation() {
        setMessage("UNMAPPING EVENT ANIMATION");
        mService.unmapEventAnimation();
    }

    @OnClick(R.id.btn_event_mapping_system_control)
    void systemControlEventMapping() {
        String parameters = mSystemControlEditText.getText().toString();

        setMessage("SYSTEM CONTROL EVENT MAPPING");
        mService.systemControlEventMapping(parameters);
    }

    @OnClick(R.id.btn_ota)
    void ota() {
        showFileChooser();
    }

    @OnClick(R.id.btn_stream_user_input_events)
    void startStreamingUserInputEvents() {
        mService.startStreamingUserInputEvents();
    }

    @OnClick(R.id.btn_start_or_stop_activating)
    void startActivating() {
        setMessage("ACTIVATING");
        mService.startActivating();
    }

    @OnClick(R.id.btn_activation_state)
    void startGettingActivationState() {
        setMessage("GETTING ACTIVATION STATE");
        mService.startGettingActivationState();
    }

    @OnClick(R.id.hidConnectButton)
    void hidConnect() {
        if (mDevice != null) {
            setMessage("HID CONNECT - success=" + mService.hidConnect(mDevice));
        }
    }

    @OnClick(R.id.hidDisconnectButton)
    void hidDisconnect() {
        if (mDevice != null) {
            setMessage("HID DISCONNECT - success=" + mService.hidDisconnect(mDevice));
        }
    }

    @OnClick(R.id.btnAdvState)
    void getOrSetAdvEventState() {
        if (mAdvFlagEditText.getText().length() > 0) {
            setMessage("SET EXTRA AD DATA STATE");
            mService.startSettingAdvEventState(Boolean.parseBoolean(mAdvFlagEditText.getText().toString()));
        } else {
            setMessage("GET EXTRA AD DATA STATE");
            mService.startGettingAdvEventState();
        }
    }

    @OnClick(R.id.btn_interrupt)
    void intterupt() {
        stopCurrentOperation();
    }

    @OnClick(R.id.btnShineToPluto)
    void plutoPage() {
        Intent i = new Intent(ShineActivity.this, PlutoActivity.class);
        startActivity(i);
        finish();
    }

    @OnClick(R.id.btnPlutoToBolt)
    void boltPage() {
        Intent i = new Intent(ShineActivity.this, BoltActivity.class);
        startActivity(i);
        finish();
    }

    @OnClick(R.id.btn_unmap_all_events)
    void unmapAllEvents() {
        setMessage("UNMAP ALL EVENTS");
        mService.unmapAllEvents();
    }

    @OnClick(R.id.btn_unmap_specific_event)
    void unmapEvent() {
        setMessage("UNMAP SPECIFIC EVENT");
        mService.unmapEvent(CustomModeEnum.MemEventNumber.DOUBLE_PRESS_N_HOLD);
    }

    @OnClick(R.id.btn_custom_mode)
    void setCustomMode() {
        setMessage("SET CUSTOM MODE");
        mService.setCustomMode(CustomModeEnum.ActionType.HID_MEDIA, CustomModeEnum.MemEventNumber.PLUTO_TRIPLE_TAP,
                CustomModeEnum.AnimNumber.TRIPLE_PRESS_SUCCEEDED, CustomModeEnum.KeyCode.MEDIA_VOLUME_UP_OR_SELFIE, true);
    }

    @OnClick(R.id.btn_get_lap_counting_status)
    void startGettingLapCountingStatus() {
        setMessage("GET LAP COUNTING STATUS");
        mService.startGettingLapCountingStatus();
    }

    @OnClick(R.id.btn_set_lap_counting_license_info_ready)
    void setLapCountingLicenseReady() {
        mService.startSettingLapCountingLicenseInfo(mDevice.getSerialNumber(), true);
    }

    @OnClick(R.id.btn_set_lap_counting_license_info_not_ready)
    void setLapCountingLicenseNotReady() {
        mService.startSettingLapCountingLicenseInfo(mDevice.getSerialNumber(), false);
    }

    @OnClick(R.id.btn_set_lap_counting_mode)
    void startSettingLapCountingMode() {
        String modeStr = mSetLapCountingModeEditText.getText().toString().trim();
        mService.startSettingLapCountingMode(modeStr);
    }

    @OnClick(R.id.btn_enable_bt)
    void enableOrDisableBluetooth() {
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            ShineAdapter.getDefaultAdapter(this).disableBluetooth();
        } else {
            ShineAdapter.getDefaultAdapter(this).enableBluetooth();
        }
        Toast.makeText(this, R.string.enable_bluetooth, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_test_connect)
    public void openTestConnect() {
        Intent intent = new Intent(this, TestSyncAndConnectActivity.class);
        intent.putExtras(TestSyncAndConnectActivity.getOpenBundle(mDevice));
        startActivity(intent);
    }

    public void getLastErrorCode(View v) {
        int connErrCode = mService.getLastConnectionErrorCode();
        StringBuilder msgBuilder = new StringBuilder();
        switch (connErrCode) {
            case -1:
                msgBuilder.append("Sorry, ShineProfile is null");
                break;
            case 0:
                msgBuilder.append("Connect Success");
                break;
            case 10:
                msgBuilder.append("Connect Fail: Gatt callback is not received");
                break;
            case 11:
                msgBuilder.append("Connect Fail: Gatt Error");
                break;
            case 12:
                msgBuilder.append("Connect Fail: Gatt Conn Terminate Peer User");
                break;
            case 13:
                msgBuilder.append("Connect Fail: Gatt Conn Terminate Local User");
                break;
            case 14:
                msgBuilder.append("Connect Fail: Gatt Conn Timeout");
                break;
            case 15:
                msgBuilder.append("Connect Fail: Gatt Others");
                break;
            case 20:
                msgBuilder.append("Connect Fail: Handshake Discover Services");
                break;
            case 21:
                msgBuilder.append("Connect Fail: Handshake Subscribe Characteristics");
                break;
            case 22:
                msgBuilder.append("Connect Fail: Handshake Get Serial Number");
                break;
            case 23:
                msgBuilder.append("Connect Fail: Handshake Get Model Name");
                break;
            case 24:
                msgBuilder.append("Connect Fail: Handshake Get Firmware Version");
                break;
            case 50:
            default:
                msgBuilder.append("Connect Fail: Unknown");
        }
        setMessage(msgBuilder.toString());
    }

    /**
     * Activity Result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    mDevice = data.getParcelableExtra(MisfitShineService.EXTRA_DEVICE);
                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                }

                mService.stopScanning();

                setState(BTLE_STATE_IDLE);
                setMessage("SCANNING STOPPED");
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_FIRMWARE_SELECTION:
                if (data != null) {
                    ExFilePickerParcelObject object = data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());
                    if (object.count > 0) {
                        onFirmwareSelected(object.path + object.names.get(0));
                    }
                }
                break;
            default:
                Log.e(TAG, "wrong request Code");
                break;
        }
    }


    @Bind({R.id.btn_create_bond,
            R.id.btn_clear_bond,
            R.id.hidConnectButton,
            R.id.hidDisconnectButton})
    List<View> viewsEnabledWhenHaveDevice;

    @Bind({R.id.btn_animate,
            R.id.btn_configuration,
            R.id.btn_get_streaming_config,
            R.id.btn_set_streaming_configuration,
            R.id.btn_change_serial_number,
            R.id.btn_set_connection_parameters,
            R.id.btn_set_flash_button_mode,
            R.id.btn_sync,
            R.id.btn_ota,
            R.id.btn_stream_user_input_events,
            R.id.btn_button_animation,
            R.id.btn_map_event_animation,
            R.id.btn_unmap_event_animation,
            R.id.btn_event_mapping_system_control,
            R.id.btn_start_or_stop_activating,
            R.id.btn_activation_state,
            R.id.btn_custom_mode,
            R.id.btn_unmap_specific_event,
            R.id.btn_unmap_all_events,
            R.id.btnAdvState,
            R.id.btnShineToPluto,
            R.id.btnPlutoToBolt,
            R.id.btn_bmw,
            R.id.btn_get_lap_counting_status,
            R.id.btn_set_lap_counting_license_info_ready,
            R.id.btn_set_lap_counting_license_info_not_ready,
            R.id.btn_set_lap_counting_mode})
    List<View> viewsEnabledWhenReady;

    @Override
    protected void setUiState() {
        super.setUiState();

        mScanButton.setEnabled(mState <= BTLE_STATE_SCANNING);
        mScanButton.setText(mState != BTLE_STATE_SCANNING ? R.string.scan : R.string.stop_scan);

        mConnectButton.setEnabled(mDevice != null && mState < BTLE_STATE_CLOSED);

        mCloseButton.setEnabled(mState >= BTLE_STATE_CLOSED);

        for (View view : viewsEnabledWhenHaveDevice) {
            view.setEnabled(mDevice != null);
        }

        for (View view : viewsEnabledWhenReady) {
            view.setEnabled(isReady);
        }

        mStopAnimationButton.setEnabled(mState >= BTLE_STATE_CLOSED);

        boolean isUserEventStreaming = mService != null && mService.isStreaming();
        mInterruptButton.setEnabled(isReady || isUserEventStreaming);

        if (mDevice != null) {
            mDeviceLabel.setText(mDevice.getName() + " - " + mDevice.getSerialNumber() + " - rssi:  " + mRssi);
        } else {
            mDeviceLabel.setText("");
        }


        updateMessage();
    }

    private void updateMessage() {
        if (TextUtils.isEmpty(mMessage)) {
            return;
        }
        if (mShineApiInfoTextView.getText().length() > 500) {
            String msg = mShineApiInfoTextView.getText().toString();
            mShineApiInfoTextView.setText(msg.substring(300, msg.length()));
            mShineApiInfoTextView.scrollTo(0, 0);
        }
        mShineApiInfoTextView.append(mMessage + "\n");
        int offset = mShineApiInfoTextView.getLineCount() * mShineApiInfoTextView.getLineHeight();
        if (offset > mShineApiInfoTextView.getHeight()) {
            mShineApiInfoTextView.scrollTo(0, offset - mShineApiInfoTextView.getHeight());
        }
        mMessage = "";
    }

    private byte[] readRawResourceFile(String path) {
        byte[] bytes = null;

        try {
            FileInputStream fis = new FileInputStream(new File(path));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte data[] = new byte[1024];
            int count;

            while ((count = fis.read(data)) != -1) {
                bos.write(data, 0, count);
            }

            bos.flush();
            bos.close();
            fis.close();

            bytes = bos.toByteArray();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException");
        } catch (IOException ioe) {
            Log.e(TAG, "IOException");
        } catch (NullPointerException npe) {
            Log.e(TAG, "NullPointerException");
        }

        return bytes;
    }

    private void showFileChooser() {
        Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
        intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
        intent.putExtra(ExFilePicker.SET_START_DIRECTORY, SharedPreferencesUtils.readConfig(this, CONFIG_LAST_OPEN_DIR, Environment.getExternalStorageDirectory().getAbsolutePath()));
        startActivityForResult(intent, REQUEST_FIRMWARE_SELECTION);
    }

    /**
     * A handmade file chooser by myself
     */
    private void showHandmadeFileChooser() {
        File mPath = new File(Environment.getExternalStorageDirectory().getPath());
        FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".bin");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                onFirmwareSelected(file.getPath());
            }
        });

        fileDialog.showDialog();
    }

    private static String getPath(Context context, Uri uri) throws URISyntaxException {
        String result;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void onFirmwareSelected(String path) {
        File file = new File(path).getParentFile();
        if (file.isDirectory()) {
            SharedPreferencesUtils.writeConfig(this, CONFIG_LAST_OPEN_DIR, file.getAbsolutePath());
        }
        byte[] firmwareData = readRawResourceFile(path);

        if (firmwareData == null || firmwareData.length <= 0) {
            Toast.makeText(ShineActivity.this, "Invalid data", Toast.LENGTH_SHORT).show();
            return;
        }

        setMessage(path.substring(path.lastIndexOf("/") + 1));
        mService.startOTAing(firmwareData, mAutoRetryOtaCb.isChecked());
    }
}
