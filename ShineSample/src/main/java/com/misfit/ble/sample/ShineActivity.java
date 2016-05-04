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
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.misfit.ble.sample.ui.BmwActivity;
import com.misfit.ble.sample.ui.TestSyncAndConnectActivity;
import com.misfit.ble.sample.utils.FileDialog;
import com.misfit.ble.sample.utils.SharedPreferencesUtils;
import com.misfit.ble.setting.SDKSetting;
import com.misfit.ble.setting.flashlink.CustomModeEnum;
import com.misfit.ble.shine.ShineAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
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

	private Button mScanButton, mConnectButton, mCloseButton;
	private Button mHIDConnectButton, mHIDDisconnectButton, mInputMethodButton;
	private Button mAnimateButton, mStopAnimationButton, mConfigButton, mSyncButton, mOTAButton;

	private Button mActivateButton, mActivateStateButton, mStreamUserInputEventsButton;
	private Button mChangeSNButton, mConnectionParametersButton;
	private EditText mConfigurationEditText, mSerialNumberEditText, mConnectionParamsEditText;

	private Button mFlashButtonModeButton;
	private EditText mFlashButtonModeEditText;

	private Button mCreateBondButton, mClearBondButton;

	private Button mGetStreamConfigButton, mSetStreamConfigButton;
	private EditText mStreamingConfigurationEditText;

	private Button mMapEventAnimationButton, mUnmapEventAnimationMapping, mButtonAnimationButton, mSystemControlButton;
	private EditText mButtonAnimationEditText, mEventAnimationMappingEditText, mSystemControlEditText;

	private TextView mDeviceLabel, mShineApiInfoTextView;

	private Button mAdStateButton;
	private CheckBox mGetSetAdvStateCheckBox;
	private EditText mAdvFlagEditText;

	private Button mInterruptButton;

	private Button mShineToPlutoButton;
	private Button mShineToBoltButton;

	private TextView mSDKVersion;

	private Button mUnmapAllEventsButton;
	private Button mUnmapSpecificEventButton;
	private Button mCustomModeButton;

	@Bind(R.id.cb_auto_retry_ota)
	CheckBox mAutoRetryOtaCb;

	/**
	 * Activity Events
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shine_layout);

		mScanButton = (Button) findViewById(R.id.btn_start_or_stop_scan);
		mScanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
		});

		mAnimateButton = (Button) findViewById(R.id.btn_animate);
		mAnimateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMessage("PLAY ANIMATION");
				mService.playAnimation();
			}
		});


		mStopAnimationButton = (Button) findViewById(R.id.btn_stop_animation);
		mStopAnimationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMessage("STOP PLAYING ANIMATION");
				mService.stopPlayingAnimation();
			}
		});

		mConnectButton = (Button) findViewById(R.id.btn_connect_or_disconnect);
		mConnectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mState == BTLE_STATE_IDLE || mState == BTLE_STATE_CLOSED) {
					if (mService.connect(mDevice, null)) {
						setState(BTLE_STATE_CONNECTING);
						setMessage("CONNECTING");
					} else {
						setMessage("CONNECTING FAILED.\nDEVICE WAS INVALIDATED, PLEASE SCAN AGAIN.");
					}
				}
			}
		});

		mCloseButton = (Button) findViewById(R.id.btn_close);
		mCloseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mState >= BTLE_STATE_CLOSED) {
					mService.close();
				}
			}
		});

		mConfigButton = (Button) findViewById(R.id.btn_configuration);
		mConfigButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String paramsString = mConfigurationEditText.getText().toString();
				if (TextUtils.isEmpty(paramsString)) {
					setMessage("GETTING CONFIGURATION");
					mService.startGettingDeviceConfiguration();
				} else {
					setMessage("SETTING CONFIGURATION");
					mService.startSettingDeviceConfiguration(paramsString);
				}
			}
		});

		mChangeSNButton = (Button) findViewById(R.id.btn_change_serial_number);
		mChangeSNButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String serialNumber = mSerialNumberEditText.getText().toString();

				setMessage("CHANGING SERIAL NUMBER");
				mService.startChangingSerialNumber(serialNumber);
			}
		});

		mConnectionParametersButton = (Button) findViewById(R.id.btn_set_connection_parameters);
		mConnectionParametersButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String parameters = mConnectionParamsEditText.getText().toString().trim();

				if (TextUtils.isEmpty(parameters)) {
					setMessage("GETTING CONNECTION PARAMETERS");
					mService.startGettingConnectionParameters();
				} else {
					setMessage("SETTING CONNECTION PARAMETERS");
					mService.startSettingConnectionParameters(parameters);
				}
			}
		});

		mFlashButtonModeButton = (Button) findViewById(R.id.btn_set_flash_button_mode);
		mFlashButtonModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String parameters = mFlashButtonModeEditText.getText().toString();

				if (TextUtils.isEmpty(parameters)) {
					setMessage("GETTING FLASH BUTTON MODE");
					mService.startGettingFlashButtonMode();
				} else {
					setMessage("SETTING FLASH BUTTON MODE");
					mService.startSettingFlashButtonMode(parameters);
				}
			}
		});

		mCreateBondButton = (Button) findViewById(R.id.btn_create_bond);
		mCreateBondButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDevice != null) {
					setMessage("CREATE BOND - success:" + mService.createBond(mDevice));
				}
			}
		});

		mClearBondButton = (Button) findViewById(R.id.btn_clear_bond);
		mClearBondButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDevice != null) {
					setMessage("REMOVE BOND - success:" + mService.removeBond(mDevice));
				}
			}
		});

		mSyncButton = (Button) findViewById(R.id.btn_sync);
		mSyncButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMessage("SYNCING");
				mService.startSync();
			}
		});

		mStreamingConfigurationEditText = (EditText) findViewById(R.id.edit_set_streaming_configuration);

		mGetStreamConfigButton = (Button) findViewById(R.id.btn_get_streaming_config);
		mGetStreamConfigButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMessage("GET STREAMING CONFIG");
				mService.startGettingStreamingConfig();
			}
		});

		mSetStreamConfigButton = (Button) findViewById(R.id.btn_set_streaming_configuration);
		mSetStreamConfigButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String parameters = mStreamingConfigurationEditText.getText().toString();

				setMessage("SET STREAMING CONFIG");
				mService.startSettingStreamingConfig(parameters);
			}
		});

		mButtonAnimationEditText = (EditText) findViewById(R.id.edit_button_animation);

		mButtonAnimationButton = (Button) findViewById(R.id.btn_button_animation);
		mButtonAnimationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String parameters = mButtonAnimationEditText.getText().toString();

				setMessage("START BUTTON ANIMATION");
				mService.startButtonAnimation(parameters);
			}
		});

		mEventAnimationMappingEditText = (EditText) findViewById(R.id.edit_event_animation_mapping);

		mMapEventAnimationButton = (Button) findViewById(R.id.btn_map_event_animation);
		mMapEventAnimationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String parameters = mEventAnimationMappingEditText.getText().toString();

				setMessage("MAPPING EVENT ANIMATION");
				mService.mapEventAnimation(parameters);
			}
		});

		mUnmapEventAnimationMapping = (Button) findViewById(R.id.btn_unmap_event_animation);
		mUnmapEventAnimationMapping.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMessage("UNMAPPING EVENT ANIMATION");
				mService.unmapEventAnimation();
			}
		});

		mSystemControlEditText = (EditText) findViewById(R.id.edit_event_mapping_system_control);

		mSystemControlButton = (Button) findViewById(R.id.btn_event_mapping_system_control);
		mSystemControlButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String parameters = mSystemControlEditText.getText().toString();

				setMessage("SYSTEM CONTROL EVENT MAPPING");
				mService.systemControlEventMapping(parameters);
			}
		});

		mOTAButton = (Button) findViewById(R.id.btn_ota);
		mOTAButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showFileChooser();
			}
		});

		mStreamUserInputEventsButton = (Button) findViewById(R.id.btn_stream_user_input_events);
		mStreamUserInputEventsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.startStreamingUserInputEvents();
			}
		});


		mConfigurationEditText = (EditText) findViewById(R.id.edit_set_configuration);
		mSerialNumberEditText = (EditText) findViewById(R.id.edit_set_serial_string);
		mConnectionParamsEditText = (EditText) findViewById(R.id.edit_set_connection_parameters);
		mFlashButtonModeEditText = (EditText) findViewById(R.id.edit_set_flash_button_mode);

		mShineApiInfoTextView = (TextView) findViewById(R.id.textView);
		mShineApiInfoTextView.setVerticalScrollBarEnabled(true);
		mShineApiInfoTextView.setHorizontallyScrolling(true);
		mShineApiInfoTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
		mDeviceLabel = (TextView) findViewById(R.id.deviceName);

		Timer rssiTimer = new Timer();
		rssiTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (mState >= BTLE_STATE_CONNECTED) {
					mService.readRssi();
				}
			}
		}, 1000, 1000);

		mActivateButton = (Button) findViewById(R.id.btn_start_or_stop_activating);
		mActivateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setMessage("ACTIVATING");
				mService.startActivating();

			}
		});

		mActivateStateButton = (Button) findViewById(R.id.btn_activation_state);
		mActivateStateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setMessage("GETTING ACTIVATION STATE");
				mService.startGettingActivationState();

			}
		});

		mHIDConnectButton = (Button) findViewById(R.id.hidConnectButton);
		mHIDConnectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDevice != null) {
					setMessage("HID CONNECT - success=" + mService.hidConnect(mDevice));
				}
			}
		});

		mHIDDisconnectButton = (Button) findViewById(R.id.hidDisconnectButton);
		mHIDDisconnectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDevice != null) {
					setMessage("HID DISCONNECT - success=" + mService.hidDisconnect(mDevice));
				}
			}
		});

		mInputMethodButton = (Button) findViewById(R.id.inputMethodButton);
		mInputMethodButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
				if (imeManager != null) {
					imeManager.showInputMethodPicker();
				} else {
					Toast.makeText(ShineActivity.this, "Can NOT show InputMethodPicker", Toast.LENGTH_LONG).show();
				}
			}
		});

		// Quoc-Hung Le
		mAdvFlagEditText = (EditText) findViewById(R.id.txtAdvFlag);
		mGetSetAdvStateCheckBox = (CheckBox) findViewById(R.id.cbGetSetAdvState);

		mAdStateButton = (Button) findViewById(R.id.btnAdvState);
		mAdStateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGetSetAdvStateCheckBox.isChecked()) {
					setMessage("SET EXTRA AD DATA STATE");
					mService.startSettingAdvEventState(Boolean.parseBoolean(mAdvFlagEditText.getText().toString()));
				} else {
					setMessage("GET EXTRA AD DATA STATE");
					mService.startGettingAdvEventState();
				}
			}
		});

		mInterruptButton = (Button) findViewById(R.id.btnShineInterrupt);
		mInterruptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopCurrentOperation();
			}
		});

		mShineToPlutoButton = (Button) findViewById(R.id.btnShineToPluto);
		mShineToPlutoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ShineActivity.this, PlutoActivity.class);
				startActivity(i);
				finish();
			}
		});

		mShineToBoltButton = (Button) findViewById(R.id.btnPlutoToBolt);
		mShineToBoltButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ShineActivity.this, BoltActivity.class);
				startActivity(i);
				finish();
			}
		});

		mSDKVersion = (TextView)findViewById(R.id.sdk_version);
		mSDKVersion.setText(SDKSetting.getSDKVersion());

		mUnmapAllEventsButton = (Button) findViewById(R.id.btn_unmap_all_events);
		mUnmapAllEventsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setMessage("UNMAP ALL EVENTS");
				mService.unmapAllEvents();
			}
		});

		mUnmapSpecificEventButton = (Button) findViewById(R.id.btn_unmap_specific_event);
		mUnmapSpecificEventButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMessage("UNMAP SPECIFIC EVENT");
				mService.unmapEvent(CustomModeEnum.MemEventNumber.DOUBLE_PRESS_N_HOLD);
			}
		});


		mCustomModeButton = (Button) findViewById(R.id.btn_custom_mode);
		mCustomModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMessage("SET CUSTOM MODE");
				mService.setCustomMode(CustomModeEnum.ActionType.HID_MEDIA, CustomModeEnum.MemEventNumber.PLUTO_TRIPLE_TAP,
						CustomModeEnum.AnimNumber.TRIPLE_PRESS_SUCCEEDED, CustomModeEnum.KeyCode.MEDIA_VOLUME_UP_OR_SELFIE, true);
			}
		});
		ButterKnife.bind(this);
	}

	@OnClick(R.id.btn_is_streaming)
	void isStreaming(){
		setMessage("isStreaming="+mService.isUserEventStreaming());
	}

	@OnClick(R.id.btn_bmw)
	void gotoBmw() {
		Intent intent = new Intent(this, BmwActivity.class);
		startActivity(intent);
	}

	public void onTestBluetooth(View v) {
		switch (v.getId()) {
			case R.id.btn_enable_bt:
				if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
					ShineAdapter.getDefaultAdapter(this).disableBluetooth();
				} else {
					ShineAdapter.getDefaultAdapter(this).enableBluetooth();
				}
				Toast.makeText(this, R.string.enable_bluetooth, Toast.LENGTH_SHORT).show();
				break;
		}
	}

    public void openTestConnect(View v){
        Intent intent = new Intent(this, TestSyncAndConnectActivity.class);
        intent.putExtras(TestSyncAndConnectActivity.getOpenBundle(mDevice));
        startActivity(intent);
    }

	public void getLastErrorCode(View v) {
		int connErrCode = mService.getLastConnectionErrorCode();
		StringBuilder msgBuilder = new StringBuilder();
		switch(connErrCode) {
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
						onFirmwareSelected(object.path+object.names.get(0));
					}
				}
				break;
			default:
				Log.e(TAG, "wrong request Code");
				break;
		}
	}

	@Override
	protected void setUiState() {
		super.setUiState();

		mScanButton.setEnabled(mState <= BTLE_STATE_SCANNING);
		mScanButton.setText(mState != BTLE_STATE_SCANNING ? R.string.scan : R.string.stop_scan);

		mConnectButton.setEnabled(mDevice != null && mState < BTLE_STATE_CLOSED);

		mCloseButton.setEnabled(mState >= BTLE_STATE_CLOSED);

		mCreateBondButton.setEnabled(mDevice != null);
		mClearBondButton.setEnabled(mDevice != null);

		mHIDConnectButton.setEnabled(mDevice != null);
		mHIDDisconnectButton.setEnabled(mDevice != null);

		mAnimateButton.setEnabled(isReady);

		mConfigButton.setEnabled(isReady);

		mGetStreamConfigButton.setEnabled(isReady);
		mSetStreamConfigButton.setEnabled(isReady);

		mChangeSNButton.setEnabled(isReady);

		mConnectionParametersButton.setEnabled(isReady);

		mFlashButtonModeButton.setEnabled(isReady);

		mSyncButton.setEnabled(isReady);

		mOTAButton.setEnabled(isReady);

		mStreamUserInputEventsButton.setEnabled(isReady);

		mButtonAnimationButton.setEnabled(isReady);

		mMapEventAnimationButton.setEnabled(isReady);
		mUnmapEventAnimationMapping.setEnabled(isReady);

		mSystemControlButton.setEnabled(isReady);

		mActivateButton.setEnabled(isReady);
		mActivateStateButton.setEnabled(isReady);

		mCustomModeButton.setEnabled(isReady);
		mUnmapSpecificEventButton.setEnabled(isReady);
		mUnmapAllEventsButton.setEnabled(isReady);

		mAdStateButton.setEnabled(isReady);
		mGetSetAdvStateCheckBox.setEnabled(isReady);

		mStopAnimationButton.setEnabled(mState >= BTLE_STATE_CLOSED);

		mShineToPlutoButton.setEnabled(isReady);
		mShineToBoltButton.setEnabled(isReady);

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
		if(TextUtils.isEmpty(mMessage)){
			return;
		}
		if(mShineApiInfoTextView.getText().length()>500){
			String msg = mShineApiInfoTextView.getText().toString();
			mShineApiInfoTextView.setText(msg.substring(300, msg.length()));
			mShineApiInfoTextView.scrollTo(0,0);
		}
		mShineApiInfoTextView.append(mMessage+"\n");
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
		try {
			Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
			intent.putExtra(ExFilePicker.SET_ONLY_ONE_ITEM, true);
			intent.putExtra(ExFilePicker.SET_START_DIRECTORY, SharedPreferencesUtils.readConfig(this, CONFIG_LAST_OPEN_DIR, Environment.getExternalStorageDirectory().getAbsolutePath()));
			startActivityForResult(intent, REQUEST_FIRMWARE_SELECTION);
//			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//			// intent.setType("*/*"); // this setup needs to test on many kinds of devices
//			startActivityForResult(intent, REQUEST_FIRMWARE_SELECTION);
		} catch (android.content.ActivityNotFoundException ex) {	// If there is no a File Explorer
			showHandmadeFileChooser();
		}
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
