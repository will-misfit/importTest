package com.misfit.ble.interfaces;

import java.util.List;

public interface IBluetoothGattCharacteristic {

	public Object getRealInstance();
	
	public IBluetoothGattService getService();
	
	public List<IBluetoothGattDescriptor> getDescriptors();
	
	public IBluetoothGattDescriptor getDescriptor(String descriptorUUID);
	
	public String getUUID();
	
	public boolean setValue(byte[] bytes);
	
	public byte[] getValue();
	
	public String getStringValue(int offset);
	
	public void setWriteType(int writeType);
	
	public int getWriteType();
	
	public int getProperties();
}
