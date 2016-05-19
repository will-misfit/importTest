package com.misfit.ble.shine.request;

import com.misfit.ble.setting.speedo.ActivityType;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.core.MisfitProtocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GetActivityTypeRequest extends GetRequest {

    private Response mResponse;

    public static class Response extends BaseResponse{
        public ActivityType activityType;
    }

    @Override
    public String getRequestName() {
        return "GetActivityType";
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    public void buildRequest() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(0, MisfitProtocol.Operation.GET);
        byteBuffer.put(1, MisfitProtocol.Algorithm.PARAMETER_ID);
        byteBuffer.put(2, MisfitProtocol.Algorithm.COMMAND_ACTIVIT_TYPE);

        mRequestData = byteBuffer.array();
    }

    @Override
    public BaseResponse getResponse() {
        return mResponse;
    }

    @Override
    protected void onReceived(String characteristicUUID, byte[] bytes) {
        Response response = new Response();
        response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, MisfitProtocol.Algorithm.PARAMETER_ID);
        if (response.result == Constants.RESPONSE_SUCCESS) {
            if (bytes.length < 4) {
                response.result = Constants.RESPONSE_ERROR;
            } else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                response.activityType = new ActivityType(byteBuffer.get(3));
            }
        }
        mResponse = response;
        mIsCompleted = true;
    }
}
