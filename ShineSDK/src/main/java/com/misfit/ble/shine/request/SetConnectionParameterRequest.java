package com.misfit.ble.shine.request;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetConnectionParameterRequest extends Request {
	
	public static class Response extends BaseResponse {
		public byte status;
		public double connectionInterval;
		public int connectionLatency;
		public int supervisionTimeout;
	}
	private Response mResponse;
	
	private double mMinConnectionInterval;
	private double mMaxConnectionInterval;
	private int mConnectionLatency;
	private int mSupervisionTimeout;

	@Override
	public Response getResponse() {
		return mResponse;
	}
	
	public double getMinConnectionInterval() {
		return mMinConnectionInterval;
	}
	
	public double getMaxConnectionInterval() {
		return mMaxConnectionInterval;
	}

	@Override
	public String getRequestName() {
		return "setConnectionParameter";
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_DEVICE_CONFIGURATION_CHARACTERISTIC_UUID;
	}

	@Override
	public int getTimeOut() {
		return 10000;
	}

	public void buildRequest(double minConnectionInterval, double maxConnectionInterval, int connectionLatency, int supervisionTimeout) {
		mMinConnectionInterval = minConnectionInterval;
		mMaxConnectionInterval = maxConnectionInterval;
		mConnectionLatency = connectionLatency;
		mSupervisionTimeout = supervisionTimeout;
		
		byte operation = Constants.DEVICE_CONFIG_OPERATION_SET;
		byte parameterId = Constants.DEVICE_CONFIG_PARAMETER_ID_CONNECTION_PARAMETER_SET;

		ByteBuffer byteBuffer = ByteBuffer.allocate(10);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, operation);
		byteBuffer.put(1, parameterId);
		byteBuffer.putShort(2, Convertor.unsignedShortFromInteger((int) Math.ceil(mMinConnectionInterval / Constants.CONNECTION_INTERVAL_UNIT)));
		byteBuffer.putShort(4, Convertor.unsignedShortFromInteger((int) Math.floor(mMaxConnectionInterval / Constants.CONNECTION_INTERVAL_UNIT)));
		byteBuffer.putShort(6, Convertor.unsignedShortFromInteger(mConnectionLatency));
		byteBuffer.putShort(8, Convertor.unsignedShortFromInteger((int) Math.ceil(mSupervisionTimeout * 1.0 / Constants.SUPERVISION_TIMEOUT_UNIT)));

		mRequestData = byteBuffer.array();
	}

	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.DEVICE_CONFIG_OPERATION_RESPONSE, Constants.DEVICE_CONFIG_PARAMETER_ID_CONNECTION_PARAMETER_GET);

		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 9) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				response.status = byteBuffer.get(2);
				response.connectionInterval = byteBuffer.getShort(3) * Constants.CONNECTION_INTERVAL_UNIT;
				response.connectionLatency = byteBuffer.getShort(5);
				response.supervisionTimeout = (byteBuffer.getShort(7) * Constants.SUPERVISION_TIMEOUT_UNIT);
				
				if (response.status != Constants.CONNECTION_PARAMETER_RESPONSE_SUCCESS) {
					response.result = Constants.RESPONSE_ERROR;
				}
			}
		}
		mResponse = response;
		mIsCompleted = true;
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		String[] params = paramsString.split(",");
		if (params.length != 4)
			return;
		
		double minConnectionInterval = Double.parseDouble(params[0]);
		double maxConnectionInterval = Double.parseDouble(params[1]);
		int connectionLatency = Integer.parseInt(params[2]);
		int supervisionTimeout = Short.parseShort(params[3]);
		buildRequest(minConnectionInterval, maxConnectionInterval, connectionLatency, supervisionTimeout);
	}

	@Override
	public String getParamsHint() {
		return "min:10.0,\nmax:20.0,\nlatency:0,\ntimeout:720";
	}
	
	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("minConnectionInterval", mMinConnectionInterval);
			json.put("maxConnectionInterval", mMaxConnectionInterval);
			json.put("connectionLatency", mConnectionLatency);
			json.put("supervisionTimeout", mSupervisionTimeout);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public JSONObject getResponseDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			if (mResponse != null) {
				json.put("result", mResponse.result);
				json.put("status", mResponse.status);
				json.put("connectionInterval", mResponse.connectionInterval);
				json.put("connectionLatency", mResponse.connectionLatency);
				json.put("supervisionTimeout", mResponse.supervisionTimeout);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
}
