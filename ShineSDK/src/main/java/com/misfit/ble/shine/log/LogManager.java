package com.misfit.ble.shine.log;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.misfit.ble.encryption.TextEncryption;
import com.misfit.ble.shine.network.APIClient;
import com.misfit.ble.shine.network.APIClient.PostLogSessionCallback;
import com.misfit.ble.shine.network.PostTask.PostTaskResult;
import com.misfit.ble.shine.storage.InternalStorage;
import com.misfit.ble.shine.storage.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class LogManager {
	private static final String TAG = "LogManager";
	
	private static final String LOG_FOLDER_NAME = "com.misfitwearables.ble.shine.log";
	private static final String POST_LOG_HANDLER_THREAD_NAME = "com.misfitwearables.ble.shine.log";
	
	public static final String SESSION_NAME_COMPONENT_SEPARATOR = "_";
	private static final String SESSION_FRAGMENT_COUNT_SEPARATOR = "-";
	private static final String SESSION_ID_DESCRIPTION_KEY = "sessionid";
	
	private static LogManager sDefaultInstance = null;
	public static LogManager getDefault() {
		if (sDefaultInstance == null) {
			sDefaultInstance = new LogManager();
			sDefaultInstance.setUpThreadHandler();
			sDefaultInstance.initiateTrackingSessionIds();
		}
		return sDefaultInstance;
	}
	
	/* 
	 * don't trigger another upload attempt if all posts have failed 
	 * to avoid continuously posting corrupted files.
	 */
	private volatile boolean mHasSuccessPost = false;
	
	private volatile int mNumberOfPendingPosts = 0;
	private volatile Object lockObject = new Object();
	
	private PostLogHandlerThread mPostLogHandlerThread;
	private void setUpThreadHandler() {
		mPostLogHandlerThread = new PostLogHandlerThread();
	}
	
	public void saveAndUploadLog(LogSession logSession) {
		if (!logSession.hasLogItem()) return;

		String logContent = logSession.toJSON().toString();
		String encryptedLog = TextEncryption.encrypt(logContent);
		String sessionName = logSession.getName();
		if (InternalStorage.saveTextToFile(encryptedLog, LOG_FOLDER_NAME, filenameFromSessionName(sessionName))) {
			onSessionSaved(sessionName);
			uploadLogSession();
		}
	}
	
	public void saveAndUploadLog(ScanLogSession logSession) {
		String logContent = logSession.toJSON().toString();
		String encryptedLog = TextEncryption.encrypt(logContent);
		String sessionName = logSession.getName();
		if (InternalStorage.saveTextToFile(encryptedLog, LOG_FOLDER_NAME, filenameFromSessionName(sessionName))) {
			onSessionSaved(sessionName);
			uploadLogSession();
		}
	}
	
	public void uploadLogSession() {
		synchronized (lockObject) {
			if (mNumberOfPendingPosts > 0)
				return;
			
			File[] logSessionFiles = InternalStorage.getFiles(LOG_FOLDER_NAME);
			if (logSessionFiles == null)
				return;
			
			sortSessionFiles(logSessionFiles);
			
			for (int i = 0; i < logSessionFiles.length; ++i) {
				mNumberOfPendingPosts++;
				mPostLogHandlerThread.postSession(logSessionFiles[i], mPostLogHandlerThreadCallback);
			}
		}
	}
	
	private void handlePostingResult(File file, PostTaskResult result) {
		synchronized (lockObject) {
			if (result != null && result.hasSucceeded()) {
				mHasSuccessPost = true;
				if (file != null && file.exists()) {
					file.delete();
				}
				updateSessionIdsMapping(result);
			}
			
			mNumberOfPendingPosts--;
			if (mNumberOfPendingPosts <= 0) {
				mNumberOfPendingPosts = 0;
				if (mHasSuccessPost) {
					uploadLogSession();
					mHasSuccessPost = false;
				}
			}
		}
	}
	
	private PostLogSessionCallback mPostLogHandlerThreadCallback = new PostLogSessionCallback() {
		@Override
		public void onPostLogSessionResult(File logFile, PostTaskResult result) {
			handlePostingResult(logFile, result);
		}
	};
	
	private static class PostLogHandlerThread extends HandlerThread {
		private Handler mHandler;
		
		public PostLogHandlerThread() {
			super(POST_LOG_HANDLER_THREAD_NAME, android.os.Process.THREAD_PRIORITY_BACKGROUND);
			start();
			mHandler = new Handler(getLooper());
		}
		
		public void postSession(final File file, final PostLogSessionCallback callback)
		{
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					APIClient.postLogSession(file, callback);
				}
			});
		}
	}
	
	/*
	 * Pushing Partial Log to DC
	 */
	private static final String LOG_MANAGER_PREFERENCE_KEY = "com.misfitwearables.ble.shine.log.LogManager";
	private static final String TRACKING_SESSION_ID_KEY = "com.misfitwearables.ble.shine.log.LogManager.trackingSessionIdsKey";
	
	private HashMap<String, String> mTrackingSessionIds = new HashMap<String, String>();
	private HashMap<String, Integer> mFragmentCounts = new HashMap<String, Integer>();
	
	private void loadTrackingSessionIds() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		String jsonString = getTrackingSessionIdsJSONString();
		if (jsonString != null) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(jsonString);
				
				Iterator<String> keysItr = jsonObject.keys();
				while (keysItr.hasNext()) {
					String key = keysItr.next();
					String value = jsonObject.getString(key);
					map.put(key, value);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		mTrackingSessionIds = map;
	}
	
	private void saveTrackingSessionIds() {
		setTrackingSessionIdsJSONString(mTrackingSessionIds);
	}
	
	private static SharedPreferences getPreferences() {
		return Preferences.getSharedPreferences(LOG_MANAGER_PREFERENCE_KEY);
	}
	
	private static String getTrackingSessionIdsJSONString() {
		return getPreferences().getString(TRACKING_SESSION_ID_KEY, null);
	}
	
	private static boolean setTrackingSessionIdsJSONString(HashMap<String, String> trackingSessionIds) {
		if (null == trackingSessionIds) {
			return getPreferences().edit().putString(TRACKING_SESSION_ID_KEY, "").commit(); 
		}
		
		JSONObject jsonObject = new JSONObject(trackingSessionIds);
		String jsonString = jsonObject.toString();
		return getPreferences().edit().putString(TRACKING_SESSION_ID_KEY, jsonString).commit();
	}
	
	private void initiateTrackingSessionIds() {
		loadTrackingSessionIds();
		
		File[] logSessionFiles = InternalStorage.getFiles(LOG_FOLDER_NAME);
		List<String> keys = new ArrayList<String>(mTrackingSessionIds.keySet());
		
		for (String sessionName : keys) {
			boolean inUse = false;
			
			if (logSessionFiles != null) {
				for (File file : logSessionFiles) {
					if (file.getName().startsWith(sessionName)) {
						inUse = true;
						break;
					}
				}
			}
			
			if (!inUse) {
				mTrackingSessionIds.remove(sessionName);
			}
		}
		
		saveTrackingSessionIds();
	}
	
	/*
	 * Sort session files
	 */
	private String timeAndIdFromSessionName(String sessionName) {
		int separatorIndex = sessionName.indexOf(SESSION_NAME_COMPONENT_SEPARATOR);
		if (separatorIndex == -1) {
			return sessionName;
		}
		return sessionName.substring(separatorIndex);
	}
	
	private void sortSessionFiles(File[] sessionFiles) {
		if (sessionFiles == null) {
			Log.e(TAG, "sortSessionFiles - Illegal arguments - sessionFiles: " + sessionFiles);
			return;
		}
		
		Arrays.sort(sessionFiles, new Comparator<File>() {
			@Override
			public int compare(File lhs, File rhs) {
				String sessionName1 = sessionNameFromFilename(lhs.getName());
				String sessionName2 = sessionNameFromFilename(rhs.getName());
				
				if (sessionName1.equalsIgnoreCase(sessionName2)) {
					int fragmentIndex1 = fragmentIndexFromFilename(lhs.getName());
					int fragmentIndex2 = fragmentIndexFromFilename(rhs.getName());
					return (fragmentIndex1 <= fragmentIndex2) ? -1 : 1;
				}
				
				String time1 = timeAndIdFromSessionName(sessionName1);
				String time2 = timeAndIdFromSessionName(sessionName2);
				return time1.compareToIgnoreCase(time2);
			}
		});
	}
	
	/*
	 * Filename - SessionName - FragmentCount
	 */
	// TODO: improve the filename format.
	private String sessionNameFromFilename(String filename) {
		if (filename == null)
			return null;
		
		int markerIndex = filename.lastIndexOf(SESSION_FRAGMENT_COUNT_SEPARATOR);
		if (markerIndex == -1)
			return filename;
		
		return filename.substring(0, markerIndex);
	}
	
	private String filenameFromSessionName(String sessionName) {
		Integer fragmentCount = mFragmentCounts.get(sessionName);
		if (fragmentCount == null || fragmentCount.intValue() == 0) {
			return sessionName;
		}
		
		return sessionName + SESSION_FRAGMENT_COUNT_SEPARATOR + fragmentCount;
	}
	
	private int fragmentIndexFromFilename(String filename) {
		if (filename == null)
			return 0;
		
		int markerIndex = filename.lastIndexOf(SESSION_FRAGMENT_COUNT_SEPARATOR);
		if (markerIndex == -1)
			return 0;
		
		return Integer.parseInt(filename.substring(markerIndex));
	}
	
	private void onSessionSaved(String sessionName) {
		Integer fragmentCount = mFragmentCounts.get(sessionName);
		if (fragmentCount == null) {
			fragmentCount = 0;
		}
		mFragmentCounts.put(sessionName, fragmentCount + 1);
	}
	
	/*
	 * SessionId and PostRequest
	 */
	public String addSessionIdToPostRequest(String filename, String content) {
		if (filename == null || content == null) {
			Log.e(TAG, "addSessionIdToPostRequest - illegal arguments: {"
					+ "filename: " + filename + ", " 
					+ "content: " + ((content != null) ? "okay" : "null"));
			return null;
		}
		
		String sessionName = sessionNameFromFilename(filename);
		String sessionId = mTrackingSessionIds.get(sessionName);
		if (sessionName != null && sessionId != null) {
			try {
				JSONObject json = new JSONObject(content);
				json.put(SESSION_ID_DESCRIPTION_KEY, sessionId);
				content = json.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return content;
	}
	
	private void updateSessionIdsMapping(PostTaskResult response) {
		if (mTrackingSessionIds == null || response == null) {
			Log.e(TAG, "saveSessionIdForSession - Illegal arguments: {"
					+ "mTrackingSessionIds: " + ((mTrackingSessionIds != null) ? "okay" : "null") + ", "
					+ "response: " + ((response != null) ? "okay" : "null"));
			return;
		}
		
		String sessionName = sessionNameFromFilename(response.mFilename);
		String sessionId = sessionIdFromResponseMessage(response.mResponseMessage);
		mTrackingSessionIds.put(sessionName, sessionId);
		saveTrackingSessionIds();
	}
	
	private String sessionIdFromResponseMessage(String responseMessage) {
		String sessionId = null;
		try {
			JSONObject json = new JSONObject(responseMessage);
			sessionId = json.getString(SESSION_ID_DESCRIPTION_KEY);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return sessionId;
	}
}
