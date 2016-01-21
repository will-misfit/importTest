package com.misfit.ble.android;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;

import com.misfit.ble.interfaces.IBluetoothGattCallback;
import com.misfit.ble.interfaces.IBluetoothGattCharacteristic;
import com.misfit.ble.interfaces.IBluetoothGattDescriptor;
import com.misfit.ble.interfaces.IBluetoothGattProfile;
import com.misfit.ble.interfaces.IBluetoothGattService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AndroidBluetoothProfile implements IBluetoothGattProfile {

	private boolean isCharacteristicSubscribing = false;
	private boolean isCharacteristicUnsubscribing = false;
	
	private Context mContext;
	private IBluetoothGattCallback mMisfitGattCallback;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothGatt mBluetoothGatt;
    private Object LOCK_GATT = new Object();
	
	public AndroidBluetoothProfile(Context context, BluetoothDevice bluetoothDevice) {
		mContext = context;
		mBluetoothDevice = bluetoothDevice;
	}
    
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		// onConnectionStateChange() for CONNECTED is possible to be invoked before connect() returns
		// so we need to set the BluetoothGatt instance in that case
    	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
    		if (gatt == null) return;

			if (mBluetoothGatt == null) {
				synchronized (LOCK_GATT) {
					mBluetoothGatt = gatt;
				}
			} else if (!mBluetoothGatt.equals(gatt)) {
				return;
			}

    		mMisfitGattCallback.onConnectionStateChange(gatt.getDevice(), status, newState);
    	}
    	
    	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    		if (gatt == null || gatt.equals(mBluetoothGatt) == false) 
    			return;
    		mMisfitGattCallback.onServicesDiscovered(status);
    	}
    	
    	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
    		if (gatt == null || gatt.equals(mBluetoothGatt) == false) 
    			return;
    		mMisfitGattCallback.onCharacteristicChanged(AndroidBluetoothGattCharacteristic.getInstance(characteristic));
    	}
    	
    	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    		if (gatt == null || gatt.equals(mBluetoothGatt) == false) 
    			return;
    		mMisfitGattCallback.onCharacteristicRead(AndroidBluetoothGattCharacteristic.getInstance(characteristic), status);
    	}
    	
    	public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    		if (gatt == null || gatt.equals(mBluetoothGatt) == false) 
    			return;
    		mMisfitGattCallback.onCharacteristicWrite(AndroidBluetoothGattCharacteristic.getInstance(characteristic), status);
    	}
    	
    	public void onDescriptorRead(BluetoothGatt gatt, android.bluetooth.BluetoothGattDescriptor descriptor, int status) {
    		if (gatt == null || gatt.equals(mBluetoothGatt) == false) 
    			return;
    		mMisfitGattCallback.onDescriptorRead(AndroidBluetoothGattDescriptor.getInstance(descriptor), status);
    	}
    	
    	public void onDescriptorWrite(BluetoothGatt gatt, android.bluetooth.BluetoothGattDescriptor descriptor, int status) {
    		if (gatt == null || gatt.equals(mBluetoothGatt) == false) 
    			return;
    		
    		IBluetoothGattDescriptor iDescriptor = AndroidBluetoothGattDescriptor.getInstance(descriptor);
	    	if ((isCharacteristicSubscribing || isCharacteristicUnsubscribing) && iDescriptor.getUUID().equals(IBluetoothGattProfile.DESCRIPTOR_UUID)) {
	    		onToggleCharacteristicSubscriptionFinished(iDescriptor, isCharacteristicSubscribing);
	    	} else {
	    		mMisfitGattCallback.onDescriptorWrite(AndroidBluetoothGattDescriptor.getInstance(descriptor), status);
	    	}
    	}
    	
    	public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
    		if (gatt == null || gatt.equals(mBluetoothGatt) == false) 
    			return;
    		mMisfitGattCallback.onReadRemoteRssi(rssi, status);
    	}
    	
    	public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
    		if (gatt == null || gatt.equals(mBluetoothGatt) == false) return;
    		// TODO: Not Implemented
    	}
	};

	@Override
	public boolean toggleCharacteristicSubscription(IBluetoothGattCharacteristic characteristic, boolean enable) {
		isCharacteristicSubscribing = enable;
		isCharacteristicUnsubscribing = !enable;
		
		if (mBluetoothGatt == null 
				|| mBluetoothGatt.setCharacteristicNotification((BluetoothGattCharacteristic)characteristic.getRealInstance(), enable) == false)
			return false;
		
		IBluetoothGattDescriptor descriptor = characteristic.getDescriptor(IBluetoothGattProfile.DESCRIPTOR_UUID);
		if (descriptor == null)
			return false;
		
		byte[] value = enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
		if (descriptor.setValue(value) == false)
			return false;
		
		return writeDescriptor(descriptor);
	}
	
	private void onToggleCharacteristicSubscriptionFinished(IBluetoothGattDescriptor descriptor, boolean enable) {
		isCharacteristicSubscribing = false;
		isCharacteristicUnsubscribing = false;
		
		mMisfitGattCallback.onCharacteristicSubscriptionStateChanged(descriptor.getCharacteristic(), enable);
	}

	@Override
	public void close() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
	}

	@Override
	public void disconnect() {
		if (mBluetoothGatt == null) 
			return;
		
		mBluetoothGatt.disconnect();		
	}

	@Override
	public boolean connect(boolean autoConnect, IBluetoothGattCallback callback) {
		mMisfitGattCallback = callback;
		BluetoothGatt gatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback); // set @param autoConnect is false
		synchronized (LOCK_GATT) {
			mBluetoothGatt = gatt;
		}
		return (mBluetoothGatt != null);
	}
	
	@Override
	public boolean connect() {
		if (mBluetoothGatt != null) {
			return mBluetoothGatt.connect();
		}
		return false;
	}

	@Override
	public BluetoothDevice getDevice() {
		if (mBluetoothGatt != null && mBluetoothDevice != null && mBluetoothDevice.equals(mBluetoothGatt.getDevice())) {
			return mBluetoothDevice;
		}
		return null;
	}

	@Override
	public boolean discoverServices() {
		if (mBluetoothGatt == null) 
			return false;
		
		return mBluetoothGatt.discoverServices();
	}

	@Override
	public List<IBluetoothGattService> getServices() {
		if (mBluetoothGatt == null) 
			return null;
		
		List<BluetoothGattService> services = mBluetoothGatt.getServices();
		
		List<IBluetoothGattService> iServices = new ArrayList<IBluetoothGattService>();
		for (BluetoothGattService service : services) {
			iServices.add(AndroidBluetoothGattService.getInstance(service));
		}
		return iServices;
	}

	@Override
	public IBluetoothGattService getService(String serviceUUID) {
		if (mBluetoothGatt == null) 
			return null;
		
		BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(serviceUUID));
		if (service == null)
			return null;
		
		return AndroidBluetoothGattService.getInstance(service);
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
	public boolean beginReliableWrite() {
		if (mBluetoothGatt == null) 
			return false;
		
		return mBluetoothGatt.beginReliableWrite();
	}

	@Override
	public boolean executeReliableWrite() {
		return mBluetoothGatt.executeReliableWrite();
	}

	@Override
	public void abortReliableWrite(BluetoothDevice device) {
		if (mBluetoothGatt == null) 
			return;
		
		mBluetoothGatt.abortReliableWrite(device);		
	}

	@Override
	public boolean setCharacteristicNotification(IBluetoothGattCharacteristic characteristic, boolean enable) {
		if (mBluetoothGatt == null) 
			return false;
		
		return mBluetoothGatt.setCharacteristicNotification((BluetoothGattCharacteristic)characteristic.getRealInstance(), enable);
	}

	@Override
	public boolean readRemoteRssi() {
		if (mBluetoothGatt == null) 
			return false;
		
		return mBluetoothGatt.readRemoteRssi();
	}

	@Override
	public int getConnectionState(BluetoothDevice device) {
		if (mBluetoothGatt == null) 
			return 0;
		
		return mBluetoothGatt.getConnectionState(device);
	}

	@Override
	public List<BluetoothDevice> getConnectedDevices() {
		if (mBluetoothGatt == null) 
			return null;
		
		return mBluetoothGatt.getConnectedDevices();
	}

	@Override
	public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
		if (mBluetoothGatt == null) 
			return null;
		
		return mBluetoothGatt.getDevicesMatchingConnectionStates(states);
	}
}
