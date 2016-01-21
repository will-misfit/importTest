package com.misfit.ble.shine.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfileCore;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.request.FileAbortRequest;
import com.misfit.ble.shine.request.GetConnectionParameterRequest;
import com.misfit.ble.shine.request.OTAEnterRequest;
import com.misfit.ble.shine.request.OTAEraseSegment;
import com.misfit.ble.shine.request.OTAGetSizeWritten;
import com.misfit.ble.shine.request.OTAPutRequest;
import com.misfit.ble.shine.request.OTAResetRequest;
import com.misfit.ble.shine.request.OTAVerifyFile;
import com.misfit.ble.shine.request.OTAVerifySegment;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.util.Convertor;
import com.misfit.ble.util.LogUtils;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OTAPhaseController extends PhaseController {
	private static final String TAG = LogUtils.makeTag(OTAPhaseController.class);

	public interface OTAPhaseControllerCallback {
		void onOTAPhaseControllerTransferData(OTAPhaseController otaPhaseController, byte[] data, float interpacketDelay);
		void onOTAPhaseControllerStopTransferData(OTAPhaseController otaPhaseController);
		void onOTAPhaseControllerProgressChanged(OTAPhaseController otaPhaseController, float progress, ShineProfile.OTACallback otaCallback);
	}

    public OTAPhaseController(PhaseControllerCallback callback, ShineProfile.OTACallback otaCallback, OTAPhaseControllerCallback otaPhaseControllerCallback, byte[] firmwareData) {
        super(ActionID.OTA,
				LogEventItem.EVENT_OTA,
				callback);

        mFirmwareData = firmwareData;
		mOtaCallback = otaCallback;
        mOTAPhaseControllerCallback = otaPhaseControllerCallback;
    }

	private ShineProfile.OTACallback mOtaCallback;
    private OTAPhaseControllerCallback mOTAPhaseControllerCallback;
	private byte[] mFirmwareData;

	// Recover from error
	private long mNumberOfBytesWritten;
	private long mNumberOfBytesVerifying;
	private long mNumberOfBytesVerified;
	private long mNumberOfBytesTransferred;
	
	// Retry in case data transfer fails
	private static final int DATA_TRANSFER_MAX_RETRIES = 2;
	private static final int DATA_TRANSFER_INCOMPLETE_MAX_RETRIES = 10;
	private ScheduledExecutorService mDataTransferRetryExecutor;
	private int mNumberOfDataTransferRetries;
	private int mNumberOfOTAPutRetries = 0;
	
	// Data transferring
	private float mPreviousDataTransferringProgress = 0;
	private static final float mDataTransferringProgressThreshold = 0.001f;
	private float mConnectionInterval = Constants.DEFAULT_CONNECTION_INTERVAL;
	
	@Override
	public void start() {
		super.start();
		sendRequest(buildRequest(OTAEnterRequest.class));
	}
	
	@Override
	public void stop() {
		onPhaseControllerFinish(RESULT_INTERRUPTED);
	}
	
	public void onDataTransferResult(int result, int blockSize, int transferredSize) {
		if (result != ShineProfileCore.RESULT_SUCCESS) {
			onPhaseControllerFinish(RESULT_DATA_TRANSFER_FAILED);
			return;
		}
		
		long totalTransferringSize = (mFirmwareData.length - mNumberOfBytesVerified);
		if (totalTransferringSize > 0) {
			long totalTransferredSize = transferredSize + mNumberOfBytesTransferred;
			float progress = (totalTransferredSize + mNumberOfBytesVerified) * 1.0f / mFirmwareData.length;

			if (Math.abs(mPreviousDataTransferringProgress - progress) > mDataTransferringProgressThreshold
					|| totalTransferredSize >= totalTransferringSize) {
				mPreviousDataTransferringProgress = progress;
				mOTAPhaseControllerCallback.onOTAPhaseControllerProgressChanged(this, progress, mOtaCallback);
			}
		}
		
		if (transferredSize >= blockSize) {
			if (transferredSize > blockSize) {
				startEOFTimeoutTimer(mCurrentRequest);
			}
			mOTAPhaseControllerCallback.onOTAPhaseControllerStopTransferData(this);
		}
	}
	
	private void retryTransferData() {
		if (mDataTransferRetryExecutor == null) {
			mDataTransferRetryExecutor = Executors.newSingleThreadScheduledExecutor();
		}
		
		mDataTransferRetryExecutor.schedule(new Runnable() {
		    @Override
		    public void run() {
		    	sendRequest(buildRequest(OTAGetSizeWritten.class));
		    }
		}, 1000, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void onRequestSentResult(Request request, int result) {
		if (request == null || request.equals(mCurrentRequest) == false)
			return;
		super.onRequestSentResult(request, result);
		
		if (result == ShineProfileCore.RESULT_FAILURE) {
			onPhaseControllerFinish(RESULT_SENDING_REQUEST_FAILED);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			onPhaseControllerFinish(RESULT_TIMED_OUT);
			return;
		} else if (result == ShineProfileCore.RESULT_UNSUPPORTED) {
			onPhaseControllerFinish(RESULT_UNSUPPORTED);
			return;
		}
		
		if (request instanceof OTAResetRequest) {
			onPhaseControllerFinish(RESULT_SUCCESS);
		}
	}
	
	@Override
	public void onResponseReceivedResult(Request request, int result) {
		if (request == null || request.equals(mCurrentRequest) == false)
			return;
		super.onResponseReceivedResult(request, result);
		
		if (result == ShineProfileCore.RESULT_FAILURE) {
			onPhaseControllerFinish(RESULT_RECEIVE_RESPONSE_FAILED);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			onPhaseControllerFinish(RESULT_TIMED_OUT);
			return;
		}
		
		if (request instanceof GetConnectionParameterRequest) {
			GetConnectionParameterRequest getConnectionParameterRequest = (GetConnectionParameterRequest)request;
			GetConnectionParameterRequest.Response response = getConnectionParameterRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				// This is just an optional request so skip this error and continue with next request.
			} else {
				mConnectionInterval = (float) response.connectionInterval;
			}
			
			sendRequest(buildRequest(OTAEnterRequest.class));
			
		} else if (request instanceof OTAEnterRequest) {
			OTAEnterRequest otaEnterRequest = (OTAEnterRequest)request;
			OTAEnterRequest.Response response = otaEnterRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				onPhaseControllerFinish(RESULT_REQUEST_ERROR);
				return;
			}
			
			sendRequest(buildRequest(OTAGetSizeWritten.class));
			
		} else if (request instanceof OTAGetSizeWritten) {
			OTAGetSizeWritten otaGetSizeWrittenRequest = (OTAGetSizeWritten)request;
			OTAGetSizeWritten.Response response = otaGetSizeWrittenRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				onPhaseControllerFinish(RESULT_REQUEST_ERROR);
				return;
			}
			
			Class<? extends Request> nextRequest;
			mNumberOfBytesVerifying = 0;
			mNumberOfBytesVerified = 0;
			mNumberOfBytesTransferred = 0;
			mNumberOfBytesWritten = response.sizeWritten;
			
			if (mNumberOfBytesWritten > mFirmwareData.length) {
				nextRequest = OTAPutRequest.class;
			} else if (mNumberOfBytesWritten <= 0) {
				nextRequest = OTAPutRequest.class;
			} else if (mNumberOfBytesWritten == mFirmwareData.length) {
				nextRequest = OTAVerifyFile.class;
			} else {
				mNumberOfBytesVerifying = mNumberOfBytesWritten;
				nextRequest = OTAVerifySegment.class;
			}
			
			sendRequest(buildRequest(nextRequest));
			
		} else if (request instanceof OTAVerifySegment) {
			OTAVerifySegment otaVerifySegmentRequest = (OTAVerifySegment)request;
			OTAVerifySegment.Response response = otaVerifySegmentRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS && response.result != Constants.RESPONSE_WRONG_CRC) {
				onPhaseControllerFinish(RESULT_REQUEST_ERROR);
				return;
			}
			
			if (response.result == Constants.RESPONSE_WRONG_CRC) {
				mNumberOfBytesVerifying = Math.max(0, mNumberOfBytesVerifying - Constants.OTA_SEGMENT_SIZE);
				
				if (mNumberOfBytesVerifying <= 0) {
					mNumberOfBytesVerified = 0;
					sendRequest(buildRequest(OTAPutRequest.class));
				} else {
					sendRequest(buildRequest(OTAVerifySegment.class));
				}
			} else {
				mNumberOfBytesVerified = mNumberOfBytesVerifying;
				
				if (mNumberOfBytesWritten == mNumberOfBytesVerified) {
					sendRequest(buildRequest(OTAPutRequest.class));
				} else {
					sendRequest(buildRequest(OTAEraseSegment.class));
				}
			}
			
		} else if (request instanceof OTAEraseSegment) {
			OTAEraseSegment eraseSegmentRequest = (OTAEraseSegment)request;
			OTAEraseSegment.Response response = eraseSegmentRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				onPhaseControllerFinish(RESULT_REQUEST_ERROR);
				return;
			}
			
			mNumberOfBytesWritten = response.newSizeWritten;
			
			if (mNumberOfBytesVerified >= mNumberOfBytesWritten) {
				mNumberOfBytesVerified = mNumberOfBytesWritten;
				sendRequest(buildRequest(OTAPutRequest.class));
			} else {
				sendRequest(buildRequest(OTAEraseSegment.class));
			}
			
		} else if (request instanceof OTAPutRequest) {
			OTAPutRequest otaPutRequest = (OTAPutRequest)request;
			OTAPutRequest.Response response = otaPutRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				boolean didTransferDataTooFast = (response.status == Constants.FILE_CONTROL_RESPONSE_OPERATION_IN_PROGRESS);
				
				if (didTransferDataTooFast) {
					DataTransferSpeedController.onDataTransferFailedDueToTransferSpeed();
				}
				
				if (didTransferDataTooFast && mNumberOfDataTransferRetries < DATA_TRANSFER_MAX_RETRIES) {
					mNumberOfDataTransferRetries += 1;
					mOTAPhaseControllerCallback.onOTAPhaseControllerStopTransferData(this);
					retryTransferData();
				} else {
					mOTAPhaseControllerCallback.onOTAPhaseControllerStopTransferData(this);
					onPhaseControllerFinish(RESULT_REQUEST_ERROR);
				}
				return;
			}
			
			if (otaPutRequest.mHasReceivedEOF == false) {
				byte[] dataToTransfer = null;
				
				long offset = otaPutRequest.getDataOffset();
				long length = otaPutRequest.getDataLength();
				if (offset == 0 && length == mFirmwareData.length) {
					dataToTransfer = mFirmwareData;
				} else {
					dataToTransfer = Arrays.copyOfRange(mFirmwareData, Convertor.unsignedIntFromLong(offset), Math.min(mFirmwareData.length, Convertor.unsignedIntFromLong(offset + length))); 
				}
				
				mOTAPhaseControllerCallback.onOTAPhaseControllerTransferData(this, dataToTransfer, DataTransferSpeedController.getInterpacketDelay(mConnectionInterval));
				
			} else {
				DataTransferSpeedController.onDataTransferSucceeded();
				
				mNumberOfBytesTransferred += otaPutRequest.getDataLength();
				
				if (mNumberOfBytesVerified + mNumberOfBytesTransferred >= mFirmwareData.length) {
					mNumberOfBytesTransferred = 0;
					sendRequest(buildRequest(OTAVerifyFile.class));
				} else {
					sendRequest(buildRequest(OTAPutRequest.class));
				}
			}
			
		} else if (request instanceof OTAVerifyFile) {
			OTAVerifyFile otaVerifyRequest = (OTAVerifyFile)request;
			OTAVerifyFile.Response response = otaVerifyRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS && response.status != Constants.FILE_CONTROL_RESPONSE_VERIFICATION_FAILURE) {
				onPhaseControllerFinish(RESULT_REQUEST_ERROR);
				return;
			}
			
			if (response.status == Constants.FILE_CONTROL_RESPONSE_VERIFICATION_FAILURE) {
				mNumberOfBytesWritten = mNumberOfBytesVerifying = mFirmwareData.length;
				sendRequest(buildRequest(OTAVerifySegment.class));
			} else {
				sendRequest(buildRequest(OTAResetRequest.class));
			}
		}
	}
	
	private Request buildRequest(Class<? extends Request> requestType) {
		Request request = null;
		
		if (requestType.equals(GetConnectionParameterRequest.class)) {
			GetConnectionParameterRequest getConnectionParametersRequest = new GetConnectionParameterRequest();
			getConnectionParametersRequest.buildRequest();
			request = getConnectionParametersRequest;
		}
		else if (requestType.equals(OTAEnterRequest.class)) 
		{
			OTAEnterRequest otaEnterRequest = new OTAEnterRequest();
			otaEnterRequest.buildRequest();
			request = otaEnterRequest;	
		} 
		else if (requestType.equals(OTAPutRequest.class)) 
		{	
			// Sending firmware data page by page.
			long offset = mNumberOfBytesVerified + mNumberOfBytesTransferred;
			long length = Math.min(Constants.OTA_SEGMENT_SIZE, mFirmwareData.length - offset);
			
			OTAPutRequest otaPutRequest = new OTAPutRequest();
			otaPutRequest.buildRequest(offset, length, mFirmwareData.length);
			request = otaPutRequest;	
		} 
		else if (requestType.equals(OTAVerifyFile.class)) 
		{
			OTAVerifyFile otaVerifyRequest = new OTAVerifyFile();
			otaVerifyRequest.buildRequest();
			request = otaVerifyRequest;	
		} 
		else if (requestType.equals(OTAResetRequest.class)) 
		{
			OTAResetRequest otaResetRequest = new OTAResetRequest();
			otaResetRequest.buildRequest();
			request = otaResetRequest;
		} 
		else if (requestType.equals(OTAGetSizeWritten.class)) 
		{
			OTAGetSizeWritten otaGetSizeWrittenRequest = new OTAGetSizeWritten();
			otaGetSizeWrittenRequest.buildRequest();
			request = otaGetSizeWrittenRequest;	
		} 
		else if (requestType.equals(OTAEraseSegment.class)) 
		{
			long offset = Math.max(0, mNumberOfBytesWritten - Constants.OTA_ERASE_END_OFFSET);
			
			OTAEraseSegment otaEraseSegmentRequest = new OTAEraseSegment();
			otaEraseSegmentRequest.buildRequest(offset);
			request = otaEraseSegmentRequest;
		} 
		else if (requestType.equals(OTAVerifySegment.class)) 
		{	
			OTAVerifySegment otaVerifySegmentRequest = new OTAVerifySegment();
			otaVerifySegmentRequest.buildRequest(mFirmwareData, 0, mNumberOfBytesVerifying, mFirmwareData.length);
			request = otaVerifySegmentRequest;
		}
		else if (requestType.equals(FileAbortRequest.class)) {
			FileAbortRequest abortRequest = new FileAbortRequest();
			abortRequest.buildRequest(Constants.FILE_HANDLE_OTA);
			request = abortRequest;
		}
		return request;
	}
	
	@Override
	public String getPhaseName() {
		return "OTAPhase";
	}

	private void onPhaseControllerFinish(int resultCode) {
		if (mCurrentRequest instanceof OTAPutRequest) {
			mOTAPhaseControllerCallback.onOTAPhaseControllerStopTransferData(this);
		}

		if (resultCode != RESULT_SUCCESS) {
			sendRequest(buildRequest(FileAbortRequest.class));
			if (resultCode == RESULT_DATA_TRANSFER_FAILED && mNumberOfOTAPutRetries < DATA_TRANSFER_INCOMPLETE_MAX_RETRIES) {
				mNumberOfOTAPutRetries += 1;
				retryTransferData();
				return;
			}
		}

		mResult = resultCode;
		switch(mResult) {
			case RESULT_SUCCESS:
				mPhaseControllerCallback.onPhaseControllerCompleted(this);
				mPhaseControllerCallback.onPhaseControllerDisconnect(this);
				mOtaCallback.onOTACompleted(ShineProfile.ActionResult.SUCCEEDED);
				mNumberOfOTAPutRetries = 0;   // reset internal OTAPut retries
				break;
			case RESULT_UNKNOWN:
				Log.e(TAG, "onPhaseControllerFinish - UNKNOWN RESULT CODE");
			default:
				mPhaseControllerCallback.onPhaseControllerFailed(this);
				mOtaCallback.onOTACompleted(ShineProfile.ActionResult.FAILED);
				break;
		}
	}

	/**
	 * PUT EOF Timer
	 */
	private EOFTimeoutHandler mEOFTimeoutHandler;
	private class EOFTimeoutHandler implements Runnable {
		private boolean isValid;
		private Request request;

		public EOFTimeoutHandler(Request request) {
			this.request = request;
			this.isValid = true;
		}

		public void invalidate() {
			this.isValid = false;
		}

		@Override
		public void run() {
			if (!this.isValid)
				return;

			onEOFTimeoutTimerFired(request);
		}
	}

	private void startEOFTimeoutTimer(Request request) {
		if (mEOFTimeoutHandler != null) {
			mEOFTimeoutHandler.invalidate();
			mEOFTimeoutHandler = null;
		}

		mEOFTimeoutHandler = new EOFTimeoutHandler(request);
		new Handler(Looper.getMainLooper()).postDelayed(mEOFTimeoutHandler, 5000);
	}

	private void onEOFTimeoutTimerFired(Request request) {
		if (mCurrentRequest == null
				|| !mCurrentRequest.equals(request)
				|| !mCurrentRequest.isWaitingForResponse()
				|| mResult != RESULT_UNKNOWN) {
			return;
		}

		Log.w(TAG, "onEOFTimeoutTimerFired");

		if (mNumberOfDataTransferRetries < DATA_TRANSFER_MAX_RETRIES) {
			mNumberOfDataTransferRetries += 1;
			retryTransferData();
		} else {
			onPhaseControllerFinish(RESULT_TIMED_OUT);
		}
	}
}
