package com.misfit.ble.interfaces;

public interface IBluetoothGattDescriptor {

	public Object getRealInstance();
	
	public IBluetoothGattCharacteristic getCharacteristic();
	
	public String getUUID();
	
	public boolean setValue(byte[] bytes);
	
	public byte[] getValue();
	
}
