package com.misfit.ble.shine.request;

import com.misfit.ble.setting.pluto.PlutoSequence;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StartSpecifiedVibrationRequest extends Request {

    private PlutoSequence.Vibe mSequence;
    private byte mRepeats;
    private short mTimeBetweenRepeats;

    @Override
    public String getRequestName() {
        return "StartSpecifiedVibration";
    }

    @Override
    public String getCharacteristicUUID() {
        return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
    }

    public void buildRequest(PlutoSequence.Vibe sequence, byte repeats, short timeBetweenRepeats) {
        mSequence = sequence;
        mRepeats = repeats;
        mTimeBetweenRepeats = timeBetweenRepeats;
        ByteBuffer byteBuffer = ByteBuffer.allocate(7);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        byteBuffer.put(Constants.DEVICE_CONFIG_OPERATION_SET);
        byteBuffer.put(Constants.VibeControl.PARAMETER_ID);
        byteBuffer.put(Constants.VibeControl.COMMAND_PLAY_SPECIFIED_VIBRATION);
        byteBuffer.put(Convertor.unsignedByteFromShort(mSequence.getValue()));
        byteBuffer.put(mRepeats);
        byteBuffer.putShort(mTimeBetweenRepeats);

        mRequestData = byteBuffer.array();
    }

    @Override
    public JSONObject getRequestDescriptionJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sequence", mSequence.getValue());
            jsonObject.put("repeats", mRepeats);
            jsonObject.put("timeBetweenRepeats", mTimeBetweenRepeats);
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
