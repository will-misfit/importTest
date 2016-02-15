package com.misfit.syncsdk;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.VolleyError;
import com.misfit.syncsdk.model.FirmwareInfo;
import com.misfit.syncsdk.request.FirmwareRequest;
import com.misfit.syncsdk.request.RequestListener;
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

    public interface CheckLatestFirmwareListener {
        void onSucceed(boolean shouldOta, String firmwareVersion);
        void onFailed(int errorReason);
    }

    public interface DownloadLatestFirmwareListener {
        void onSucceed(String filePath);
        void onFailed(int failReason);
    }

    protected static final String TAG = "FirmwareDownloadService";

    public static final String SHINE_MODEL_NAME = "shine";

    public static final String FIRMWARE_EXTENSION         = "bin";
    public static final String TEMP_FIRMWARE_EXTENSION    = "bin.temp";

    public static final String FIRMWARE_KEY         = "firmware_version_key";
    public static final String DOWNLOAD_KEY         = "firmware_download_url_key";
    public static final String CHECKSUM_KEY         = "firmware_checksum_key";
    public static final String CHANGE_LOG_KEY       = "firmware_change_log_key";
    public static final String MODEL_NUMBER_KEY     = "firmware_model_number_key";

    private boolean mCheckingFirmware = false;

    public static FirmwareInfo latestFirmwareInfo;

    private String mCurrentModelName;
    private String mCurrentFirmwareVersion;

    private CheckLatestFirmwareListener mCheckFirmwareListener;
    private DownloadLatestFirmwareListener mDownloadFirmwareListener;

    private static FirmwareManager mFirmwareManager;

    private FirmwareManager() {
        /*
        SharedPreferencesUtils sharedInstance = SharedPreferencesUtils.sharedInstance();
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

    /**
     * 1. query latest firmware version from remote firmware server
     * 2. download firmware if there is new firmware available
     * No callback to tell the result
     */
    public void checkLatestFirmware(String modelName, String firmwareVersionNumber) {
        mCurrentModelName = modelName;
        mCurrentFirmwareVersion = firmwareVersionNumber;

        mCheckingFirmware = true;
        //TODO: send FirmwareRequest
    }

    /**
     * if checkLatestFirmware is not working, start the FirmwareRequest, and inform the invoker via callback
     * */
    public void shouldOta(String modelName, String firmwareVersionNumber, CheckLatestFirmwareListener getFirmwareListener) {
        mCurrentModelName = modelName;
        mCurrentFirmwareVersion = firmwareVersionNumber;
        setCheckFirmwareListener(getFirmwareListener);
        //TODO: send FirmwareRequest
    }

    /**
     * tells if the new firmware to OTA is downloaded already
     * if the new firmware download is ongoing, notify the invoker by the listener
     * */
    public void whenFirmwareReady(String firmwareVersion, DownloadLatestFirmwareListener downloadListener) {
        setDownloadFirmwareListener(downloadListener);
        //TODO: if firmware is not existing locally and download task is not started, start DownloadFirmware task;
        //TODO: if it starts already, set callback and wait for its invoke
    }

    /**
     * tells if the given firmware file exists locally
     * */
    public boolean isFirmwareExisting(String firmwareVersion) {
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

    /**
     * FirmwareRequest extends PrometheusJsonObjectRequest extending volley.toolbox.JsonRequest
     * RequestListener extends volley.Listener, ErrorListener
     * */
    public RequestListener<FirmwareRequest> latestFirmwareRequestListener = new RequestListener<FirmwareRequest>() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Network error when checking firmware version");
            mCheckingFirmware = false;
            onCheckFirmwareFailed(error);
        }

        @Override
        public void onResponse(FirmwareRequest request) {
            FirmwareInfo newFirmwareInfo = request.exportFirmwareInfo();
            String newFwModelName = newFirmwareInfo.getModelName();
            String oldFwModelName = mCurrentModelName;
            String newFwVersionNumber = newFirmwareInfo.getVersionNumber();
            String oldFwVersionNumber = mCurrentFirmwareVersion;

            // FIXME: except model name of 'shine', is there any other device model need to OTA when oldFwModelName is null?
            if (!CheckUtils.isStringEmpty(newFwModelName)) {
                if (newFwModelName.equals(oldFwModelName)
                        || (CheckUtils.isStringEmpty(newFwModelName) && oldFwModelName.equals(SdkConstants.SHINE_MODEL_NAME))
                        || (CheckUtils.isStringEmpty(oldFwModelName) && newFwModelName.equals(SdkConstants.SHINE_MODEL_NAME))) {
                    if (newFwVersionNumber != null && !newFwVersionNumber.equals(oldFwVersionNumber)) {
                        latestFirmwareInfo = newFirmwareInfo;
                    }

                    if (shouldDownloadNewFirmware(newFwVersionNumber, oldFwVersionNumber)) {
                        onCheckFirmwareSucceed(true, newFwVersionNumber);
                        String params[] = null;
                        new DownloadFirmwareTask(newFirmwareInfo, oldFwVersionNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                    } else {
                        onCheckFirmwareSucceed(false, newFwVersionNumber);
                        mCheckingFirmware = false;
                    }
                } else {  // FIXME: do we need DownloadFirmware in this clause?
                    latestFirmwareInfo = newFirmwareInfo;

                    if (!isFirmwareExisting(newFwVersionNumber)) {
                        onCheckFirmwareSucceed(true, newFwVersionNumber);
                        String params[] = null;
                        new DownloadFirmwareTask(newFirmwareInfo, oldFwVersionNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                    } else {
                        onCheckFirmwareSucceed(false, newFwVersionNumber);
                        mCheckingFirmware = false;
                    }
                }
            } else {
                mCheckingFirmware = false;
            }
        }
    };

    /**
     * AsyncTask to download the firmware
     * */
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
                if (!(CheckUtils.isStringEmpty(oldVersionNumber))){
                    deleteOldFirmware(oldVersionNumber, newFirmwareInfo.getVersionNumber());
                }
                onDownloadFirmwareSucceed(newFirmwareInfo.getVersionNumber());
            } else {
                onDownloadFirmwareFailed(-1);  // TODO: define the download error code
            }
            mCheckingFirmware = false;
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

    protected boolean shouldDownloadNewFirmware(String newFirmwareVersion, String oldFirmwareVersion) {
        if (newFirmwareVersion != null && !newFirmwareVersion.equals(oldFirmwareVersion)) {
            return (!isFirmwareExisting(newFirmwareVersion));
        } else {
            return false;
        }
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
        boolean downloaded = mFirmwareManager.isFirmwareExisting(latestFirmwareInfo.getVersionNumber());
        Log.d(TAG, "isFirmwareReady " + downloaded);
        return downloaded;
    }

    public void setDownloadFirmwareListener(DownloadLatestFirmwareListener downloadListener) {
        mDownloadFirmwareListener = downloadListener;
    }

    public void cleanDownloadFirmwareListener() {
        mDownloadFirmwareListener = null;
    }

    public void setCheckFirmwareListener(CheckLatestFirmwareListener checkFirmwareListener) {
        mCheckFirmwareListener = checkFirmwareListener;
    }

    public void cleanGetFirmwareListener() {
        mCheckFirmwareListener = null;
    }

    /* invoke GetLatestFirmwareListener callback */
    private void onCheckFirmwareSucceed(boolean shouldOta, String fwVersionNumber) {
        if (mCheckFirmwareListener != null) {
            mCheckFirmwareListener.onSucceed(shouldOta, fwVersionNumber);
        }
    }

    private void onCheckFirmwareFailed(VolleyError volleyError) {
        int errorReason = -1;
        if (volleyError != null && volleyError.networkResponse != null) {
            errorReason = volleyError.networkResponse.statusCode;
        }

        if (mCheckFirmwareListener != null) {
            mCheckFirmwareListener.onFailed(errorReason);
        }
    }

    /* invoke DownloadFirmwareListener callback */
    private void onDownloadFirmwareSucceed(String firmwareVersion) {
        if (mDownloadFirmwareListener != null) {
            mDownloadFirmwareListener.onSucceed(getFirmwareFileName(firmwareVersion));
        }
    }

    private void onDownloadFirmwareFailed(int errorCode) {
        if (mDownloadFirmwareListener != null) {
            mDownloadFirmwareListener.onFailed(errorCode);
        }
    }
}