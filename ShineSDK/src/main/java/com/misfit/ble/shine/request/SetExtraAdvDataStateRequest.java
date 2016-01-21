package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 8/7/15.
 */
public class SetExtraAdvDataStateRequest extends Request {

    private static final String TAG = "SetExtraAdvDataRequest";

    private boolean mAdvDataState;

    @Override
    public int getTimeOut() {
        return 0;
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    private byte getOperation() {
        return Constants.DEVICE_CONFIG_OPERATION_SET;
    }

    public byte getParameterId() {
        return Constants.DEVICE_CONFIG_PARAMETER_ID_DEVICE_SETTINGS_IN_OUT;
    }

    public byte getSettingId() {
        return Constants.DEVICE_SETTING_ID_EXTRA_ADVERTISING_DATA_STATE;
    }

    public void buildRequest(boolean advDataState) {
        mAdvDataState = advDataState;

        byte operation = getOperation();
        byte parameterId = getParameterId();
        byte settingsId = getSettingId();

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.put(0, operation);
        byteBuffer.put(1, parameterId);
        byteBuffer.put(2, settingsId);
        byteBuffer.put(3, Convertor.byteFromBoolean(mAdvDataState));

        mRequestData = byteBuffer.array();
    }

    @Override
    public void buildRequestWithParams(String paramsString) {
        String[] params = paramsString.split(",");
        if (params.length != 1)
            return;

        boolean enable = Boolean.parseBoolean(params[0]);
        buildRequest(enable);
    }

    @Override
    public JSONObject getRequestDescriptionJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("advDataState", mAdvDataState);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public String getParamsHint() {
        return "true/false";
    }

    @Override
    public void onRequestSent(int status) {
        mIsCompleted = true;
    }
}
