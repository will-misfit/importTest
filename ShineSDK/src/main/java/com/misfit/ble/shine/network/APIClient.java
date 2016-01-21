package com.misfit.ble.shine.network;

import com.misfit.ble.BuildConfig;
import com.misfit.ble.sdk.GlobalVars;
import com.misfit.ble.shine.network.PostTask.PostTaskResult;
import com.misfit.ble.util.Helper;

import java.io.File;

public class APIClient {
	
	private static final String API_SERVER = BuildConfig.API_SERVER;
	private static final String ACCESS_KEY = BuildConfig.ACCESS_KEY;
	
	public static interface PostLogSessionCallback {
		public void onPostLogSessionResult(File file, PostTaskResult response);
	}
	
	public static void postLogSession(final File logFile, final PostLogSessionCallback callback) {
		PostTaskResult result = null;
		
		if (logFile == null || logFile.exists() == false || Helper.isInternetConnectionAvailable(GlobalVars.getApplicationContext()) == false) {
			// do nothing.
		} else {
			PostTask postTask = new PostTask(API_SERVER, ACCESS_KEY, logFile);
			result = postTask.execute();
		}
		callback.onPostLogSessionResult(logFile, result);
	}
}
