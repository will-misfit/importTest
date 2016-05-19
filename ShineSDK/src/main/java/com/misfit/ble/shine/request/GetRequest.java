package com.misfit.ble.shine.request;

public abstract class GetRequest extends Request{

    @Override
    public void handleResponse(String characteristicUUID, byte[] bytes) {
        onReceived(characteristicUUID, bytes);
    }

    abstract protected void onReceived(String characteristicUUID, byte[] bytes);
}
