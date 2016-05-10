package com.misfit.ble.shine.request;

import com.misfit.ble.setting.pluto.PlutoSequence;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StartSpecifiedAnimationRequest extends Request {

    private PlutoSequence.LED mLed;
    private PlutoSequence.Color mColor;
    private byte mRepeats;
    private short mTimeBetweenRepeats;

    @Override
    public String getRequestName() {
        return "StartSpecifiedAnimation";
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    public void buildRequest(PlutoSequence.LED led, byte repeats, short timeBetweenRepeats, PlutoSequence.Color color) {
        mLed = led;
        mRepeats = repeats;
        mTimeBetweenRepeats = timeBetweenRepeats;
        mColor = color;
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.put(0, Constants.DEVICE_CONFIG_OPERATION_SET);
        byteBuffer.put(1, Constants.LEDControl.PARAMETER_ID);
        byteBuffer.put(2, Constants.LEDControl.COMMAND_START_SPECIFIED_ANIMATION);
        byteBuffer.put(3, Convertor.unsignedByteFromShort(mLed.getValue()));
        byteBuffer.put(4, mRepeats);
        byteBuffer.putShort(5, mTimeBetweenRepeats);
        byteBuffer.put(7, Convertor.unsignedByteFromShort(mColor.getValue()));

        mRequestData = byteBuffer.array();
    }

    @Override
    public JSONObject getRequestDescriptionJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sequence", mLed.getValue());
            jsonObject.put("repeats", mRepeats);
            jsonObject.put("timeBetweenRepeats", mTimeBetweenRepeats);
            jsonObject.put("color", mColor.getValue());
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
