package com.misfit.ble.samsung.v2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.misfit.ble.interfaces.IBluetoothGattCallback;
import com.misfit.ble.interfaces.IBluetoothGattCharacteristic;
import com.misfit.ble.interfaces.IBluetoothGattDescriptor;
import com.misfit.ble.interfaces.IBluetoothGattProfile;
import com.misfit.ble.interfaces.IBluetoothGattService;
import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class SamsungBluetoothProfile_v2 implements IBluetoothGattProfile {

	private boolean isCharacteristicSubscribing = false;
	private boolean isCharacteristicUnsubscribing = false;
	private boolean isTryingToConnect = false;
	
	private IBluetoothGattCallback mMisfitGattCallback;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothDevice mBluetoothDevice = null;
	private boolean mAutoConnect = false;
	
	public SamsungBluetoothProfile_v2(Context context, BluetoothDevice bluetoothDevice) {
		mBluetoothDevice = bluetoothDevice;
		BluetoothGattAdapter.getProfileProxy(context, mProfileServiceListener, BluetoothGattAdapter.GATT);
	}
	
	private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
		
		@Override
		public void onAppRegistered(int status) {
			// Do Nothing
		};
		
		@Override
		public void onReadRemoteRssi(BluetoothDevice bluetoothDevice, int rssi, int status) {
			mMisfitGattCallback.onReadRemoteRssi(rssi, status);
		};
		
		@Override
		public void onReliableWriteCompleted(BluetoothDevice arg0, int arg1) {};
		
		
		@Override
		public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
			isTryingToConnect = false;
			mMisfitGattCallback.onConnectionStateChange(device, status, newState);
		}

		@Override
	    public void onServicesDiscovered(BluetoothDevice device, int status) {
	    	mMisfitGattCallback.onServicesDiscovered(status);
	    }

		@Override
	    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
	    	mMisfitGattCallback.onCharacteristicChanged(SamsungBluetoothGattCharacteristic_v2.getInstance(characteristic));
	    }

		@Override
	    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
	    	mMisfitGattCallback.onCharacteristicRead(SamsungBluetoothGattCharacteristic_v2.getInstance(characteristic), status);
	    }
	    
		@Override
	    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
	    	mMisfitGattCallback.onCharacteristicWrite(SamsungBluetoothGattCharacteristic_v2.getInstance(characteristic), status);
	    }
	    
		@Override
	    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
	    	IBluetoothGattDescriptor iDescriptor = SamsungBluetoothGattDescriptor_v2.getInstance(descriptor);
	    	if ((isCharacteristicSubscribing || isCharacteristicUnsubscribing) && iDescriptor.getUUID().equals(IBluetoothGattProfile.DESCRIPTOR_UUID)) {
	    		setCharacteristicNotificationPhase3(iDescriptor, isCharacteristicSubscribing);
	    	} else {
	    		mMisfitGattCallback.onDescriptorWrite(iDescriptor, status);
	    	}
	    }
	    
		@Override
	    public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status) {
	    	IBluetoothGattDescriptor iDescriptor = SamsungBluetoothGattDescriptor_v2.getInstance(descriptor);
	    	if ((isCharacteristicSubscribing || isCharacteristicUnsubscribing) && iDescriptor.getUUID().equals(IBluetoothGattProfile.DESCRIPTOR_UUID)) {
	    		setCharacteristicNotificationPhase2(iDescriptor, isCharacteristicSubscribing);
	    	} else {
	    		mMisfitGattCallback.onDescriptorRead(iDescriptor, status);
	    	}
	    }
	};
	
	/**
     * Profile Listener
     */
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @SuppressLint("NewApi")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothGattAdapter.GATT) {
                mBluetoothGatt = (BluetoothGatt) proxy;
                mBluetoothGatt.registerApp(mBluetoothGattCallback);;
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothGattAdapter.GATT) {
                SamsungBluetoothProfile_v2.this.cleanUp();
            }
        }
    };
    
    private void cleanUp() {
    	if (mBluetoothGatt == null)
    		return;
    	
    	List<BluetoothDevice> connectedDevices = getConnectedDevices();
		if (connectedDevices != null) {
			for (BluetoothDevice bluetoothDevice : connectedDevices) {
				mBluetoothGatt.cancelConnection(bluetoothDevice);
			}
		}
		
		mBluetoothGatt.unregisterApp();
		mBluetoothGatt = null;
    }
	
	@Override
	public void close() {
		if (mBluetoothGatt != null) {
			BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT, mBluetoothGatt);
		}
	}

	@Override
	public void disconnect() {
		isTryingToConnect = false;
		if (mBluetoothGatt != null && mBluetoothDevice != null) {
			mBluetoothGatt.cancelConnection(mBluetoothDevice);
		}
	}

	@Override
	public boolean connect(boolean autoConnect, IBluetoothGattCallback callback) {
		mMisfitGattCallback = callback;
		mAutoConnect = autoConnect;
		
		if (mBluetoothGatt == null) {
			isTryingToConnect = true;
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					if (isTryingToConnect == false)
						return;
					if (connectImpl(SamsungBluetoothProfile_v2.this.mMisfitGattCallback))
						return;
					
					mBluetoothGattCallback.onConnectionStateChange(SamsungBluetoothProfile_v2.this.mBluetoothDevice, -1, BluetoothProfile.STATE_DISCONNECTED);
				}
			}, 3000);
			return true;
		}
		return connectImpl(callback);
	}
	
	@Override
	public boolean connect() {
		return connectImpl(mMisfitGattCallback);
	};
	
	private boolean connectImpl(IBluetoothGattCallback callback) {
		if (mBluetoothGatt == null || mBluetoothDevice == null || callback == null)
			return false;
		
		return mBluetoothGatt.connect(mBluetoothDevice, mAutoConnect);
	}

	@Override
	public BluetoothDevice getDevice() {
		if (mBluetoothGatt == null)
			return null;
		
		return mBluetoothDevice;
	}

	@Override
	public boolean discoverServices() {
		if (mBluetoothGatt == null || mBluetoothDevice == null)
			return false;
		
		return mBluetoothGatt.discoverServices(mBluetoothDevice);
	}

	@Override
	public boolean beginReliableWrite() {
		if (mBluetoothGatt == null || mBluetoothDevice == null)
			return false;
		
		return mBluetoothGatt.beginReliableWrite(mBluetoothDevice);
	}

	@Override
	public boolean executeReliableWrite() {
		if (mBluetoothGatt == null || mBluetoothDevice == null)
			return false;
		
		return mBluetoothGatt.executeReliableWrite(mBluetoothDevice);
	}

	@Override
	public void abortReliableWrite(BluetoothDevice bluetoothDevice) {
		if (mBluetoothGatt == null)
			return;
		
		mBluetoothGatt.abortReliableWrite(bluetoothDevice);		
	}

	@Override
	public boolean readRemoteRssi() {
		if (mBluetoothGatt == null || mBluetoothDevice == null)
			return false;
		
		return mBluetoothGatt.readRemoteRssi(mBluetoothDevice);
	}

	@Override
	public int getConnectionState(BluetoothDevice device) {
		if (mBluetoothGatt == null)
			return BluetoothProfile.STATE_DISCONNECTED;
		
		return mBluetoothGatt.getConnectionState(device);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BluetoothDevice> getConnectedDevices() {
		if (mBluetoothGatt == null)
			return null;
		
		return mBluetoothGatt.getConnectedDevices();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
		if (mBluetoothGatt == null)
			return null;
		
		return mBluetoothGatt.getDevicesMatchingConnectionStates(states);
	}

	@Override
	public List<IBluetoothGattService> getServices() {
		if (mBluetoothGatt == null || mBluetoothDevice == null)
			return null;
		
		@SuppressWarnings("unchecked")
		List<BluetoothGattService> services = mBluetoothGatt.getServices(mBluetoothDevice);
		if (services == null)
			return null;
		
		List<IBluetoothGattService> iServices = new ArrayList<IBluetoothGattService>();
		for (BluetoothGattService service : services) {
			iServices.add(SamsungBluetoothGattService_v2.getInstance(service));
		}
		return iServices;
	}

	@Override
	public IBluetoothGattService getService(String serviceUUID) {
		if (mBluetoothGatt == null || mBluetoothDevice == null)
			return null;
		
		BluetoothGattService service = mBluetoothGatt.getService(mBluetoothDevice, UUID.fromString(serviceUUID));
		if (service == null)
			return null;
		
		return SamsungBluetoothGattService_v2.getInstance(service);
	}

	@Override
	public boolean readCharacteristic(IBluetoothGattCharacteristic characteristic) {
		if (mBluetoothGatt == null)
			return false;
		
		return mBluetoothGatt.readCharacteristic((BluetoothGattCharacteristic)characteristic.getRealInstance());
	}

	@Override
	public boolean writeCharacteristic(IBluetoothGattCharacteristic characteristic) {
		if (mBluetoothGatt == null)
			return false;
		
		return mBluetoothGatt.writeCharacteristic((BluetoothGattCharacteristic)characteristic.getRealInstance());
	}

	@Override
	public boolean readDescriptor(IBluetoothGattDescriptor descriptor) {
		if (mBluetoothGatt == null)
			return false;
		
		return mBluetoothGatt.readDescriptor((BluetoothGattDescriptor)descriptor.getRealInstance());
	}

	@Override
	public boolean writeDescriptor(IBluetoothGattDescriptor descriptor) {
		if (mBluetoothGatt == null)
			return false;
		
		return mBluetoothGatt.writeDescriptor((BluetoothGattDescriptor)descriptor.getRealInstance());
	}

	@Override
	public boolean setCharacteristicNotification(IBluetoothGattCharacteristic characteristic, boolean enable) {
		if (mBluetoothGatt == null)
			return false;
		
		return mBluetoothGatt.setCharacteristicNotification((BluetoothGattCharacteristic)characteristic.getRealInstance(), enable);
	}
	
	@Override
	public boolean toggleCharacteristicSubscription(IBluetoothGattCharacteristic characteristic, boolean enable) {
		if (mBluetoothGatt == null)
			return false;
		
		isCharacteristicSubscribing = enable;
		isCharacteristicUnsubscribing = !enable;
		
		IBluetoothGattDescriptor descriptor = characteristic.getDescriptor(IBluetoothGattProfile.DESCRIPTOR_UUID);
		if (descriptor == null)
			return false;
		
		return readDescriptor(descriptor);
	}

	private boolean setCharacteristicNotificationPhase2(IBluetoothGattDescriptor descriptor, boolean enable) {
		if (mBluetoothGatt == null)
			return false;
		
		IBluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
		if (characteristic == null)
			return false;
		
		if (setCharacteristicNotification(characteristic, enable) == false)
			return false;
		
		byte[] value = enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
		if (descriptor.setValue(value) == false)
			return false;
		
		return writeDescriptor(descriptor);
	}
	
	private void setCharacteristicNotificationPhase3(IBluetoothGattDescriptor descriptor, boolean enable) {
		if (mBluetoothGatt == null)
			return;
		
		isCharacteristicSubscribing = false;
		isCharacteristicUnsubscribing = false;
		
		mMisfitGattCallback.onCharacteristicSubscriptionStateChanged(descriptor.getCharacteristic(), enable);
	}
}
