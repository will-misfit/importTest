package com.misfit.ble.interfaces;

import java.util.List;

public interface IBluetoothGattService {
	
	public Object getRealInstance();
	
	public String getUUID();
	
	public List<IBluetoothGattCharacteristic> getCharacteristic();
	
	public IBluetoothGattCharacteristic getCharacteristic(String characteristicUUID);
	
}
