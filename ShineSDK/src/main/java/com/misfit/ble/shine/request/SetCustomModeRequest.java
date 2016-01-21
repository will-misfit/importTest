package com.misfit.ble.shine.request;

import android.util.Log;

import com.misfit.ble.setting.flashlink.CustomModeEnum;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;
import com.misfit.ble.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Quoc-Hung Le on 9/14/15.
 */
public class SetCustomModeRequest extends Request{

	private static final String TAG = LogUtils.makeTag(SetCustomModeRequest.class);

	private CustomModeEnum.ActionType actionType;
	private CustomModeEnum.MemEventNumber eventNumber;
	private CustomModeEnum.AnimNumber animNumber;
	private CustomModeEnum.KeyCode keyCode;
	private boolean releaseEnable;

	@Override
	public String getRequestName() {
		return "setCustomMode";
	}

	@Override
	public int getTimeOut() {
		return 0;
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}

	public byte getParameterId() {
		return Constants.DEVICE_CONFIG_PARAMETER_ID_EVENT_MAPPING_CONFIGURATION;
	}

	public void buildRequest(CustomModeEnum.ActionType actionType, CustomModeEnum.MemEventNumber eventNumber, CustomModeEnum.AnimNumber animNumber, CustomModeEnum.KeyCode keyCode, boolean releaseEnable) {

		this.actionType = actionType;
		this.eventNumber = eventNumber;
		this.animNumber = animNumber;
		this.keyCode = keyCode;
		this.releaseEnable = releaseEnable;

		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = getParameterId();

		ByteBuffer byteBuffer = null;

		if (actionType == CustomModeEnum.ActionType.HID_KEYBOARD) {
			byteBuffer = ByteBuffer.allocate(7);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			byteBuffer.put(0, operation);
			byteBuffer.put(1, parameterId);
			byteBuffer.put(2, actionType.getId());
			byteBuffer.put(3, eventNumber.getId());
			byteBuffer.put(4, animNumber.getId());
			byteBuffer.put(5, Convertor.bytesFromInteger(keyCode.getId())[0]);
			byteBuffer.put(6, Convertor.byteFromBoolean(releaseEnable));
		} else if (actionType == CustomModeEnum.ActionType.HID_MEDIA) {
			byteBuffer = ByteBuffer.allocate(8);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

			byteBuffer.put(0, operation);
			byteBuffer.put(1, parameterId);
			byteBuffer.put(2, actionType.getId());
			byteBuffer.put(3, eventNumber.getId());
			byteBuffer.put(4, animNumber.getId());

			byte[] tmp = Convertor.bytesFromInteger(keyCode.getId());
			byteBuffer.put(5, tmp[0]);
			byteBuffer.put(6, tmp[1]);

			byteBuffer.put(7, Convertor.byteFromBoolean(releaseEnable));
		}

		mRequestData = byteBuffer.array();
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 5)
			return;

		CustomModeEnum.ActionType actionType = CustomModeEnum.ActionType.valueOf(params[0]);
		CustomModeEnum.MemEventNumber eventNumber = CustomModeEnum.MemEventNumber.valueOf(params[1]);
		CustomModeEnum.AnimNumber animNumber = CustomModeEnum.AnimNumber.valueOf(params[2]);
		CustomModeEnum.KeyCode keyCode = CustomModeEnum.KeyCode.valueOf(params[3]);
		boolean releaseEnable = Boolean.parseBoolean(params[4]);

		buildRequest(actionType, eventNumber, animNumber, keyCode, releaseEnable);
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("actionType", actionType.getId());
			json.put("eventNumber", eventNumber.getId());
			json.put("animNumber", animNumber.getId());
			json.put("keyCode", keyCode.getId());
			json.put("releaseEnable", releaseEnable);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "actionType, eventNumber, animNumber, keyCode, releaseEnable";
	}

	@Override
	public void onRequestSent(int status) {
		mIsCompleted = true;
	}
}
