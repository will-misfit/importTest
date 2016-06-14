package com.misfit.ble.shine.request;


import com.misfit.ble.setting.MappingType;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.core.MisfitProtocol;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GetMappingTypeRequest extends GetRequest {

    private Response mResponse;

    public static class Response extends BaseResponse {
        public MappingType mappingType;
    }

    @Override
    public String getRequestName() {
        return "GetMappingType";
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    private byte getParameterId() {
        return MisfitProtocol.MappingType.PARAMETER_ID;
    }

    private byte getCommandId() {
        return MisfitProtocol.MappingType.COMMAND_ID;
    }

    public void buildRequest() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(0, MisfitProtocol.Operation.GET);
        byteBuffer.put(1, getParameterId());
        byteBuffer.put(2, getCommandId());

        mRequestData = byteBuffer.array();
    }

    @Override
    public BaseResponse getResponse() {
        return mResponse;
    }

    @Override
    protected void onReceived(String characteristicUUID, byte[] bytes) {
        Response response = new Response();
        response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, getParameterId());
        if (response.result == Constants.RESPONSE_SUCCESS) {
            if (bytes.length < 4) {
                response.result = Constants.RESPONSE_ERROR;
            } else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                response.mappingType = new MappingType(byteBuffer.get(3));
            }
        }
        mResponse = response;
        mIsCompleted = true;
    }

    @Override
    public JSONObject getResponseDescriptionJSON() {
        JSONObject json = super.getResponseDescriptionJSON();
        if (json == null) {
            json = new JSONObject();
        }

        try {
            if (mResponse != null) {
                json.put("mappingType", mResponse.mappingType.getValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
