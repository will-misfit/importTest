package com.misfit.ble.shine.request;

import android.util.Log;

import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.result.UserInputEvent;
import com.misfit.ble.util.Convertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class FileStreamingUserInputEventsRequest extends Request {
	private static final String TAG = FileStreamingUserInputEventsRequest.class.getName();

	public interface EventListener {
		void onStreamingStarted();
		void onButtonEventReceived(int eventID);
		void onHeartbeatReceived();
	}

	public static class Response extends BaseResponse {
		public byte status;
		public short handle;
	}
	private Response mResponse;

	@Override
	public Response getResponse() {
		return mResponse;
	}

	private EventListener mEventListener;

	private short mHandle;
	private int mOffset;
	private int mLength;

//	private ArrayList<byte[]> mResponseRawData;
	private byte mSequenceNumber;
	private short mUserInputEventSequenceNumber;

	private String mDeviceAddress;
	private static HashMap<String, Short> mUserInputEventSequenceNumberManager = new HashMap<>();

	public FileStreamingUserInputEventsRequest(String deviceAddress, EventListener listener) {
		mEventListener = listener;
//		mResponseRawData = new ArrayList<byte[]>();
		mSequenceNumber = -1;
		mUserInputEventSequenceNumber = -1;

		mDeviceAddress = deviceAddress;
		if(!mUserInputEventSequenceNumberManager.containsKey(deviceAddress)) {
			mUserInputEventSequenceNumberManager.put(deviceAddress, (short)-1);
		}
	}

	public void cancelRequest() {
		mIsCompleted = true;
	}

	@Override
	public String getRequestName() {
		return "fileStreamingUserInputEvents";
	}

	@Override
	public String getCharacteristicUUID() {
		return Constants.MFSERVICE_FILE_TRANSFER_CONTROL_CHARACTERISTIC_UUID;
	}

	@Override
	public int getTimeOut() {
		return 0;
	}

	public void parseFile(byte[] bytes) {
		// Do nothing
	}

	public void buildRequest(short fileHandle) {
		buildRequest(fileHandle, 0, Constants.FILE_LENGTH_STREAMING_EVENTS);
	}

	private void buildRequest(short fileHandle, int offset, int length) {
		mHandle = fileHandle;
		mOffset = offset;
		mLength = length;

		byte operation = Constants.FILE_CONTROL_OPERATION_GET;

		ByteBuffer byteBuffer = ByteBuffer.allocate(11);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

		byteBuffer.put(0, operation);
		byteBuffer.putShort(1, fileHandle);
		byteBuffer.putInt(3, offset);
		byteBuffer.putInt(7, length);

		mRequestData = byteBuffer.array();
	}

	@Override
	public void buildRequestWithParams(String paramsString) {
		if (paramsString == null || paramsString.length() <= 0)
			return;

		String[] params = paramsString.split(",");
		if (params.length != 3)
			return;

		if (params[0].startsWith("0x"))
			params[0] = params[0].substring(2);

		short handle = Short.parseShort(params[0], 16);
		int offset = Convertor.unsignedIntFromLong(Long.parseLong(params[1]));
		int length = Convertor.unsignedIntFromLong(Long.parseLong(params[2]));
		buildRequest(handle, offset, length);
	}

	@Override
	public void handleResponse(String characteristicUUID, byte[] bytes) {
		if (mIsCompleted == true) {
			Log.w(TAG, "Skip response: " + bytes);
			return;
		}

		if (mResponse == null) {
			mResponse = parseResponse(bytes);
			if (mResponse.result != Constants.RESPONSE_SUCCESS) {
				mIsCompleted = true;
			} else {
				mEventListener.onStreamingStarted();
			}
			return;
		}

		if (bytes == null || bytes.length < 2) {
			Log.w(TAG, "Invalid packet.");
			return;
		}
		Log.i(TAG, "data: " + Convertor.bytesToString(bytes));

		byte sequenceNumber = (byte) (bytes[0] & Constants.FILE_TRANSFER_SEQUENCE_MASK);
		if (sequenceNumber == mSequenceNumber) {
			Log.d(TAG, "old sequence number: " + sequenceNumber + ", skip.");
			return;
		}

		byte expectedSequenceNumber = (byte) ((mSequenceNumber + 1) & Constants.FILE_TRANSFER_SEQUENCE_MASK);
		if (mSequenceNumber != -1 && sequenceNumber != expectedSequenceNumber) {
			Log.w(TAG, "WRONG sequence number: " + sequenceNumber + " - expected: " + expectedSequenceNumber);
//			return;
		}
		mSequenceNumber = sequenceNumber;

		byte packetType = bytes[1];
		if (packetType == Constants.STREAMING_PACKET_TYPE_CONNECTION_HEARTBEAT_LEGACY ||
				packetType == Constants.STREAMING_PACKET_TYPE_CONNECTION_HEARTBEAT) {
			Log.i(TAG, "heartbeat...");
			mEventListener.onHeartbeatReceived();
			return;
		} else if (packetType == Constants.STREAMING_PACKET_TYPE_USER_INPUT_EVENTS) {
			if (!((Constants.HEARTBEAT_WITH_APPID_PACKET_LENGTH == bytes.length && Constants.FILE_HANDLE_STREAMING_EVENTS_WITH_APP_ID == mHandle)
					|| ((Constants.HEARTBEAT_PACKET_LENGTH == bytes.length) && (Constants.FILE_HANDLE_STREAMING_EVENTS_LEGACY == mHandle || Constants.FILE_HANDLE_STREAMING_EVENTS == mHandle)))) {
				Log.w(TAG, "Invalid event packet.");
				return;
			}

			short userInputEventSequenceNumber = Convertor.unsignedByteToShort(bytes[2]);
			if (userInputEventSequenceNumber == mUserInputEventSequenceNumberManager.get(mDeviceAddress)) {
				Log.d(TAG, "old event sequence number: " + sequenceNumber + ", skip.");
				return;
			}

			short expectedUserInputEventSequenceNumber = (short) ((mUserInputEventSequenceNumber + 1) & Constants.STREAMING_USER_INPUT_EVENTS_SEQUENCE_MASK);
			if (mUserInputEventSequenceNumber != -1 && userInputEventSequenceNumber != expectedUserInputEventSequenceNumber) {
				Log.w(TAG, "WRONG event sequence number: " + userInputEventSequenceNumber + " - expected: " + expectedUserInputEventSequenceNumber);
//				return;
			}
			mUserInputEventSequenceNumberManager.put(mDeviceAddress, userInputEventSequenceNumber);

			int eventType = Convertor.unsignedByteToShort(bytes[3]);
			if (UserInputEvent.validate(eventType) == false) {
				Log.w(TAG, "INVALID event type: " + eventType);
				return;
			}

			Log.d(TAG, "Received event: " + eventType);
			if (mEventListener != null) {
				mEventListener.onButtonEventReceived(eventType);
			}
		}

//		mResponseRawData.add(bytes);
	}

	protected Response parseResponse(byte[] bytes) {
		Response response = new Response();
		response.result = validateResponse(bytes, Constants.FILE_CONTROL_OPERATION_GET_RESPONSE);
		response.status = 0;

		if (response.result == Constants.RESPONSE_SUCCESS) {
			if (bytes.length < 4) {
				response.result = Constants.RESPONSE_ERROR;
			} else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				response.handle = byteBuffer.getShort(1);
				response.status = byteBuffer.get(3);
				if (response.handle != mHandle
						|| (response.status != Constants.FILE_CONTROL_RESPONSE_SUCCESS
							&& response.status != Constants.FILE_CONTROL_RESPONSE_OPERATION_IN_PROGRESS)) {
					response.result = Constants.RESPONSE_ERROR;
				}
			}
		}
		return response;
	}

	@Override
	public JSONObject getRequestDescriptionJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("handle", Convertor.unsignedShortToInteger(mHandle));
			json.put("offset", mOffset);
			json.put("length", mLength);
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
				json.put("handle", mResponse.handle);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public String getParamsHint() {
		return "handle:0x100,\noffset:0,\nlength:65535";
	}

}
