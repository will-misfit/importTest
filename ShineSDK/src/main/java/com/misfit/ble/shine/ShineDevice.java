package com.misfit.ble.shine;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.misfit.ble.android.AndroidGattProfile;
import com.misfit.ble.android.AndroidHIDProfile;
import com.misfit.ble.setting.SDKSetting;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public final class ShineDevice implements Parcelable {
	private static final String TAG = ShineDevice.class.getName();
	/**
	 * Key to serialize/deserialize ShineDevice from bundle
	 */
	/*package*/ static final String MAC_ADDRESS_KEY = "mac_address";
	/*package*/ static final String SERIAL_NUMBER_KEY = "serial_number";

	/*package*/ BluetoothDevice mBluetoothDevice;

	private String mSerialNumber;
	//FIXME: remove ShineProfile
	private WeakReference<ShineProfile> mGattProfileRef;

    // After pairing, the internal object became invalid.
    private boolean mIsInvalid;
	
	/*package*/ ShineDevice(BluetoothDevice bluetoothDevice, String serialNumber) {
		super();
		mBluetoothDevice = bluetoothDevice;
		mSerialNumber = serialNumber;
        mIsInvalid = false;
	}

	/*package*/ ShineDevice(BluetoothDevice bluetoothDevice) {
		super();
		mBluetoothDevice = bluetoothDevice;
		mSerialNumber = null;
		mIsInvalid = false;
	}

	public String getSerialNumber() {
		return mSerialNumber;
	}
	
	/*package*/ void setSerialNumber(String serialNumber) {
		mSerialNumber = serialNumber;
	}

	public ShineProfile getShineProfile() {
		if (mGattProfileRef == null)
			return null;

		ShineProfile gattProfile = mGattProfileRef.get();
		if (gattProfile == null || gattProfile.getState() == ShineProfile.State.CLOSED)
			return null;

		return gattProfile;
	}

	/*package*/ void setShineProfile(ShineProfile shineProfile) {
		mGattProfileRef = new WeakReference<>(shineProfile);
	}

	public boolean isInvalid() {
		return mIsInvalid;
	}

    /*package*/ void invalidate() {
        mIsInvalid = true;
    }

	/*package*/ void onDeviceDiscovered() {
		mIsInvalid = false;
	}

    public static final Parcelable.Creator<ShineDevice> CREATOR = new Parcelable.Creator<ShineDevice>() {
        public ShineDevice createFromParcel(Parcel in) {
			ShineDevice device = null;
			Log.d("SHINE_PARCEL", "createFromParcel: " + in.toString());
			Bundle bundle = in.readBundle();
			if (bundle != null) {
				String macAddress = bundle.getString(MAC_ADDRESS_KEY);
				device = ShineDeviceFactory.getCachedDevice(macAddress);
				Log.d("SHINE_PARCEL", "createFromParcel - macAddress: " + macAddress);
				// NOTE: support passing Parcel to a different process
				if (device == null) {
					String serialNumber = bundle.getString(SERIAL_NUMBER_KEY);
					Log.d("SHINE_PARCEL", "createFromParcel - serialNumber: " + serialNumber);
					device = ShineDeviceFactory.getShineDevice(macAddress, serialNumber);
				}
			}
			Log.d("SHINE_PARCEL", "device: " + device);
            return device;
        }

        public ShineDevice[] newArray(int size) {
			return null;
        }
    };

	@Override
	public int describeContents() {
		return 0;
	}
    
    @Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle bundle = new Bundle();
		bundle.putString(MAC_ADDRESS_KEY, getAddress());
		if (getSerialNumber() != null) {
			bundle.putString(SERIAL_NUMBER_KEY, getSerialNumber());
		}
		// TODO: is it okay not to put mGattProfileRef to Parcel?
		dest.writeBundle(bundle);
	}
    
    @Override
    public boolean equals(Object o) {
    	if (o instanceof ShineDevice) {
    		ShineDevice other = (ShineDevice)o;
    		
    		if (!TextUtils.isEmpty(mSerialNumber)) {
    			if (mSerialNumber.equals(other.mSerialNumber)
    					&& mIsInvalid == other.mIsInvalid) {
        			return super.equals(other);
        		}
    		}
        }
        return false;
    }
    
    @Override
    public int hashCode() {
    	int hashCode = super.hashCode();
    	
    	if (!TextUtils.isEmpty(mSerialNumber)) {
    		hashCode += mSerialNumber.hashCode() * 10;
    	}
    	return hashCode;
    }
    
    public ShineProfile connectProfile(Context context, boolean autoConnect,
									   ShineProfile.ConnectionCallback connectionCallback) throws IllegalStateException {
		SDKSetting.validateSettings();

		if (!ShineAdapter.getDefaultAdapter(context).isEnabled()) {
			Log.d(TAG, "connectProfile() return null: BluetoothAdapter is not enabled");
			return null;
		}

		if (isInvalid()) {
			Log.d(TAG, "connectProfile() return null: invalidated");
			return null;
		}

		ShineProfile existingGattProfile = getShineProfile();
		if (existingGattProfile != null) {
			Log.d(TAG, "connectProfile() return null: ShineProfile exists");
			return null;
		}

		ShineProfile shineProfile = new ShineProfile(context, this);
		if (!shineProfile.connect(autoConnect, connectionCallback))
			return null;

		setShineProfile(shineProfile);
		return shineProfile;
	}

	public String getName() {
		return mBluetoothDevice.getName();
	}

	public String getAddress() {
		return mBluetoothDevice.getAddress();
	}

	public int getBondState() {
		return mBluetoothDevice.getBondState();
	}

	public boolean createBond() { return mBluetoothDevice.createBond(); }

	public boolean removeBond() {
		Log.d(TAG, "REMOVE BOND with " + getAddress());
		boolean result;
		try {
			Method localMethod;
			localMethod = mBluetoothDevice.getClass().getMethod("removeBond", new Class[0]);
			if (localMethod == null) {
				Log.e(TAG, "localMethod NOT found???");
				return false;
			}

			result = (Boolean) localMethod.invoke(mBluetoothDevice, new Object[0]);
			Log.d(TAG, "removeBond - success?: " + result);
		} catch (Exception localException) {
			Log.e(TAG, "removeBond - got exception!");
			localException.printStackTrace();
			result = false;
		}
		return result;
	}

	public interface ShineHIDConnectionCallback {
		void onHIDConnectionStateChanged(ShineDevice device, int state);
	}

	private ShineHIDConnectionCallback mHIDConnectionCallback;

	public void registerHIDConnectionCallback(ShineHIDConnectionCallback callback) {
		mHIDConnectionCallback = callback;
		AndroidHIDProfile.getSharedInstance().registerHIDConnectionCallback(mBluetoothDevice, new AndroidHIDProfile.HIDConnectionCallback() {
			@Override
			public void onHIDConnectionStateChanged(BluetoothDevice device, int state) {
				if (mHIDConnectionCallback != null) {
					mHIDConnectionCallback.onHIDConnectionStateChanged(ShineDevice.this, state);
				}
			}
		});
	}

	public boolean hidConnect() {
		if (getBondState() == BluetoothDevice.BOND_BONDING)
			return false;

		return AndroidHIDProfile.getSharedInstance().connect(mBluetoothDevice);
	}

	public boolean hidDisconnect() {
		return AndroidHIDProfile.getSharedInstance().disconnect(mBluetoothDevice);
	}

	public int getGattConnectionState() {
		return AndroidGattProfile.getSharedInstance().getConnectionState(mBluetoothDevice);
	}
}
