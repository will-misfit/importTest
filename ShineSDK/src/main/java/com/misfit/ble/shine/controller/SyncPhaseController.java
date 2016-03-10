package com.misfit.ble.shine.controller;

import android.os.Bundle;

import com.misfit.ble.shine.ActionID;
import com.misfit.ble.shine.ShineProfile;
import com.misfit.ble.shine.ShineProfileCore;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.log.LogEventItem;
import com.misfit.ble.shine.parser.ActivityDataParser;
import com.misfit.ble.shine.parser.ActivityDataParserFactory;
import com.misfit.ble.shine.parser.TimestampCorrector;
import com.misfit.ble.shine.parser.TimestampCorrectorNew;
import com.misfit.ble.shine.parser.swim.SwimSessionPostProcessor;
import com.misfit.ble.shine.request.FileEraseActivityRequest;
import com.misfit.ble.shine.request.FileEraseHardwareLogRequest;
import com.misfit.ble.shine.request.FileGetActivityRequest;
import com.misfit.ble.shine.request.FileGetHardwareLogRequest;
import com.misfit.ble.shine.request.FileListRequest;
import com.misfit.ble.shine.request.Request;
import com.misfit.ble.shine.result.SyncResult;
import com.misfit.ble.util.MutableBoolean;

import java.util.ArrayList;
import java.util.List;

public class SyncPhaseController extends PhaseController {

	public interface SyncPhaseControllerCallback {
		void onSyncPhaseControllerSyncDataReadProgress(
				SyncPhaseController syncPhaseController,
				Bundle extraInfo, 
				MutableBoolean shouldStop,
				ShineProfile.SyncCallback syncCallback);
		void onSyncPhaseControllerSyncDataReadCompleted(
				SyncPhaseController controller,
				List<SyncResult> syncResults,
				MutableBoolean shouldStop,
				ShineProfile.SyncCallback syncCallback);
		void onGetActivityDataFinished();
		void onHWLogRead(byte[] hwLog, ShineProfile.SyncCallback syncCallback);
	}
	
	public class SyncSession {
		public short mNumberOfActivityFiles;
		public short mNumberOfActivityFilesRead;
		public short mNumberOfActivityFilesErased;
		public long mTotalFilesSize;
		private List<SyncResult> syncResults = new ArrayList<>();
	}

	private TimestampCorrector mTimestampCorrector;
	private SwimSessionPostProcessor mSwimLapPostProcessor;
	private SyncSession mSyncSession;
	private SyncPhaseControllerCallback mSyncPhaseCallback;

	private ShineProfile.SyncCallback mSyncCallback;

    public SyncPhaseController(PhaseControllerCallback callback, ShineProfile.SyncCallback syncCallback, SyncPhaseControllerCallback syncPhaseCallback) {
        super(ActionID.SYNC,
				LogEventItem.EVENT_SYNC,
				callback);

		mSyncCallback = syncCallback;
        mSyncPhaseCallback = syncPhaseCallback;
    }

    public SyncSession getSession() {
		return mSyncSession;
	}

	@Override
	public void start() {
		super.start();
		
		mSyncSession = new SyncSession();
		mTimestampCorrector = new TimestampCorrector();
		mSwimLapPostProcessor = new SwimSessionPostProcessor();
		sendRequest(buildRequest(FileListRequest.class));
	}

	@Override
	public void stop() {
		super.stop();
		mSyncCallback.onSyncCompleted(ShineProfile.ActionResult.INTERRUPTED);
		mSyncSession.syncResults = new ArrayList<>();
	}
	
	@Override
	public void onRequestSentResult(Request request, int result) {
		if (request == null || request.equals(mCurrentRequest) == false)
			return;
		super.onRequestSentResult(request, result);
		
		if (result == ShineProfileCore.RESULT_FAILURE) {
			postProcessing(RESULT_SENDING_REQUEST_FAILED);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			postProcessing(RESULT_TIMED_OUT);
			return;
		} else if (result == ShineProfileCore.RESULT_UNSUPPORTED) {
			postProcessing(RESULT_FLOW_BROKEN);
			return;
		}
	}
	
	@Override
	public void onResponseReceivedResult(Request request, int result) {
		if (request == null || request.equals(mCurrentRequest) == false)
			return;
		super.onResponseReceivedResult(request, result);
		
		if (result == ShineProfileCore.RESULT_FAILURE) {
			postProcessing(RESULT_RECEIVE_RESPONSE_FAILED);
			return;
		} else if (result == ShineProfileCore.RESULT_TIMED_OUT) {
			postProcessing(RESULT_TIMED_OUT);
			return;
		}
		
		if (request instanceof FileListRequest) {
			FileListRequest fileListRequest = (FileListRequest)request;
			FileListRequest.Response response = fileListRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				postProcessing(RESULT_REQUEST_ERROR);
				return;
			}
			
			mSyncSession.mNumberOfActivityFiles = response.numberOfFiles;
			mSyncSession.mNumberOfActivityFilesRead = mSyncSession.mNumberOfActivityFilesErased = 0; 
			mSyncSession.mTotalFilesSize = response.totalFileSize;
			mSyncSession.syncResults = new ArrayList<>();

			if (mSyncSession.mNumberOfActivityFiles > 0) {
				sendRequest(buildRequest(FileGetActivityRequest.class));
			} else {
				if (notifyActivityReadCompletedAndShouldStop()){
					return;
				}
				sendRequest(buildRequest(FileGetHardwareLogRequest.class));
			}
			
		} else if (request instanceof FileGetActivityRequest) {
			FileGetActivityRequest fileGetRequest = (FileGetActivityRequest)request;
			FileGetActivityRequest.Response response = fileGetRequest.getResponse();
			
			// TODO: handle CRC_ERROR
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				postProcessing(RESULT_REQUEST_ERROR);
				return;
			}

			float syncProgress = 0.0f;
			short totalFiles = mSyncSession.mNumberOfActivityFiles;
			if (totalFiles != 0) {
				syncProgress = (mSyncSession.mNumberOfActivityFilesRead + 1) * 1.0f / totalFiles;
			}
			boolean isLastFile = (mSyncSession.mNumberOfActivityFilesRead >= mSyncSession.mNumberOfActivityFiles - 1);

			SyncResult syncResult = parseShineData(response.fileFormat, response.fileTimestamp, response.rawData, isLastFile);

			if (syncResult == null) {
				// FIXME: record the event, then discard the data.
				postProcessing(RESULT_PARSE_ERROR);
				return;
			} else {
				SyncResult processedSyncResult = correctTimestamp(syncResult, response.fileTimestamp, isLastFile);
				if (processedSyncResult != null) {
					mSyncSession.syncResults.add(processedSyncResult); // add sync result to list
				}

				Bundle bundle = new Bundle();
				bundle.putFloat(ShineProfile.SYNC_PROGRESS_KEY, syncProgress);

				MutableBoolean shouldStop = new MutableBoolean(false);
				mSyncPhaseCallback.onSyncPhaseControllerSyncDataReadProgress(this, bundle, shouldStop, mSyncCallback);
				
				if (shouldStop.getValue()) {
					postProcessing(RESULT_INTERRUPTED);
				} else {
					// send next request: either continue get activity file or erase activity file
					handleActivityFileReadCompleted();
				}
			}
		} else if (request instanceof FileEraseActivityRequest) {
			FileEraseActivityRequest fileEraseRequest = (FileEraseActivityRequest)request;
			FileEraseActivityRequest.Response response = fileEraseRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				postProcessing(RESULT_REQUEST_ERROR);
				return;
			}
			
	        handleActivityFileErasedCompleted();
			
		} else if (request instanceof FileGetHardwareLogRequest) {
			FileGetHardwareLogRequest fileGetHardwareLogRequest = (FileGetHardwareLogRequest)request;
			FileGetHardwareLogRequest.Response response = fileGetHardwareLogRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				postProcessing(RESULT_REQUEST_ERROR);
				return;
			}

			if(mSyncPhaseCallback!=null){
				mSyncPhaseCallback.onHWLogRead(response.data, mSyncCallback);
			}

			sendRequest(buildRequest(FileEraseHardwareLogRequest.class));
			
		} else if (request instanceof FileEraseHardwareLogRequest) {
			FileEraseHardwareLogRequest fileEraseHardwareLogRequest = (FileEraseHardwareLogRequest)request;
			FileEraseHardwareLogRequest.Response response = fileEraseHardwareLogRequest.getResponse();
			
			if (response.result != Constants.RESPONSE_SUCCESS) {
				postProcessing(RESULT_REQUEST_ERROR);
				return;
			}
			postProcessing(RESULT_SUCCESS);
		}
	}
	
	private Request buildRequest(Class<? extends Request> requestType) {
		Request request = null;
	    
		if (requestType.equals(FileListRequest.class)) {
			FileListRequest fileListRequest = new FileListRequest();
			fileListRequest.buildRequest();
			request = fileListRequest;
		} else if (requestType.equals(FileGetActivityRequest.class)) {
			FileGetActivityRequest fileGetRequest = new FileGetActivityRequest();
			fileGetRequest.buildRequest((short)(Constants.FILE_HANDLE_ACTIVITY_FILE + mSyncSession.mNumberOfActivityFilesRead + 1));
			request = fileGetRequest;
		} else if (requestType.equals(FileEraseActivityRequest.class)) {
			FileEraseActivityRequest fileEraseRequest = new FileEraseActivityRequest();
			fileEraseRequest.buildRequest();
			request = fileEraseRequest;
		} else if (requestType.equals(FileGetHardwareLogRequest.class)) {
			FileGetHardwareLogRequest fileGetHardwareLogRequest = new FileGetHardwareLogRequest();
			fileGetHardwareLogRequest.buildRequest();
			request = fileGetHardwareLogRequest;
		} else if (requestType.equals(FileEraseHardwareLogRequest.class)) {
			FileEraseHardwareLogRequest fileEraseHardwareLogRequest = new FileEraseHardwareLogRequest();
			fileEraseHardwareLogRequest.buildRequest();
			request = fileEraseHardwareLogRequest;
		}
		return request;
	}
	
	private void handleActivityFileReadCompleted() {
	    mSyncSession.mNumberOfActivityFilesRead += 1;
	    
	    if (mSyncSession.mNumberOfActivityFilesRead < mSyncSession.mNumberOfActivityFiles) {
	    	sendRequest(buildRequest(FileGetActivityRequest.class));
	    }
	    else {
			//invoke callback
			if (notifyActivityReadCompletedAndShouldStop()){
				return;
			}

			if (mSwimLapPostProcessor.hasCompleted()) {
				mSyncPhaseCallback.onGetActivityDataFinished(); // all ActivityData files have been got and read already, send DC log immediately
				sendRequest(buildRequest(FileEraseActivityRequest.class));
			} else {
				sendRequest(buildRequest(FileGetHardwareLogRequest.class));
			}
	    }
	}

	private boolean notifyActivityReadCompletedAndShouldStop() {
		MutableBoolean shouldStop = new MutableBoolean(false);
		if(mSyncPhaseCallback != null) {
			mSyncPhaseCallback.onSyncPhaseControllerSyncDataReadCompleted(this, mSyncSession.syncResults, shouldStop, mSyncCallback);
		}

		if (shouldStop.getValue()) {
            postProcessing(RESULT_INTERRUPTED);
			return true;
        }
		return false;
	}

	private void handleActivityFileErasedCompleted() {
		mSyncSession.mNumberOfActivityFilesErased += 1;
	    
	    if (mSyncSession.mNumberOfActivityFilesErased < mSyncSession.mNumberOfActivityFiles) {
	    	sendRequest(buildRequest(FileEraseActivityRequest.class));
	    }
	    else {
	    	sendRequest(buildRequest(FileGetHardwareLogRequest.class));
	    }
	}
	
	private SyncResult parseShineData(int fileFormat, long fileTimestamp, byte[] rawData, boolean isLastFile) {
		ActivityDataParser dataParser = ActivityDataParserFactory.getDataParser(fileFormat);
		
		if (dataParser.parseRawData(rawData, fileFormat, fileTimestamp) == false) {
			return null;
		}
		
		SyncResult syncResult = new SyncResult();
		syncResult.mActivities = dataParser.mActivities;
		syncResult.mTapEventSummarys = dataParser.mTapEventSummarys;
		syncResult.mSessionEvents = dataParser.mSessionEvents;

		double fileEndTimestamp = fileTimestamp;
		if (syncResult.mActivities != null && syncResult.mActivities.size() > 0) {
			fileEndTimestamp = syncResult.mActivities.get(syncResult.mActivities.size() - 1).mEndTimestamp;
		}
		syncResult.mSwimSessions = mSwimLapPostProcessor.processSwimEntries(dataParser.mSwimEntries, fileEndTimestamp, isLastFile);
		return syncResult;
	}
	
	private void postProcessing(int result) {
		mResult = result;		
		if (mResult == RESULT_SUCCESS) {
			mPhaseControllerCallback.onPhaseControllerCompleted(this);
			mSyncCallback.onSyncCompleted(ShineProfile.ActionResult.SUCCEEDED);
		} else {
			mPhaseControllerCallback.onPhaseControllerFailed(this);
			mSyncCallback.onSyncCompleted(ShineProfile.ActionResult.FAILED);
		}
	}

	private SyncResult correctTimestamp(SyncResult syncData, long fileTimestamp, boolean isLastFile) {
		SyncResult syncResult = mTimestampCorrector.processSyncData(syncData, fileTimestamp, isLastFile);
		return syncResult;
	}

	@Override
	public String getPhaseName() {
		return "SyncPhase";
	}
}
