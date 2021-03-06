package com.misfit.ble.shine.request;

import com.misfit.ble.setting.pluto.PlutoSequence;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 8/19/15.
 */
public class PlayVibrationRequest extends Request {

	private PlutoSequence.Vibe mVibrationSequence;
	private short mNumOfRepeats;
	private int mMillisBetweenRepeats;

	@Override
	public String getRequestName() {
		return "playVibration";
	}

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
		return Constants.DEVICE_CONFIG_PARAMETER_ID_DIAGNOSTIC_FUNCTIONS;
	}

	public byte getSettingId() {
		return Constants.DEVICE_CONFIG_DIAGNOSTIC_FUNCTIONS_PLAY_VIBRATION;
	}

	public void buildRequest(PlutoSequence.Vibe vibrationSequence, short numOfRepeats, int millisBetweenRepeats) {
		mVibrationSequence = vibrationSequence;
		mNumOfRepeats = numOfRepeats;
		mMillisBetweenRepeats = millisBetweenRepeats;

		ByteBuffer byteBuffer = ByteBuffer.allocate(7);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, getOperation());
		byteBuffer.put(1, getParameterId());
		byteBuffer.put(2, getSettingId());
		byteBuffer.put(3, Convertor.unsignedByteFromShort(mVibrationSequence.getValue()));
		byteBuffer.put(4, Convertor.unsignedByteFromShort(mNumOfRepeats));

		byte[] tmp = Convertor.bytesFromInteger(mMillisBetweenRepeats);
		byteBuffer.put(5, tmp[0]);
		byteBuffer.put(6, tmp[1]);

		mRequestData = byteBuffer.array();


	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("vibrationSequence", mVibrationSequence.getValue());
			json.put("numOfRepeats", mNumOfRepeats);
			json.put("millisecondsBetweenRepeats", mMillisBetweenRepeats);
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
