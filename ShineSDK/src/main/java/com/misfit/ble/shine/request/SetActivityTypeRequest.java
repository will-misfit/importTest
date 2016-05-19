package com.misfit.ble.shine.request;

import com.misfit.ble.setting.speedo.ActivityType;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.core.MisfitProtocol;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetActivityTypeRequest extends SetRequest {

    ActivityType mActivityType;

    @Override
    public String getRequestName() {
        return "SetActivityType";
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    public void buildRequest(ActivityType activityType) {
        mActivityType = activityType;

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(0, MisfitProtocol.Operation.SET);
        byteBuffer.put(1, MisfitProtocol.Algorithm.PARAMETER_ID);
        byteBuffer.put(2, MisfitProtocol.Algorithm.COMMAND_ACTIVIT_TYPE);
        byteBuffer.put(3, activityType.getValue());

        mRequestData = byteBuffer.array();
    }

    @Override
    public JSONObject getRequestDescriptionJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("activityType", mActivityType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
