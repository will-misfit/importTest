package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetLapCountingLicenseInfoRequest extends Request {

    private byte[] mLicenseInfo;

    @Override
    public String getRequestName() {
        return "SetLapCountingLicenseInfo";
    }

    @Override
    public int getTimeOut() {
        return 0;
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    public void buildRequest(byte[] licenseInfo) {
        mLicenseInfo = licenseInfo;
        byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
        byte parameterId = Constants.LapCounting.PARAMETER_ID_LAP_COUNTING;
        byte commandId = Constants.LapCounting.COMMAND_ID_LAP_COUNTING;
        byte settingId = Constants.LapCounting.SETTING_ID_LAP_COUNTING;


        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + licenseInfo.length);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.put(0, operation);
        byteBuffer.put(1, parameterId);
        byteBuffer.put(2, commandId);
        byteBuffer.put(3, settingId);
        byteBuffer.position(4);
        byteBuffer.put(licenseInfo);

        mRequestData = byteBuffer.array();
    }

    @Override
    public JSONObject getRequestDescriptionJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("LicenseInfo", mLicenseInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    @Override
    public void onRequestSent(int status) {
        mIsCompleted = true;
    }
}
