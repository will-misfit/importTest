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
public class GetExtraAdvDataStateRequest extends Request{

    private static final String TAG = "GetExtraAdvDataState";

    public static class Response extends BaseResponse{
        public boolean advDataState;
    }

    private Response mResponse;

	@Override
    public Response getResponse() {
        return mResponse;
    }

    @Override
    public String getRequestName() {
        return "getAdvDataState";
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    public byte getParameterId() {
        return Constants.DEVICE_CONFIG_PARAMETER_ID_DEVICE_SETTINGS_IN_OUT;
    }

    public byte getOperationId() {
        return Constants.DEVICE_CONFIG_OPERATION_GET;
    }

    public byte getSettingId() {
        return Constants.DEVICE_SETTING_ID_EXTRA_ADVERTISING_DATA_STATE;
    }

    public void buildRequest() {
        byte operation = getOperationId();
        byte parameterId = getParameterId();
        byte settingsId = getSettingId();

        ByteBuffer byteBuffer = ByteBuffer.allocate(3);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.put(0, operation);
        byteBuffer.put(1, parameterId);
        byteBuffer.put(2, settingsId);

        mRequestData = byteBuffer.array();
    }

    @Override
    public void handleResponse(String characteristicUUID, byte[] bytes) {
        Response response = new Response();
        response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, getParameterId());

        if (response.result == Constants.RESPONSE_SUCCESS) {
            if (bytes.length < 3) {
                response.result = Constants.RESPONSE_ERROR;
            } else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                response.advDataState = Convertor.byteToBoolean(byteBuffer.get(2));
            }
        }
        mResponse = response;
        mIsCompleted = true;
    }

    @Override
    public void buildRequestWithParams(String paramsString) {
        buildRequest();
    }

    @Override
    public JSONObject getResponseDescriptionJSON() {
        JSONObject json = new JSONObject();
        try {
            if (mResponse != null) {
                json.put("result", mResponse.result);
                json.put("advDataState", mResponse.advDataState);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
