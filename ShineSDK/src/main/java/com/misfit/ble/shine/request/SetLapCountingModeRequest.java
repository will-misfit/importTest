package com.misfit.ble.shine.request;

import com.misfit.ble.setting.lapCounting.LapCountingMode;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetLapCountingModeRequest extends Request {
    private byte mLapCountingMode;
    private byte mLapCountingTimeout;

    @Override
    public String getRequestName() {
        return "SetLapCountingMode";
    }

    @Override
    public int getTimeOut() {
        return 0;
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    public void buildRequest(LapCountingMode mode, short timeout) {
        byte operationId = Constants.DEVICE_CONFIG_OPERATION_SET;
        byte parameterId = Constants.LapCounting.PARAMETER_ID_MODE;
        byte commandId = Constants.LapCounting.COMMAND_ID_MODE;
        byte settingId = Constants.LapCounting.SETTING_ID_MODE;
        byte extra = Constants.LapCounting.EXTRA_MODE;

        mLapCountingMode = mode.getMode();
        mLapCountingTimeout = Convertor.unsignedByteFromShort(timeout);

        ByteBuffer byteBuffer = ByteBuffer.allocate(7);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(0, operationId);
        byteBuffer.put(1, parameterId);
        byteBuffer.put(2, commandId);
        byteBuffer.put(3, settingId);
        byteBuffer.put(4, extra);
        byteBuffer.put(5, mLapCountingMode);
        byteBuffer.put(6, mLapCountingTimeout);

        mRequestData = byteBuffer.array();
    }

    @Override
    public JSONObject getResponseDescriptionJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("lapCountingMode", mLapCountingMode);
            json.put("lapCountingTimeout", mLapCountingTimeout);
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
