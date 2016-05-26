package com.misfit.ble.shine.request;

import com.misfit.ble.setting.lapCounting.LapCountingLicenseStatus;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GetLapCountingStatusRequest extends Request {

    public static class Response extends BaseResponse {
        public LapCountingLicenseStatus licenseStatus;
        public byte trialCounter;
        public byte lapCountingMode;
        public short timeout;
    }

    private Response mResponse;

    @Override
    public String getRequestName() {
        return "GetLapCountingStatus";
    }

    public Response getResponse() {
        return mResponse;
    }
    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    public byte getOperationId() {
        return Constants.DEVICE_CONFIG_OPERATION_GET;
    }

    public byte getParameterId() {
        return Constants.LapCounting.PARAMETER_ID_LAP_COUNTING;
    }

    public byte getSettingId() {
        return Constants.LapCounting.SETTING_ID_LAP_COUNTING;
    }

    public byte getCommandId() {
        return Constants.LapCounting.COMMAND_ID_LAP_COUNTING;
    }

    public void buildRequest() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.put(0, getOperationId());
        byteBuffer.put(1, getParameterId());
        byteBuffer.put(2, getCommandId());
        byteBuffer.put(3, getSettingId());

        mRequestData = byteBuffer.array();
    }

    @Override
    public void handleResponse(String characteristicUUID, byte[] bytes) {
        Response response = new Response();
        response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, getParameterId());
        if (response.result == Constants.RESPONSE_SUCCESS) {
            if (bytes.length < 8) {
                response.result = Constants.RESPONSE_ERROR;
            } else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

                response.licenseStatus = new LapCountingLicenseStatus(byteBuffer.get(4));
                response.trialCounter = byteBuffer.get(5);
                response.lapCountingMode = byteBuffer.get(6);
                response.timeout = Convertor.unsignedByteToShort(byteBuffer.get(7));
            }
        }
        mResponse = response;
        mIsCompleted = true;
    }

    @Override
    public JSONObject getResponseDescriptionJSON() {
        JSONObject json = new JSONObject();
        try {
            if (mResponse != null) {
                json.put("licenseStatus", mResponse.licenseStatus);
                json.put("trialCounter", mResponse.trialCounter);
                json.put("lapCountingMode", mResponse.lapCountingMode);
                json.put("timeout", mResponse.timeout);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
