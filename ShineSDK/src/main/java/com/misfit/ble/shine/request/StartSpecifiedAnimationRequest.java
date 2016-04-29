package com.misfit.ble.shine.request;

import com.misfit.ble.setting.pluto.SpecifiedAnimationSetting;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StartSpecifiedAnimationRequest extends Request {

    private SpecifiedAnimationSetting mSpecifiedAnimationSetting;

    @Override
    public String getRequestName() {
        return "StartSpecifiedAnimation";
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    public void buildRequest(SpecifiedAnimationSetting specifiedAnimationSetting) {
        mSpecifiedAnimationSetting = specifiedAnimationSetting;
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.put(Constants.DEVICE_CONFIG_OPERATION_SET);
        byteBuffer.put(Constants.LEDControl.PARAMETER_ID);
        byteBuffer.put(Constants.LEDControl.COMMAND_START_SPECIFIED_ANIMATION);
        byteBuffer.put(Convertor.unsignedByteFromShort(specifiedAnimationSetting.getSequence().getValue()));
        byteBuffer.put(specifiedAnimationSetting.getNumberOfRepeats());
        byteBuffer.putShort(specifiedAnimationSetting.getTimeBetweenRepeats());
        byteBuffer.put(specifiedAnimationSetting.getColor());

        mRequestData = byteBuffer.array();
    }

    @Override
    public JSONObject getRequestDescriptionJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sequence", mSpecifiedAnimationSetting.getSequence());
            jsonObject.put("repeats", mSpecifiedAnimationSetting.getNumberOfRepeats());
            jsonObject.put("timeBetweenRepeats", mSpecifiedAnimationSetting.getTimeBetweenRepeats());
            jsonObject.put("color", mSpecifiedAnimationSetting.getColor());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public void onRequestSent(int status) {
        mIsCompleted = true;
    }
}
