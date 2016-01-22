package com.misfit.syncsdk;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.VolleyError;
import com.misfit.syncsdk.model.FirmwareInfo;
import com.misfit.syncsdk.utils.CheckUtils;
import com.misfit.syncsdk.utils.LocalFileUtils;
import com.misfit.syncsdk.utils.SdkConstants;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class to manage the firmware version query and binary file download
 */
public class FirmwareManager {

    public interface GetLatestFirmwareListener {
        void onSucceed(boolean shouldOta, String firmwareVersion);
        void onFailed(int errorReason);
    }

    public interface DownloadLatestFirmwareListener {
        void onFinished(String filePath);
        void onFailed(int errorReason);
    }

    protected static final String TAG = "FirmwareDownloadService";

    public static final String SHINE_MODEL_NAME = "shine";

    public static final String FIRMWARE_EXTENSION         = "bin";
    public static final String TEMP_FIRMWARE_EXTENSION     = "bin.temp";

    public static final String FIRMWARE_KEY         = "firmware_version_key";
    public static final String DOWNLOAD_KEY         = "firmware_download_url_key";
    public static final String CHECKSUM_KEY         = "firmware_checksum_key";
    public static final String CHANGE_LOG_KEY     = "firmware_change_log_key";
    public static final String MODEL_NUMBER_KEY     = "firmware_model_number_key";

    private boolean isCheckingFirmware = false;

    public static FirmwareInfo latestFirmwareInfo;

    private Handler mDownloadHandler;
    private static FirmwareManager mFirmwareManager;

    private FirmwareManager() {
        /*
        SharedPreferencesUtils sharedInstance = SharedPreferencesUtils
                .sharedInstance();
        latestFirmware = new ShineFirmware(
                sharedInstance.getInfo(TAG, MODEL_NUMBER_KEY, ""),
                sharedInstance.getInfo(TAG, FIRMWARE_KEY, ""),
                sharedInstance.getInfo(TAG, CHANGE_LOG_KEY, ""),
                sharedInstance.getInfo(TAG, CHECKSUM_KEY, ""),
                sharedInstance.getInfo(TAG, DOWNLOAD_KEY, ""));
                */
    }

    public static FirmwareManager getInstance() {
        if (mFirmwareManager == null) {
            synchronized (FirmwareManager.class) {
                if (mFirmwareManager == null) {
                    mFirmwareManager = new FirmwareManager();
                }
            }
        }
        return mFirmwareManager;
    }

    public void setDownloadHandler(Handler handler) {
        this.mDownloadHandler = handler;
    }

    /**
     * 1. query latest firmware version from remote firmware server
     * 2. download firmware if there is new firmware available
     * No callback to tell the result
     */
    public void checkLatestFirmware(String modelName, String currFwVersionNumber) {
    }

    /**
     * @return: Firmware version number.
     * If it needs to OTA, tell invoker by callback onSucceed()
     * If it does not need OTA, tell invoker by callback onSucceed()
     * If the query fails, tell invoker by callback onFail()
     * */
    public void shouldOta(String modelName, String currFwVersionNumber, GetLatestFirmwareListener getFirmwareListener) {
        return;
    }

    /**
     * tells if the new firmware to OTA is downloaded already
     * @return if the new firmware is ready, return true
     * if the new firmware download is ongoing, return false now, and notify the invoker by the listener
     * */
    public boolean isNewFirmwareReady(String firmwareVersion, DownloadLatestFirmwareListener downloadListener) {
        return true;
    }

    /**
     * tells if the new firmware to OTA is downloaded already. return the result immediately
     * */
    public boolean isNewFirmwareReadyNow(String firmwareVersion) {
        return LocalFileUtils.isFileExist(getFirmwareFileName(firmwareVersion));
    }

    /*
    public void checkLatestFirmware(String modelName, GetLatestFirmwareListener listener){
        if (isCheckingFirmware) {
            return;
        }

        isCheckingFirmware = true;
        if (CheckUtils.isStringEmpty(modelName)) {
            modelName = SHINE_MODEL_NAME;
        }
        // APIClient.CommonAPI.downloadLatestFirmware(latestRequestListener, modelName);
    }
    */

    public boolean isCheckingFirmware() {
        return isCheckingFirmware;
    }

    public boolean shouldUpdate(int deviceType, String currentFirmware, String targetFirmware){
        return false;
    }

    /**
     * FirmwareRequest extends PrometheusJsonObjectRequest extending volley.toolbox.JsonRequest
     * RequestListener extends volley.Listener, ErrorListener
     * */
    /*
    public RequestListener<FirmwareRequest> latestRequestListener = new RequestListener<FirmwareRequest>() {

        public void onErrorRequest(VolleyError error) {
            Log.d(TAG, "Network error when checking firmware version");
            isCheckingFirmware = false;
        }

        public void onResponse(FirmwareRequest request) {
            FirmwareInfo newFirmwareInfo = request.exportFirmwareInfo();
            String newFwVersionNumber = newFirmwareInfo.getVersionNumber();
			String oldFwVersionNumber = latestFirmwareInfo.getVersionNumber();
			String newFwModelName = newFirmwareInfo.getModelName();
			String oldFwModelName = latestFirmwareInfo.getModelName();

            if (!CheckUtils.isStringEmpty(newFwModelName)) {
			    if (newFwModelName.equals(oldFwModelName)
						|| (CheckUtils.isStringEmpty(newFwModelName) && oldFwModelName.equals(SdkConstants.SHINE_MODEL_NAME))
						|| (CheckUtils.isStringEmpty(oldFwModelName) && newFwModelName.equals(SdkConstants.SHINE_MODEL_NAME))) {
					if (newFwVersionNumber != null && !newFwVersionNumber.equals(oldFwVersionNumber)) {
					    latestFirmwareInfo = newFirmwareInfo;
						saveLatestFwVersionInfoToPreferences();
					}

					if (shouldDownloadNewFirmware(newFwVersionNumber, oldFwVersionNumber)) {
					    String params[] = null;
						new DownloadFirmwareTask(newFirmwareInfo, oldFwVersionNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
					} else {
					    isCheckingFirmware = false;
					}
				} else {
				    latestFirmwareInfo = newFirmwareInfo;
					saveLatestFwVersionInfoToPreferences();

					if (!isNewFirmwareReadyNow(newFwVersionNumber)) {
					    String params[] = null;
						new DownloadFirmwareTask(newFirmwareInfo, oldFwVersionNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
					} else {
					    isCheckingFirmware = false;
					}
				}
			} else {
			    isCheckingFirmware = false;
			}
        }
    };
    */

    private class DownloadFirmwareTask extends AsyncTask<String, Integer, Boolean> {
        private String oldVersionNumber;
        private FirmwareInfo newFirmwareInfo;

        public DownloadFirmwareTask(FirmwareInfo newFwInfo, String oldVerNumber) {
            this.oldVersionNumber = oldVerNumber;
            this.newFirmwareInfo = newFwInfo;
        }

        protected Boolean doInBackground(String... params) {
            boolean result = downloadFirmwareFile(newFirmwareInfo);
            return result;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                // delete old firmware
                if (!(CheckUtils.isStringEmpty(oldVersionNumber))){
                    deleteOldFirmware(oldVersionNumber, newFirmwareInfo.getVersionNumber());
                }
            }
            isCheckingFirmware = false;
        }
    }

    private boolean downloadFirmwareFile(FirmwareInfo firmwareInfo) {
        String versionNumber = firmwareInfo.getVersionNumber();
        String tempFirmwareFileName = getTempFirmwareFileName(versionNumber);
        String firmwareFileName = getFirmwareFileName(versionNumber);

        String downloadUrl = firmwareInfo.getDownloadUrl();
        String checksum = firmwareInfo.getChecksum();

        try {
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                return false;
            }
            URL url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("connection", "close");
            connection.connect();

            if (LocalFileUtils.isFileExist(firmwareFileName)) {
                // verify
                String md5 = LocalFileUtils.getMD5String(LocalFileUtils.read(firmwareFileName));
                if (checksum.equals(md5)) {
                    return true;
                } else {
                    LocalFileUtils.delete(firmwareFileName);
                }
            }
            // Always delete temp file if it exists
            if (LocalFileUtils.isFileExist(TEMP_FIRMWARE_EXTENSION)) {
                LocalFileUtils.delete(TEMP_FIRMWARE_EXTENSION);
            }

            InputStream input = new BufferedInputStream(connection.getInputStream());
            FileOutputStream output = LocalFileUtils.getOutputStream(tempFirmwareFileName);

            byte[] data = new byte[1024];
            int count = 0;
            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();

            byte[] fileData = LocalFileUtils.read(tempFirmwareFileName);

            if (total > 0) {
                String md5 = LocalFileUtils.getMD5String(fileData);
                if (!md5.equals(checksum)) {
                    Log.d(TAG, "Download succeeds but MD5 verification fails");
                    LocalFileUtils.delete(tempFirmwareFileName);
                    return false;
                } else {
                    Log.d(TAG, "Verification succeeds " + firmwareFileName);
                    LocalFileUtils.rename(tempFirmwareFileName, firmwareFileName);
                    return true;
                }
            } else {
                Log.d(TAG, "Download fails");
                LocalFileUtils.delete(tempFirmwareFileName);
                return false;
            }
        } catch (Exception e) {
            LocalFileUtils.delete(tempFirmwareFileName);
            Log.d(TAG, "Exception during download " + e.toString());
            return false;
        }
    }

    public void deleteOldFirmware(String oldFirmwareVersion, String newFirmwareVersion) {
        if (!CheckUtils.isStringEmpty(oldFirmwareVersion)
                && !oldFirmwareVersion.equals(newFirmwareVersion)) {
            deleteFirmware(oldFirmwareVersion);
        }
    }

    public void deleteFirmware(String firmwareVersion) {
        String firmwareName = getFirmwareFileName(firmwareVersion);
        if (LocalFileUtils.delete(firmwareName)) {
            Log.d(TAG, String.format("Delete old firmware %s successfully", firmwareVersion));
        } else {
            Log.d(TAG, String.format("Delete old firmware %s error/does't exist", firmwareVersion));
        }
    }

    protected boolean shouldDownloadNewFirmware(String newFirmwareVersion,
            String oldFirmwareVersion) {

        if (newFirmwareVersion != null
                && !newFirmwareVersion.equals(oldFirmwareVersion)) {
            // If newfirmware and oldfirmware is different, we should download
            // new firmware if it doesn't exist
            return (!isNewFirmwareReadyNow(newFirmwareVersion));
        } else {
            // If new firmware is null or is the newest firmware, we should
            // download it again if it isn't ready (maybe md5 checking failed in
            // the last downloading time)
            return (!isNewFirmwareReadyNow(oldFirmwareVersion));
        }
    }

    private void sendMessage(int option) {
        if (mDownloadHandler != null) {
            Message msg = new Message();
            msg.what = option;
            mDownloadHandler.sendMessage(msg);
        }
    }

    private static void saveLatestFwVersionInfoToPreferences() {
        /*
        SharedPreferencesUtils sharedInstance = SharedPreferencesUtils
                .sharedInstance();
        sharedInstance.saveInfo(TAG, FIRMWARE_KEY, latestFirmware.getVersionNumber());
        sharedInstance.saveInfo(TAG, DOWNLOAD_KEY, latestFirmware.getDownloadUrl());
        sharedInstance.saveInfo(TAG, CHECKSUM_KEY, latestFirmware.getChecksum());
        sharedInstance.saveInfo(TAG, CHANGE_LOG_KEY, latestFirmware.getChangeLog());
        sharedInstance.saveInfo(TAG, MODEL_NUMBER_KEY, latestFirmware.getModelNumber());
        */
    }

    /* utility API */
    public static String getFirmwareFileName(String firmwareVersion) {
        return String.format("%s.%s", firmwareVersion, FIRMWARE_EXTENSION);
    }

    public static String getTempFirmwareFileName(String firmwareVersion) {
        return String.format("%s.%s", firmwareVersion, TEMP_FIRMWARE_EXTENSION);
    }

    public static boolean shouldUpgradeFirmware(String targetVersion) {
        String currFirmwareVersion = latestFirmwareInfo.getVersionNumber();
        return (!CheckUtils.isStringEmpty(targetVersion)
                && !CheckUtils.isStringEmpty(currFirmwareVersion)
                && !targetVersion.equals(currFirmwareVersion));
    }

    public static boolean isTheSameTypeOfFirmware(String modelNumber) {
        String currentFirmwareVersion = latestFirmwareInfo.getVersionNumber();
        String currentModelName = latestFirmwareInfo.getModelName();
        return (!CheckUtils.isStringEmpty(modelNumber)
                && (CheckUtils.isStringEmpty(currentFirmwareVersion)
                    || modelNumber.equals(currentModelName)
                    || (SdkConstants.SHINE_MODEL_NAME.equals(modelNumber) && CheckUtils.isStringEmpty(currentModelName))));
    }

    public static byte[] getFirmwareData(String firmwareVersion) {
        return LocalFileUtils.read(getFirmwareFileName(firmwareVersion));
    }

    public static byte[] getFirmwareData() {
        return LocalFileUtils.read(getFirmwareFileName(latestFirmwareInfo.getVersionNumber()));
    }

    public static String getVersionNumber() {
        return latestFirmwareInfo.getVersionNumber();
    }

    public static boolean isFirmwareReady() {
        boolean isFirmwareReady = mFirmwareManager.isNewFirmwareReadyNow(latestFirmwareInfo.getVersionNumber());
        Log.d(TAG, "isFirmwareReady " + isFirmwareReady);
        return isFirmwareReady;
    }
}