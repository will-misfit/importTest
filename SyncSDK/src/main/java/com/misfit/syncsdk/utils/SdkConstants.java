package com.misfit.syncsdk.utils;

/**
 * some constant parameters defined for SyncSDK
 */
public class SdkConstants {

	public final static int OPERATOR_RETRY_TIMES = 3;

	/* timeout values, in MilliSeconds */
	public final static long DEFAULT_TIMEOUT = 10 * 1000;
	public final static long READ_DATA_TIMEOUT = 10 * 1000;
	public final static long ERASE_DATA_TIMEOUT = 30 * 1000;
	public final static long CONNECT_TIMEOUT = 45 * 1000;
	public final static long SCAN_TIMEOUT = 20 * 1000;

	/* device model name */
	public static final String SHINE_MODEL_NAME = "shine";
	public static final String EARLY_SHINE_MODEL_PREFIX = "SH";

	/* .prometheus.app.PrometheusConfig.apiKey */
	public static final String HTTP_REQUEST_API_KEY = "76801581";

	/* local log file name prefix */
	public final static String EVENTS_PREFIX = "events_";
	public final static String SESSION_PREFIX = "session_";
}
