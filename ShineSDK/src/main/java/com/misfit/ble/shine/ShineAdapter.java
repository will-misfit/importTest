package com.misfit.ble.shine;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.misfit.ble.android.AndroidGattProfile;
import com.misfit.ble.android.AndroidHIDProfile;
import com.misfit.ble.interfaces.BluetoothGattFactory;
import com.misfit.ble.interfaces.IBluetoothGattAdapter;
import com.misfit.ble.setting.SDKSetting;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.log.LogManager;
import com.misfit.ble.shine.log.LogUtilHelper;
import com.misfit.ble.shine.log.ScanLogSession;
import com.misfit.ble.util.Convertor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.misfit.ble.shine.log.LogUtilHelper.LogEventItemPosition.ResponseFinished;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public final class ShineAdapter {

	private static final short SERVICE_AD_TYPE_START = 2;
	private static final short SERVICE_AD_TYPE_END = 7;

	// Manufacturer Specific Data AD Structure: 
	// AD Length [1 bytes] - AD Type [1 bytes] - Company ID [2 bytes] - Serial Number [10 bytes] - Extra Data [0-14 bytes]
	private static final short MANUFACTURER_AD_TYPE = 0xff;
	private static final int MANUFACTURER_AD_TYPE_LENGTH = 1;
	private static final int MANUFACTURER_COMPANY_ID_LENGTH = 2;
	private static final int MANUFACTURER_SERIAL_NUMBER_LENGTH = 10;
	private static ScanLogSession mScanLogSession;
	private static final Object SCANLogWriteLock = new Object();

    private AtomicInteger mRestartScanningIndex = new AtomicInteger(0);
    private LEScanCallbackWrapper mCurrScanCallbackWrapper;
    private AtomicBoolean mClientScanningOn = new AtomicBoolean(false);
    private static Timer mRestartScanningTimer = new Timer("ShineAdapterRestartScanning");
	
	public interface ShineScanCallback {
		void onScanResult(ShineDevice device, int rssi);
	}

	public interface ShineScanCallbackForTest extends ShineScanCallback {
	}

	public interface ShineRetrieveCallback  {
		void onConnectedShinesRetrieved(List<ShineDevice> connectedShines);
	}
	
	private static class LEScanCallbackWrapper implements IBluetoothGattAdapter.IBluetoothGattScanCallback {
		
		/*
		 * Class Level
		 */
		private static HashMap<ShineScanCallback, LEScanCallbackWrapper> mCache = new HashMap<>();
		
		private static LEScanCallbackWrapper getInstance(ShineScanCallback callback) {
			if (callback == null) return null;

			LEScanCallbackWrapper wrapper = mCache.get(callback);
			if (wrapper == null) {
				wrapper = new LEScanCallbackWrapper();
				wrapper.mCallback = callback;
				mCache.put(callback, wrapper);
			}
			return wrapper;
		}
		
		private static LEScanCallbackWrapper getExistingInstance(ShineScanCallback callback) {
			return mCache.get(callback);
		}
		
		private static void flushCachedInstance(ShineScanCallback callback) {
			mCache.remove(callback);
		}
		
		/*
		 * Instance Level
		 */
		private ShineScanCallback mCallback;

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String deviceName = device.getName();
			String macAddr = device.getAddress();
			boolean isLogged = false;

			if (isSupportedDevice(scanRecord)) {
				String serialNumber = ShineAdapter.parseSerialNumber(scanRecord);
				if (serialNumber != null) {
					// scan log need to execute ASAP as App would call stopScanning()
					// if it finds its required device
					new WriteScanResultInScanLogTask().execute(deviceName, macAddr, serialNumber, scanRecord);
					isLogged = true;
					ShineDevice shineDevice = ShineDeviceFactory.getShineDevice(device, serialNumber);
					if (shineDevice != null) {
						shineDevice.onDeviceDiscovered();
						shineDevice.setSerialNumber(serialNumber);
						mCallback.onScanResult(shineDevice, rssi);
					}
				}
			}

			if (!isLogged) {
				new WriteScanResultInScanLogTask().execute(deviceName, macAddr, null, scanRecord);
			}
		}
		
		@Override
		public void onBatchScanResults(List<ScanResult> results) {
            if (results == null)    return;
			for (ScanResult result : results) {
				onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
			}
		}

		@Override
		// callback when scan could not be started
		public void onScanFailed(int errorCode) {
			if (mScanLogSession != null) {
				mScanLogSession.appendScanFailError(errorCode);
			}
			sShineAdapter.internalStopScanning();
		}

		@Override
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		// callback when a BLE advertisement data is found
		public void onScanResult(int callbackType, ScanResult result) {
            if (result == null || result.getScanRecord() == null) {
				return;
			}
			onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
		}

		private static boolean isSupportedDevice(byte[] scanRecord) {
			List<String> supportedServices = getSupportedServicesFromAdvertisementData(scanRecord);
			return supportedServices.contains(Constants.MFSERVICE_UUID_WITHOUT_SEPERATOR_UPPERCASED);
		}
	} 
	
	/*
	 * Class Level
	 */
	private static ShineAdapter sShineAdapter;
	public static ShineAdapter getDefaultAdapter(Context context) {
		if (context == null)
			return null;
		
		LogManager.getDefault().uploadLogSession();
		
		if (sShineAdapter == null) {
			IBluetoothGattAdapter bluetoothAdapter = BluetoothGattFactory.buildBluetoothAdapter(context);
			if (bluetoothAdapter != null) {
				sShineAdapter = new ShineAdapter(bluetoothAdapter);
			}
		}
		return sShineAdapter;
	}

	/*
	 * Instance Level
	 */
	private IBluetoothGattAdapter mBluetoothAdapter;
	private ShineAdapter(IBluetoothGattAdapter bluetoothAdapter) {
		mBluetoothAdapter = bluetoothAdapter;
	}

	public boolean isEnabled() {
		return BluetoothAdapter.getDefaultAdapter().isEnabled();
	}

	/**
	 * enable Bluetooth
	 *
	 * @return true if bluetooth startup has began or bluetooth already on
	 */
	public boolean enableBluetooth() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (!adapter.isEnabled()) {
			return adapter.enable();
		} else {
			return true;
		}
	}

	/**
	 * @return true if bluetooth shutdown has began or bluetooth already off
	 */
	public boolean disableBluetooth() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter.isEnabled()) {
			return adapter.disable();
		} else {
			return true;
		}
	}

	public boolean startScanning(final ShineScanCallback callback) throws IllegalStateException {
		SDKSetting.validateSettings();
		if (!isEnabled()) {
			throw new IllegalStateException("Bluetooth is not enabled!");
		}

        if (mClientScanningOn.get()) {
            return false;
        }
        mClientScanningOn.set(true);

		if (mScanLogSession == null) {
			mScanLogSession = new ScanLogSession();
		}
		mScanLogSession.start(callback);

		LEScanCallbackWrapper wrapper = LEScanCallbackWrapper.getInstance(callback);
        mCurrScanCallbackWrapper = wrapper;
		// NOTE: scan with SCAN_SERVICES_UUIDs filter does not work with Nexus 6/9 on Android 5.1.1.
		return mBluetoothAdapter.startScanning(wrapper);
	}

    /**
     * used among internal retry scan scenario, compared to startScanning(),
     * - without creating a new LogSession
     * - if Bluetooth is not enabled, not to throw exception
     * - apply the same callback from startScanning()
     * */
    public boolean internalStartScanning() {
        if (!isEnabled() || mCurrScanCallbackWrapper == null) {
            return false;
        }
        mRestartScanningIndex.addAndGet(1);
        mScanLogSession.internalStart(mRestartScanningIndex.get());
        return mBluetoothAdapter.startScanning(mCurrScanCallbackWrapper);
    }
	
	public void stopScanning(final ShineScanCallback callback) {
		if (!isEnabled()) return;
        mClientScanningOn.set(false);  // whatever happens, Client don't want scan any more

		LEScanCallbackWrapper wrapper = LEScanCallbackWrapper.getExistingInstance(callback);
		if (wrapper == null) return;

		mBluetoothAdapter.stopScanning(wrapper);
		LEScanCallbackWrapper.flushCachedInstance(callback);

		mScanLogSession.stop(callback);
		LogManager.getDefault().saveAndUploadLog(mScanLogSession);
		mScanLogSession.clearLogItems();
		ShineDeviceFactory.saveDevicesCache();
	}

    /**
     * used among internal retry scan scenario, compared to stopScanning()
     * - without end log session
     * - without removing callback wrapper from cache
     * */
    public void internalStopScanning() {
        if (!isEnabled() || mCurrScanCallbackWrapper == null) {
            return;
        }
        mBluetoothAdapter.stopScanning(mCurrScanCallbackWrapper);
        mScanLogSession.internalStop();

        if (!mClientScanningOn.get()) {
            return;
        }

        mRestartScanningTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mClientScanningOn.get()) {
                    internalStartScanning();
                }
            }
        }, Constants.INTERNAL_BEFORE_RESTART_SCAN);
    }

	private ShineRetrieveCallback mShineRetrieveCallback;
	public void getConnectedShines(ShineRetrieveCallback callback) {
		SDKSetting.validateSettings();

		boolean isInProgress = (mShineRetrieveCallback != null);
		mShineRetrieveCallback = callback;
		if (isInProgress)
			return;

		final Set<ShineDevice> connectedShines = new HashSet<>();

		List<ShineDevice> connectedGatts = getGattConnectedShines(); // return non null List<>
		connectedShines.addAll(connectedGatts);

		getHIDConnectedShines(new ShineRetrieveCallback() {
			@Override
			public void onConnectedShinesRetrieved(List<ShineDevice> connectedHIDs) {
				if (connectedHIDs != null) {
					connectedShines.addAll(connectedHIDs);
				}

				new WriteConnectedDeviceInScanLogTask().execute(new ArrayList<>(connectedShines));

				ShineRetrieveCallback callback = mShineRetrieveCallback;
				mShineRetrieveCallback = null;
				callback.onConnectedShinesRetrieved(new ArrayList<>(connectedShines));
			}
		});
	}

	private ShineRetrieveCallback mShineHIDRetrieveCallback;
	public void getHIDConnectedShines(ShineRetrieveCallback callback) {
		SDKSetting.validateSettings();

		boolean isInProgress = (mShineHIDRetrieveCallback != null);
		mShineHIDRetrieveCallback = callback;
		if (isInProgress)
			return;

		AndroidHIDProfile.getSharedInstance().getConnectedHIDDevices(new AndroidHIDProfile.RetrieveConnectedHIDCallback() {
			@Override
			public void onConnectedHIDRetrieved(List<BluetoothDevice> connectedHIDs) {
				List<ShineDevice> shineDevices = filterShineDevices(connectedHIDs);

				ShineRetrieveCallback callback = mShineHIDRetrieveCallback;
				mShineHIDRetrieveCallback = null;
				callback.onConnectedShinesRetrieved(shineDevices);
			}
		});
	}

	public List<ShineDevice> getGattConnectedShines() {
		SDKSetting.validateSettings();
		List<BluetoothDevice> gattConnectedDevices = AndroidGattProfile.getSharedInstance().getConnectedGattDevices();
		List<ShineDevice> shineDevices = filterShineDevices(gattConnectedDevices);
		return shineDevices;
	}

	private List<ShineDevice> filterShineDevices(List<BluetoothDevice> bluetoothDevices) {
		ArrayList<ShineDevice> shineDevices = new ArrayList<>();
		for (BluetoothDevice device : bluetoothDevices) {
			String deviceName = device.getName();
			if (deviceName != null
					&& (deviceName.contains("Shine")
						|| deviceName.contains("Flash")
						|| deviceName.length() == 8 /* Encrypted Bluetooth Ad Name's length in Bolt control mode*/)) {
				ShineDevice shineDevice = ShineDeviceFactory.getShineDevice(device); // returned ShineDevice may has no serial number if it is not in cache
				if (shineDevice != null) {
					shineDevice.onDeviceDiscovered();
					shineDevices.add(shineDevice);
				}
			}
		}
		return shineDevices;
	}
	
	/**
	 * Parse serial number
	 */
	private static String parseSerialNumber(byte[] advertisementData) {
		byte[] serialNumberData = getSerialNumberDataFromAdvertisementData(advertisementData);
		if (serialNumberData != null) {
			return new String(serialNumberData);
		}
    	return null;
    }
	
	private static byte[] getSerialNumberDataFromAdvertisementData(byte[] advertisementData) {
		int index = 0;
		
		while (index < advertisementData.length) {
			int length = advertisementData[index++];
			if (length == 0){
				break;
			}
			
			short type = Convertor.unsignedByteToShort(advertisementData[index]);
			if (type == 0) {
				break; 
			} else if (type == MANUFACTURER_AD_TYPE 
					&& length >= MANUFACTURER_AD_TYPE_LENGTH + MANUFACTURER_COMPANY_ID_LENGTH + MANUFACTURER_SERIAL_NUMBER_LENGTH) {
				return Arrays.copyOfRange(advertisementData, 
						index + MANUFACTURER_AD_TYPE_LENGTH + MANUFACTURER_COMPANY_ID_LENGTH, 
						index + MANUFACTURER_AD_TYPE_LENGTH + MANUFACTURER_COMPANY_ID_LENGTH + MANUFACTURER_SERIAL_NUMBER_LENGTH); 
			}
			index += length;
		}
		return null;
	}

	/*
     * - ScanRecord structure: list of items.
     * - Item structure:
     *  + Length: 1 byte
     *  + Type: 1 byte
     *  + Value: length byte(s).
     *
     * - Service UUID item:
     *  + Type: 2 (const defined in Bluetooth 4.0 Spec).
     *  + Length: x (depending on service uuid).
     *  + Value: serviceUUID
     *
     * This code is used to check whether the discovered bluetooth device is a Bolt unit by checking whether has BOLT_SERVICE_ID.
     */
	private static List<String> getSupportedServicesFromAdvertisementData(byte[] advertisementData) {
		ArrayList<String> serviceUUIDs = new ArrayList<>();

		int index = 0;
		while (index < advertisementData.length) {
			int length = advertisementData[index++] & 0xff;
			if (length == 0)
				break;

			short type = Convertor.unsignedByteToShort(advertisementData[index]);
			if (SERVICE_AD_TYPE_START <= type && type <= SERVICE_AD_TYPE_END) {
				byte[] serviceUUIDRaw = Arrays.copyOfRange(advertisementData, index + 1, index + length);
				String serviceUUID = Convertor.bytesToStringInReverse(serviceUUIDRaw);
				serviceUUIDs.add(serviceUUID);
			}
			index += length;
		}

		return serviceUUIDs;
	}

	class WriteConnectedDeviceInScanLogTask extends AsyncTask<List<ShineDevice>, Void, Void> {
		@Override
		protected void onPreExecute() {}

		@Override
		protected Void doInBackground(List<ShineDevice>... passings) {
			if (passings == null || passings.length < 1) return null;

			List<ShineDevice> shineDevices = passings[0];
			for (ShineDevice device : shineDevices) {
				LogEventItem logEventItem
					= LogUtilHelper.makeConnectedDeviceEventItem(device, ResponseFinished);
				if (mScanLogSession != null) {
					synchronized (SCANLogWriteLock) {
						mScanLogSession.addLogEventItem(logEventItem);
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void obj){}
	}

	static class WriteScanResultInScanLogTask extends AsyncTask<Object, Void, Void> {
		@Override
		protected void onPreExecute() {}

		@Override
		protected Void doInBackground(Object... passings) {
			if (passings == null || passings.length != 4) return null;

			String deviceName = (String)passings[0];
			String macAddr = (String)passings[1];
			String serialNumber = (String)passings[2];
			byte[] scanRecord = (byte[])passings[3];

			LogEventItem logEventItem
				= LogUtilHelper.makeScanResultEventItem(deviceName, macAddr, serialNumber, scanRecord);
			if (mScanLogSession != null) {
				synchronized (SCANLogWriteLock) {
					mScanLogSession.addLogEventItem(logEventItem);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void obj){}
	}
}
