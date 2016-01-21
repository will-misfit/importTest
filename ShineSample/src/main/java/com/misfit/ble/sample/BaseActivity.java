package com.misfit.ble.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;

import com.misfit.ble.shine.core.DeviceTransparentCommand;
import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.shine.result.UserInputEvent;

/**
 * Created by Quoc-Hung Le on 8/24/15.
 */
public class BaseActivity extends Activity {

	private static String TAG = "BaseActivity";

	// iVars
	protected static int mState;
	protected static int mRssi;
	protected static String mMessage;

	protected static ShineDevice mDevice = null;

	protected static final int BTLE_STATE_IDLE = 0;
	protected static final int BTLE_STATE_SCANNING = 1;
	protected static final int BTLE_STATE_CLOSED = 2;
	protected static final int BTLE_STATE_CONNECTING = 3;
	protected static final int BTLE_STATE_CONNECTED = 4;

	protected MisfitShineService mService = null;

	protected boolean isReady = false;
	protected boolean isBusy = false;
	protected boolean isStreamConfigReady = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setUpShineService();
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart() mService= " + mService);
	}

	@Override
	public void onDestroy() {
		unbindService(mServiceConnection);
		stopService(new Intent(this, MisfitShineService.class));
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		updateUi();
	}

	/**
	 * BTLE Event Handler
	 */
	protected Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
				case MisfitShineService.SHINE_SERVICE_INITIALIZED:
					updateUi();
					break;
				case MisfitShineService.SHINE_SERVICE_CONNECTED:
					setState(BTLE_STATE_CONNECTED);
					setMessage(message.getData().getString(MisfitShineService.EXTRA_MESSAGE));
					break;
				case MisfitShineService.SHINE_SERVICE_CLOSED:
					setState(BTLE_STATE_IDLE);
					setMessage("CLOSED");
					break;
				case MisfitShineService.SHINE_SERVICE_OPERATION_END:
					setState(BTLE_STATE_CONNECTED);
					setMessage(message.getData().getString(MisfitShineService.EXTRA_MESSAGE));
					break;
				case MisfitShineService.SHINE_SERVICE_OTA_RESET:
				case MisfitShineService.SHINE_SERVICE_OTA_PROGRESS_CHANGED:
				case MisfitShineService.SHINE_SERVICE_BUTTON_EVENTS:
					setMessage(message.getData().getString(MisfitShineService.EXTRA_MESSAGE));
					break;
				case MisfitShineService.SHINE_SERVICE_RSSI_READ:
					mRssi = message.getData().getInt(MisfitShineService.EXTRA_RSSI);
					updateUi();
					break;
				case MisfitShineService.SHINE_SERVICE_STREAMING_USER_INPUT_EVENTS_RECEIVED_EVENT:
					int eventId = message.arg1;
					onHandleUserInputEvent(eventId);
					setMessage(message.getData().getString(MisfitShineService.EXTRA_MESSAGE));
				default:
					super.handleMessage(message);
			}
		}
	};

	protected void onHandleUserInputEvent(int eventId){
		if(eventId== UserInputEvent.EVENT_TYPE_QUADRA_TAP_BEGIN || eventId==UserInputEvent.EVENT_TYPE_QUADRA_TAP_END){
			Log.d(TAG,"send quadra tap received animation");
			mService.startButtonAnimation(DeviceTransparentCommand.Animation.QUADRA_TAP_RECEIVED, (byte) 1);
		}
	}

	/**
	 * Shine Service
	 */
	protected void setUpShineService() {
		Intent bindIntent = new Intent(this, MisfitShineService.class);
		startService(bindIntent);
		bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * UI
	 */
	protected void setState(int state) {
		mState = state;
	}

	protected void updateUi() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setUiState();
			}
		});
	}

	protected void setUiState() {
		Log.d(TAG, "update UI for state: " + mState);

		/* Enable another buttons except Interrupt button */
		isReady = (mService != null && mService.isReady());
		isBusy = (mService != null && mService.isBusy());
		isStreamConfigReady = (mService != null && (mService.isReady() || mService.isStreaming()));

		if (mState == BTLE_STATE_IDLE) {
			keepScreenOn(false);
		} else {
			keepScreenOn(true);
		}
	}

	protected ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			mService = ((MisfitShineService.LocalBinder) rawBinder).getService();
			mService.setHandler(mHandler);
		}

		public void onServiceDisconnected(ComponentName classname) {
			mService = null;
		}
	};

	public void keepScreenOn(boolean enabled) {
		if (enabled) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	protected void stopCurrentOperation() {
		mService.interrupt();
	}

	protected void setMessage(String message) {
		mMessage = message;
		updateUi();
	}
}
