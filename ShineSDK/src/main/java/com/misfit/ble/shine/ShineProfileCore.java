package com.misfit.ble.shine;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.misfit.ble.BuildConfig;
import com.misfit.ble.interfaces.BluetoothGattFactory;
import com.misfit.ble.interfaces.IBluetoothGattCallback;
import com.misfit.ble.interfaces.IBluetoothGattCharacteristic;
import com.misfit.ble.interfaces.IBluetoothGattDescriptor;
import com.misfit.ble.interfaces.IBluetoothGattProfile;
import com.misfit.ble.interfaces.IBluetoothGattService;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.log.LogEventItem.RequestFinishedLog;
import com.misfit.ble.shine.log.LogEventItem.RequestStartedLog;
import com.misfit.ble.shine.log.LogEventItem.ResponseFinishedLog;
import com.misfit.ble.shine.log.LogSession;
import com.misfit.ble.shine.request.OTAPutRequest;
import com.misfit.ble.shine.request.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ShineProfileCore {
	
	public interface ShineCoreCallback {
		void onConnectionStateChange(int status, int newState);
		void onHandshakingResult(int result);
		void onServicesDiscovered();
		void onRequestSentResult(Request request, int result);
		void onFirstResponseReceivedResult(Request request, int result, byte[] data);
		void onResponseReceivedResult(Request request, int result);
        void onSerialNumberReadResult(int result, String serialNumber);
		void onFirmwareVersionReadResult(int result, String firmwareVersion);
		void onModelNumberReadResult(int result, String modelNumber);
		void onReadRemoteRssi(int result, int rssi);
		void onPackageTransferred(int result, int totalSize, int transferredSize);
	}
	
	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_FAILURE = 1;
	public static final int RESULT_TIMED_OUT = 2;
	public static final int RESULT_UNSUPPORTED = 3;
	public static final int RESULT_INSUFFICIENT_AUTHENTICATION = 4;

	public static final long DELAY_FOR_DATA_TRANSFER_RETRY = 40;

	static final String TAG = ShineProfileCore.class.getName();
	static final String TAG_OTA_PUT = "ShineProfile";

	// Characteristics
	private ArrayList<IBluetoothGattCharacteristic> mToSubscribeCharacteristics;

	private volatile Request mCurrentRequest;
	private volatile Request mCurrentButtonRequest;
	
	private ScheduledExecutorService mDataTransferExecutor;
	private ScheduledFuture<?> mDataTransferTask;
	
	private ShineCoreCallback mCallback;
	private LogSession mLogSession;
	private LogEventItem mCurrentLogItem;
	
	public final Object lockObject = new Object();

	private IBluetoothGattProfile mBluetoothGatt = null;
	private ShineDevice mShineDevice = null;

	private Context mContext = null;

    /**
     * Set up
     */
	public ShineProfileCore(Context context, ShineDevice shineDevice) {
//		BluetoothProfileFactory.buildBluetoothProfile(this, this, Arrays.asList(Constants.DISERVICE_UUID, Constants.MFSERVICE_UUID));
		mBluetoothGatt = BluetoothGattFactory.buildBluetoothProfile(context, shineDevice.mBluetoothDevice);
		mShineDevice = shineDevice;
		mContext = context;
	}

	/**
	* update internal BluetoothGatt instance inside AndroidBluetoothProfile to reconnect BLE
	* */
	public void updateIBluetoothGatt () {
		mBluetoothGatt = BluetoothGattFactory.buildBluetoothProfile(mContext, mShineDevice.mBluetoothDevice);
	}
	
	public void setLogSession(LogSession logSession) {
		mLogSession = logSession;
	}
    
    /**
     * GATT client callbacks
     */
	private IBluetoothGattCallback mBluetoothGattCallback = new IBluetoothGattCallback() {
		
		@Override
	    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
			synchronized (ShineProfileCore.this.lockObject) {
				mCurrentRequest = null;
                mCallback.onConnectionStateChange(status, newState);
			}
	    }

	    @Override
	    public void onServicesDiscovered(int status) {
	    	int finalResult = RESULT_FAILURE;
	    	int result = RESULT_FAILURE;
	    	
	    	List<IBluetoothGattCharacteristic> characteristics = null;
	    	
	    	if (status == BluetoothGatt.GATT_SUCCESS) {
	    		result = RESULT_SUCCESS;
	    		
	    		// Verify all required services & characteristics
	    		IBluetoothGattService diService = getService(Constants.DISERVICE_UUID);
	    		IBluetoothGattService mfService = getService(Constants.MFSERVICE_UUID);
	    		
	    		if (diService != null && mfService != null) {
	    			IBluetoothGattCharacteristic firmwareRevisionCharacteristic = diService.getCharacteristic(Constants.DISERVICE_FIRMWARE_REVISION_CHARACTERISTIC_UUID);
	    			IBluetoothGattCharacteristic modelNumberCharacteristic = diService.getCharacteristic(Constants.DISERVICE_MODEL_NUMBER_CHARACTERISTIC_UUID);
		    		IBluetoothGattCharacteristic deviceConfigCharacteristic = mfService.getCharacteristic(Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID);
		    		IBluetoothGattCharacteristic fileControlCharacteristic = mfService.getCharacteristic(Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID);
		    		IBluetoothGattCharacteristic fileDataCharacteristic = mfService.getCharacteristic(Constants.MFSERVICE_FILE_TRANSFER_DATA_CHARACTERISTIC_UUID);

		    		if (firmwareRevisionCharacteristic != null
		    				&& modelNumberCharacteristic != null
		    				&& deviceConfigCharacteristic != null
		    				&& fileControlCharacteristic != null
		    				&& fileDataCharacteristic != null) {
		    			characteristics = Arrays.asList(deviceConfigCharacteristic, fileControlCharacteristic, fileDataCharacteristic);
		    			finalResult = RESULT_SUCCESS;
		    		}
	    		}
	    	}
	    	
	    	JSONObject json = null;
	    	if (result == RESULT_SUCCESS) {
	    		json = new JSONObject();
	    		try {
					json.put("result", finalResult);
					json.put("services", jsonDescription(getServices()));
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    	}
	    	mCurrentLogItem.mResponseFinishedLog = new ResponseFinishedLog(result, json);
	    	
	    	if (finalResult == RESULT_FAILURE) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "onServicesDiscovered(), finalResult is FAILURE, call onHandshakingResult(FAILURE)");
				}
	    		mCallback.onHandshakingResult(RESULT_FAILURE);
	    	} else {
				mCallback.onServicesDiscovered();
	    		resumeHandshaking(characteristics);
	    	}
	    }

	    public void onCharacteristicChanged(IBluetoothGattCharacteristic characteristic) {
	    	synchronized (ShineProfileCore.this.lockObject) {
	    		if (mCurrentRequest == null) {
		    		Log.w(TAG, "onCharacteristicChanged: currentRequest is null");
		    		return;
		    	} else if (!mCurrentRequest.isWaitingForResponse()) {
		    		Log.w(TAG, "onCharacteristicChanged: currentRequest does not expecting any response.");
		    		return;
		    	}
		    	
		    	byte[] bytes = characteristic.getValue();
		    	
		    	mCallback.onFirstResponseReceivedResult(mCurrentRequest, RESULT_SUCCESS, bytes);
		    	
		    	mCurrentRequest.handleResponse(characteristic.getUUID(), bytes);

		    	if (mCurrentRequest.getIsCompleted()) {
		    		mCallback.onResponseReceivedResult(mCurrentRequest, RESULT_SUCCESS);
		    	}
	    	}
	    }

		private int previousStatusCode = -1;

	    public void onCharacteristicRead(IBluetoothGattCharacteristic characteristic, int status) {
	    	String uuid = characteristic.getUUID();

			if (status != BluetoothGatt.GATT_SUCCESS) {
				Log.w(TAG, "onCharacteristicRead - characteristic: " + uuid + ", status: " + status);
			}

            if (Constants.DISERVICE_SERIAL_NUMBER_CHARACTERISTIC_UUID.equals(uuid)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    String serialNumber = characteristic.getStringValue(0);
                    mCallback.onSerialNumberReadResult(RESULT_SUCCESS, serialNumber);
                } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                    mCallback.onSerialNumberReadResult(RESULT_INSUFFICIENT_AUTHENTICATION, null);
                } else if (status == Constants.GATT_AUTH_FAIL || status == Constants.GATT_ERROR) {
					if (previousStatusCode == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
						// Android 4.4.4 -> waiting for the onConnectionStateChanged event then reconnect.
						// FIXME can we remove this clause as it is never entered
					} else {
						mCallback.onSerialNumberReadResult(RESULT_FAILURE, null);
					}
                } else {
                    mCallback.onSerialNumberReadResult(RESULT_FAILURE, null);
                }
            } else if (Constants.DISERVICE_FIRMWARE_REVISION_CHARACTERISTIC_UUID.equals(uuid)) {
	    		if (status == BluetoothGatt.GATT_SUCCESS) {
	    			String firmwareVersion = characteristic.getStringValue(0);
	    			mCallback.onFirmwareVersionReadResult(RESULT_SUCCESS, firmwareVersion);
	        	} else {
	        		mCallback.onFirmwareVersionReadResult(RESULT_FAILURE, null);
	        	}
	    	} else if (Constants.DISERVICE_MODEL_NUMBER_CHARACTERISTIC_UUID.equals(uuid)) {
	    		if (status == BluetoothGatt.GATT_SUCCESS) {
	    			String modelNumber = characteristic.getStringValue(0);
	    			mCallback.onModelNumberReadResult(RESULT_SUCCESS, modelNumber);
	    		} else {
	    			mCallback.onModelNumberReadResult(RESULT_FAILURE, null);
	    		}
	    	}
	    }

		public void onCharacteristicWrite(IBluetoothGattCharacteristic characteristic, int status) {
			synchronized (ShineProfileCore.this.lockObject) {
				Request request = null;

				boolean isCurrentSideRequest = false;
				if (mCurrentRequest != null
						&& mCurrentRequest.getCharacteristicUUID() != null
						&& mCurrentRequest.getCharacteristicUUID().equalsIgnoreCase(characteristic.getUUID())) {
					request = mCurrentRequest;
				}

				if (mCurrentButtonRequest != null
						&& mCurrentButtonRequest.getCharacteristicUUID() != null
						&& mCurrentButtonRequest.getCharacteristicUUID().equalsIgnoreCase(characteristic.getUUID())) {
					request = mCurrentButtonRequest;
					isCurrentSideRequest = true;
				}

				if (request == null) {
					// TODO: Properly handle this case. OTAPutRequest is special, as it actually writes to 2 different characteristics.
					if (mCurrentRequest instanceof OTAPutRequest && characteristic.getUUID().equalsIgnoreCase(Constants.MFSERVICE_FILE_TRANSFER_DATA_CHARACTERISTIC_UUID))
						return;

					Log.w(TAG, "onCharacteristicWrite: unexpected onWrite event on characteristic: " + characteristic.getUUID());
					return;
				}

				request.onRequestSent(status);

				if (status == BluetoothGatt.GATT_SUCCESS) {
					mCallback.onRequestSentResult(request, RESULT_SUCCESS);

					if (isCurrentSideRequest) {
						mCurrentButtonRequest = null;
					}
				} else {
					mCallback.onRequestSentResult(request, RESULT_FAILURE);

					if (!isCurrentSideRequest) {
						mCurrentRequest = null;
					} else {
						mCurrentButtonRequest = null;
					}
				}
			}
		}

	    public void onDescriptorWrite(IBluetoothGattDescriptor descriptor, int status) {}

	    public void onDescriptorRead(IBluetoothGattDescriptor descriptor, int status) {}
	    
	    @Override
	    public void onCharacteristicSubscriptionStateChanged(IBluetoothGattCharacteristic characteristic, boolean enable) {
	    	mCurrentLogItem.mResponseFinishedLog = new ResponseFinishedLog(enable ? RESULT_SUCCESS : RESULT_FAILURE, null);
	    	
	    	if (enable) {
	    		mToSubscribeCharacteristics.remove(characteristic);
	    		continueHandshaking();
	    	}
	    }
	    
	    @Override
	    public void onReadRemoteRssi(int rssi, int status) {
	    	if (status == BluetoothGatt.GATT_SUCCESS) {
	    		mCallback.onReadRemoteRssi(RESULT_SUCCESS, rssi);
	    	} else {
	    		mCallback.onReadRemoteRssi(RESULT_FAILURE, rssi);
	    	}
	    }
	};
	
    /**
     * Misfit Service
     */
    private void continueHandshaking() {
    	if (mToSubscribeCharacteristics.size() > 0) {
    		IBluetoothGattCharacteristic characteristic = mToSubscribeCharacteristics.get(0);
    		
    		mCurrentLogItem = newLogEventItem(LogEventItem.EVENT_SUBSCRIBE_CHARACTERISTIC);
    		JSONObject jsonObject = new JSONObject();
    		try {
				jsonObject.put("characteristic", jsonDescription(characteristic));
			} catch (JSONException e) {
				e.printStackTrace();
			}
    		mCurrentLogItem.mRequestStartedLog = new RequestStartedLog(jsonObject);
    		
    		boolean result = toggleCharacteristicSubscription(characteristic, true);
    		
    		mCurrentLogItem.mRequestFinishedLog = new RequestFinishedLog(result ? RESULT_SUCCESS : RESULT_FAILURE);
    	} else {
    		mCallback.onHandshakingResult(RESULT_SUCCESS);
    	}
    }
    
    private void resumeHandshaking(List<IBluetoothGattCharacteristic> toSubscribeCharacteristic) {
        mToSubscribeCharacteristics = new ArrayList<>(toSubscribeCharacteristic);
        continueHandshaking();
    }
    
    /**
     * Public Interface - Wrapper
     */
	public boolean connect(boolean autoConnect, ShineCoreCallback callback) {
		if (!mBluetoothGatt.connect(autoConnect, mBluetoothGattCallback)) {
			return false;
		}
		
		mCallback = callback;
		return true;
	}

    public boolean connect() {
    	return mBluetoothGatt.connect();
    }

	public void disconnect() {
		synchronized (lockObject) {
			mBluetoothGatt.disconnect();
    		mCurrentRequest = null;
		}
    }

    public boolean readRemoteRssi() {
		return mBluetoothGatt.readRemoteRssi();
    }

    public ShineDevice getDevice() {
		if (mShineDevice != null && mShineDevice.mBluetoothDevice.equals(mBluetoothGatt.getDevice()))
			return mShineDevice;
		return null;
	}

	public void close() {
    	synchronized (lockObject) {
			mBluetoothGatt.close();
			mCurrentRequest = null;
			if (mDataTransferTask != null) {
				mDataTransferTask.cancel(true);
				mDataTransferTask = null;
			}
    	}
	}

	/**
     * Public Interface
     */
    public boolean readSerialNumber() {
        IBluetoothGattService diService = getService(Constants.DISERVICE_UUID);
        if (diService == null)
            return false;

        IBluetoothGattCharacteristic serialNumberCharacteristic = diService.getCharacteristic(Constants.DISERVICE_SERIAL_NUMBER_CHARACTERISTIC_UUID);
        if (serialNumberCharacteristic == null)
            return false;

        return readCharacteristic(serialNumberCharacteristic);
    }

    public boolean readFirmwareVersion() {
    	IBluetoothGattService diService = getService(Constants.DISERVICE_UUID);
    	if (diService == null)
    		return false;
    	
    	IBluetoothGattCharacteristic firmwareRevisionCharacteristic = diService.getCharacteristic(Constants.DISERVICE_FIRMWARE_REVISION_CHARACTERISTIC_UUID);
    	if (firmwareRevisionCharacteristic == null)
    		return false;
    	
    	return readCharacteristic(firmwareRevisionCharacteristic);
    }
    
    public boolean readModelNumber() {
    	IBluetoothGattService diService = getService(Constants.DISERVICE_UUID);
    	if (diService == null)
    		return false;
    	
    	IBluetoothGattCharacteristic modelNumberCharacteristic = diService.getCharacteristic(Constants.DISERVICE_MODEL_NUMBER_CHARACTERISTIC_UUID);
    	if (modelNumberCharacteristic == null)
    		return false;
    	
    	return readCharacteristic(modelNumberCharacteristic);
    }
    
    public boolean handshake() {
    	mCurrentLogItem = newLogEventItem(LogEventItem.EVENT_DISCOVER_SERVICES);
		mCurrentLogItem.mRequestStartedLog = new RequestStartedLog(null);
		
		boolean result = mBluetoothGatt.discoverServices();
		
		mCurrentLogItem.mRequestFinishedLog = new RequestFinishedLog(result ? RESULT_SUCCESS : RESULT_FAILURE);
    	return result;
    }
    
    public boolean sendRequest(Request request) {
    	synchronized (lockObject) {
    		if (mCurrentRequest != null && !mCurrentRequest.getIsCompleted())
        		return false;
        	
        	IBluetoothGattService service = getService(Constants.MFSERVICE_UUID);
        	if (service == null)
        		return false;
        	
        	IBluetoothGattCharacteristic characteristic = service.getCharacteristic(request.getCharacteristicUUID());
        	if (characteristic == null)
        		return false;
        	
        	byte[] bytes = request.getRequestData();
        	if (!characteristic.setValue(bytes))
        		return false;
        	
        	if (!writeCharacteristic(characteristic))
            	return false;

            mCurrentRequest = request;
        	startTimeOutTimer(mCurrentRequest);
        	return true;
		}
    }
    
    public boolean sendButtonRequest(Request request) {
    	synchronized (lockObject) {
    		if (mCurrentButtonRequest != null && !mCurrentButtonRequest.getIsCompleted())
        		return false;
    		
        	IBluetoothGattService service = getService(Constants.MFSERVICE_UUID);
        	if (service == null)
        		return false;
        	
        	IBluetoothGattCharacteristic characteristic = service.getCharacteristic(request.getCharacteristicUUID());
        	if (characteristic == null)
        		return false;
        	
        	byte[] bytes = request.getRequestData();
        	if (!characteristic.setValue(bytes))
        		return false;
        	
        	if (!writeCharacteristic(characteristic))
            	return false;

        	mCurrentButtonRequest = request;
        	return true;
    	}
    }
    
    // Transfer Data
    public void transferData(final byte[] data, final float interpacketDelay) {
    	IBluetoothGattService service = getService(Constants.MFSERVICE_UUID);
    	if (service == null)
    		return;
    	
    	final IBluetoothGattCharacteristic characteristic = service.getCharacteristic(Constants.MFSERVICE_FILE_TRANSFER_DATA_CHARACTERISTIC_UUID);
    	if (characteristic == null)
    		return;
    	
    	Runnable runnableDataTransfer = new Runnable() {
    		private static final int PACKAGE_LENGTH = 20;
    		
    		private boolean mIsInitialized = false;
    		private int mTotalLength;
    		private int mTransferedLength;
    		private byte mSequenceNumber;
    		
    		private boolean mHasDelayAmongPackages; 
    		
    		private ByteBuffer mByteBuffer = null;
    		byte[] bytes;
    		
    		private void initialize() {
    			mByteBuffer = ByteBuffer.wrap(data);
				mTotalLength = data.length;
				mTransferedLength = 0;
				mSequenceNumber = 0;
				mHasDelayAmongPackages = (interpacketDelay > 0);
				mIsInitialized = true;
    		}
    		
			@Override
			public void run() {
				if (!mIsInitialized) {
					initialize();
				}
				
				do {
					int remaining = mByteBuffer.remaining();
					if (remaining <= 0) {
						break;
					}

					int packageLength = 1 + Math.min(remaining, PACKAGE_LENGTH - 1);
					if (bytes == null || packageLength != bytes.length) {
						bytes = new byte[packageLength];
					}

					bytes[0] = mSequenceNumber;
					mByteBuffer.get(bytes, 1, bytes.length - 1);

					boolean result = false;
					if (characteristic.setValue(bytes)) {
						if (writeCharacteristic(characteristic)) {
							mSequenceNumber += 1;
							mTransferedLength += packageLength - 1;
							result = true;
						} else {
							if (BuildConfig.DEBUG) {
								Log.w(TAG_OTA_PUT, "retry once");
							}
							try {
								Thread.sleep(DELAY_FOR_DATA_TRANSFER_RETRY);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (writeCharacteristic(characteristic)) {
								mSequenceNumber += 1;
								mTransferedLength += packageLength - 1;
								result = true;
							} else if (BuildConfig.DEBUG) {
								Log.e(TAG_OTA_PUT, "retry failed");
							}
						}
					}
					mCallback.onPackageTransferred(result ? RESULT_SUCCESS : RESULT_FAILURE, mTotalLength, mTransferedLength);
					
				} while (!mHasDelayAmongPackages);
			}
		};
    	
		if (mDataTransferExecutor == null) {
    		mDataTransferExecutor = Executors.newSingleThreadScheduledExecutor();
    	}
		
		if (interpacketDelay > 0) {
			mDataTransferTask = mDataTransferExecutor.scheduleWithFixedDelay(runnableDataTransfer, 0, (long) Math.ceil(interpacketDelay * 1000), TimeUnit.MICROSECONDS);
		} else {
			mDataTransferTask = mDataTransferExecutor.schedule(runnableDataTransfer, 0, TimeUnit.MILLISECONDS);
		}
    }
    
    public void stopTransferData() {
    	if (mDataTransferTask != null) {
    		mDataTransferTask.cancel(true);
    	}
    }
    
    /**
     * Timer
     */
    private Timer mRequestTimeOutTimer = new Timer();
    private RequestTimedOutTimerTask mCurrentRequestTimeOutTimerTask = null;
    
    private class RequestTimedOutTimerTask extends TimerTask {
    	public boolean mIsCancelled = false;
    	public Request mRequest;
    	
    	public RequestTimedOutTimerTask(Request request) {
    		super();
    		mRequest = request;
    	}
    	
		@Override
		public void run() {
			synchronized (ShineProfileCore.this.lockObject) {
				if (!mIsCancelled && !mRequest.getIsCompleted()) {
					ShineProfileCore.this.onRequestTimedOut(mRequest, this);
				}
			}
		}
    }
    
    private void onRequestTimedOut(Request request, RequestTimedOutTimerTask timerTask) {
    	if (timerTask == mCurrentRequestTimeOutTimerTask) {
    		mCurrentRequestTimeOutTimerTask = null;
    	}
    	mCallback.onResponseReceivedResult(request, RESULT_TIMED_OUT);
    	
    	if (request.equals(mCurrentRequest)) {
    		mCurrentRequest = null;
    	}
    }
    
    private void startTimeOutTimer(Request request) {
    	if (mCurrentRequestTimeOutTimerTask != null) {
    		mCurrentRequestTimeOutTimerTask.mIsCancelled = true;
			mCurrentRequestTimeOutTimerTask.cancel();
    	}
    	
    	if (mCurrentRequest != null && mCurrentRequest.getTimeOut() > 0) {	
    		mCurrentRequestTimeOutTimerTask = new RequestTimedOutTimerTask(mCurrentRequest);
    		mRequestTimeOutTimer.schedule(mCurrentRequestTimeOutTimerTask, mCurrentRequest.getTimeOut());
    	}
    }
    
    /**
     * Log
     */
    private LogEventItem newLogEventItem(String eventName) {
		LogEventItem logEventItem = new LogEventItem(eventName);
		if (mLogSession != null) {
			mLogSession.addLogItem(logEventItem);
		}
		return logEventItem;
	}
    
    private JSONArray jsonDescription(List<IBluetoothGattService> services) {
    	if (services == null)
    		return null;
    	
    	JSONArray jsonArray = new JSONArray();
    	for (IBluetoothGattService service : services) {
			jsonArray.put(jsonDescription(service));
		}
    	return jsonArray;
    }
    
    private JSONObject jsonDescription(IBluetoothGattService service) {
    	if (service == null)
    		return null;
    	
    	JSONObject jsonObject = new JSONObject();
    	try {
			jsonObject.put("uuid", service.getUUID());
			JSONArray jsonArray = new JSONArray();
			
			List<IBluetoothGattCharacteristic> characteristics =  service.getCharacteristic();
			if (characteristics != null) {
				for (IBluetoothGattCharacteristic characteristic : characteristics) {
					JSONObject jsonCharacteristic = new JSONObject();
					try {
						jsonCharacteristic.put("uuid", characteristic.getUUID());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					jsonArray.put(jsonCharacteristic);
				}
			}
			
			jsonObject.put("characteristics", jsonArray);
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return jsonObject;
    }
    
    private JSONObject jsonDescription(IBluetoothGattCharacteristic characteristic) {
    	if (characteristic == null)
    		return null;
    	
    	JSONObject jsonObject = new JSONObject();
    	try {
			jsonObject.put("uuid", characteristic.getUUID());
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return jsonObject;
    }

	/**
	 * Utility
	 */
	protected IBluetoothGattService getService(String serviceUUID) {
		return mBluetoothGatt.getService(serviceUUID);
	}

	protected List<IBluetoothGattService> getServices() {
		return mBluetoothGatt.getServices();
	}

	/**
	 * Misfit Service
	 */
	protected boolean toggleCharacteristicSubscription(IBluetoothGattCharacteristic characteristic, boolean enable) {
		if (characteristic == null) {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "toggleCharacteristicSubscription(), characteristic is null, return false to stop the handshaking");
			}
			Log.e(TAG, "characteristic not found");
			return false;
		}

		return mBluetoothGatt.toggleCharacteristicSubscription(characteristic, enable);
	}

	protected boolean readCharacteristic(IBluetoothGattCharacteristic characteristic) {
		return mBluetoothGatt.readCharacteristic(characteristic);
	}

	protected boolean writeCharacteristic(IBluetoothGattCharacteristic characteristic) {
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}
}
