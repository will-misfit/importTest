package com.misfit.syncsdk.utils;

/**
 * some constant parameters defined for SyncSDK
 */
public class SdkConstants {

	public final static int OPERATOR_RETRY_TIMES = 0;

	/* timeout values, in MilliSeconds */
	public final static long DEFAULT_TIMEOUT = 10 * 1000;
    public final static long SET_CONNECTION_PARAM_TIMEOUT = 11 * 1000;
	public final static long READ_DATA_TIMEOUT = 30 * 1000;  // in theory, this value should be dependent on activity file count
	public final static long ERASE_DATA_TIMEOUT = 30 * 1000; // same to above, this value should be dependent on activity file count
	public final static long CONNECT_TIMEOUT = 45 * 1000;
	public final static long SCAN_ONE_DEVICE_TIMEOUT = 20 * 1000;
    public final static long SCAN_DEVICE_TYPE_TIMEOUT = 30 * 1000;
    public final static long DISCONNECT_TIMEOUT = 2 * 1000;

	/* device model name */
	public static final String SHINE_MODEL_NAME = "shine";
	public static final String EARLY_SHINE_MODEL_PREFIX = "SH";

	/* .prometheus.app.PrometheusConfig.apiKey */
	public static final String HTTP_REQUEST_API_KEY = "76801581";

	/* local log file name prefix */
	public final static String EVENTS_PREFIX = "events_";
	public final static String SESSION_PREFIX = "session_";

	/* SyncMode for LogSession */
	public final static int SYNC_MODE_DEFAULT = -1;
	public final static int SYNC_MODE_MANUAL  = 1;
	public final static int SYNC_MODE_QUIET   = 2;
	public final static int SYNC_MODE_BACKGROUND = 3;
}
