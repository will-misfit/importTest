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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to manage the firmware version query and binary file download
 */
public class FirmwareManager {
    protected static final String TAG = "FirmwareDownloadService";

    public interface CheckFirmwareServerListener {
        void onSucceed(boolean shouldOta, String firmwareVersion);
        void onFailed(int errorReason);
    }

    public interface DownloadFirmwareListener {
        void onSucceed(String fileName);
        void onFailed(int failReason);
    }

    public static final int STATUS_NOT_STARTED = 0;
    public static final int STATUS_CHECKING_SERVER = 1;
    public static final int STATUS_CHECKING_COMPLETED = 2;

    private AtomicInteger mWorkingStatus;

    public static final String FIRMWARE_FOLDER = "com.misfit.syncsdk.firmware";

    public static final String SHINE_MODEL_NAME = "shine";

    public static final String FIRMWARE_EXTENSION         = "bin";
    public static final String TEMP_FIRMWARE_EXTENSION    = "bin.temp";

    // TODO: does it need to save FirmwareInfo to SharedPreference?
    public static final String FIRMWARE_KEY         = "firmware_version_key";
    public static final String DOWNLOAD_KEY         = "firmware_download_url_key";
    public static final String CHECKSUM_KEY         = "firmware_checksum_key";
    public static final String CHANGE_LOG_KEY       = "firmware_change_log_key";
    public static final String MODEL_NUMBER_KEY     = "firmware_model_number_key";

    public static FirmwareInfo mNewFirmwareInfo;
    private int mErrorNetworkStatus = 0;

    private String mCurrentModelName;
    private String mCurrentFirmwareVersion;

    private List<CheckFirmwareServerListener> mCheckFirmwareListeners = new ArrayList<>();
    private List<DownloadFirmwareListener> mDownloadFirmwareListeners = new ArrayList<>();

    private static FirmwareManager mFirmwareManager;

    private FirmwareManager() {
        mWorkingStatus = new AtomicInteger(STATUS_NOT_STARTED);
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
    public void checkLatestFirmware(String modelName, String firmwareVersion) {
        checkLatestFirmware(modelName, firmwareVersion, null);
    }

    /**
     * overload method which entitles listener to subscribe result of checking the latest firmware version remotely
     * */
    public void checkLatestFirmware(String modelName, String firmwareVersion, CheckFirmwareServerListener checkListener) {
        if (mWorkingStatus.get() == STATUS_CHECKING_SERVER) {
            Log.d(TAG, "checkLatestFirmware(), but it is working on");
            subscribeFirmwareCheck(checkListener);
            return;
        }

        if (mWorkingStatus.get() == STATUS_CHECKING_COMPLETED) {
            Log.d(TAG, "checkLatestFirmware(), check remote firmware has completed");
            if (mNewFirmwareInfo != null) {
                boolean shouldOta = shouldOTA(mNewFirmwareInfo.getVersionNumber(), mCurrentFirmwareVersion);
                if (checkListener != null) {
                    checkListener.onSucceed(shouldOta, mNewFirmwareInfo.getVersionNumber());
                }
            } else {
                if (checkListener != null) {
                    checkListener.onFailed(mErrorNetworkStatus);
                }
            }
            return;
        }

        Log.d(TAG, String.format("checkLatestFirmware() start running, model name %s, firmware version %s", modelName, firmwareVersion));
        subscribeFirmwareCheck(checkListener);

        mCurrentModelName = modelName;
        mCurrentFirmwareVersion = firmwareVersion;
        mWorkingStatus.set(STATUS_CHECKING_SERVER);
        FirmwareRequest.getLatestRequest(latestFirmwareRequestListener, modelName).execute();
    }

    public static boolean isFirmwareExisting(String firmwareVersion) {
        if (CheckUtils.isStringEmpty(firmwareVersion)) {
            return false;
        }
        String fileName = getFirmwareFileName(firmwareVersion);
        return LocalFileUtils.isFileExist(FIRMWARE_FOLDER, fileName);
    }

    /**
     * FirmwareRequest extends PrometheusJsonObjectRequest extending volley.toolbox.JsonRequest
     * RequestListener extends volley.Listener, ErrorListener
     * */
    public RequestListener<FirmwareRequest> latestFirmwareRequestListener = new RequestListener<FirmwareRequest>() {

        @Override
        public void onErrorResponse(VolleyError error) {
            mWorkingStatus.set(STATUS_CHECKING_COMPLETED);
            Log.d(TAG, String.format("FirmwareRequest onErrorResponse(), %s", error.getMessage()));
            handleOnCheckFirmwareFailed(error);
        }

        // here FirmwareRequest behaves like @response
        @Override
        public void onResponse(FirmwareRequest request) {
            mWorkingStatus.set(STATUS_CHECKING_COMPLETED);
            Log.d(TAG, String.format("FirmwareRequest onResponse(), %s", request.toString()));

            FirmwareInfo newFirmwareInfo = request.exportFirmwareInfo();
            String newFwModelName = newFirmwareInfo.getModelName();
            String oldFwModelName = mCurrentModelName;
            String newFwVersionNumber = newFirmwareInfo.getVersionNumber();
            String oldFwVersionNumber = mCurrentFirmwareVersion;

            // FIXME: except model name of 'shine', is there any other device model need to OTA when oldFwModelName is null?
            if (!CheckUtils.isStringEmpty(newFwModelName)) {
                if (newFwModelName.equals(oldFwModelName)
                        || (CheckUtils.isStringEmpty(oldFwModelName) && newFwModelName.equals(SdkConstants.SHINE_MODEL_NAME))) {
                    if (shouldOTA(newFwVersionNumber, oldFwVersionNumber)) {
                        mNewFirmwareInfo = newFirmwareInfo;
                        if (!isFirmwareExisting(newFwVersionNumber)) {
                            String params[] = null;
                            new DownloadFirmwareTask(newFirmwareInfo, oldFwVersionNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                        }
                        handleOnCheckFirmwareSucceed(true, newFwVersionNumber);
                    } else {
                        handleOnCheckFirmwareSucceed(false, newFwVersionNumber);
                    }
                } else {
                    // different type of device(modelName), should OTA to replace
                    mNewFirmwareInfo = newFirmwareInfo;

                    if (!isFirmwareExisting(newFwVersionNumber)) {
                        String params[] = null;
                        new DownloadFirmwareTask(newFirmwareInfo, oldFwVersionNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                    }
                    handleOnCheckFirmwareSucceed(true, newFwVersionNumber);
                }
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
                handleOnDownloadFirmwareSucceed(newFirmwareInfo.getVersionNumber());
            } else {
                handleOnDownloadFirmwareFailed(-1);  // TODO: define the download error code
            }
        }
    }

    /**
     * download firmware binary file
     * */
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

            if (LocalFileUtils.isFileExist(FIRMWARE_FOLDER, firmwareFileName)) {
                // verify
                String md5 = LocalFileUtils.getMD5String(LocalFileUtils.read(FIRMWARE_FOLDER, firmwareFileName));
                if (checksum.equals(md5)) {
                    return true;
                } else {
                    LocalFileUtils.delete(FIRMWARE_FOLDER, firmwareFileName);
                }
            }
            // Always delete temp file if it exists
            if (LocalFileUtils.isFileExist(FIRMWARE_FOLDER, TEMP_FIRMWARE_EXTENSION)) {
                LocalFileUtils.delete(FIRMWARE_FOLDER, TEMP_FIRMWARE_EXTENSION);
            }

            InputStream input = new BufferedInputStream(connection.getInputStream());
            FileOutputStream output = LocalFileUtils.openFileOutput(FIRMWARE_FOLDER, tempFirmwareFileName);

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

            byte[] fileData = LocalFileUtils.read(FIRMWARE_FOLDER, tempFirmwareFileName);

            if (total > 0) {
                String md5 = LocalFileUtils.getMD5String(fileData);
                if (!md5.equals(checksum)) {
                    Log.d(TAG, "Download succeeds but MD5 verification fails");
                    LocalFileUtils.delete(FIRMWARE_FOLDER, tempFirmwareFileName);
                    return false;
                } else {
                    Log.d(TAG, "Verification succeeds " + firmwareFileName);
                    LocalFileUtils.rename(tempFirmwareFileName, firmwareFileName);
                    return true;
                }
            } else {
                Log.d(TAG, "Download fails");
                LocalFileUtils.delete(FIRMWARE_FOLDER, tempFirmwareFileName);
                return false;
            }
        } catch (Exception e) {
            LocalFileUtils.delete(FIRMWARE_FOLDER, tempFirmwareFileName);
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
        if (LocalFileUtils.delete(FIRMWARE_FOLDER, firmwareName)) {
            Log.d(TAG, String.format("Delete old firmware %s successfully", firmwareVersion));
        } else {
            Log.d(TAG, String.format("Delete old firmware %s error/does't exist", firmwareVersion));
        }
    }

    public static boolean shouldOTA(String newFirmwareVersion, String oldFirmwareVersion) {
        return (!CheckUtils.isStringEmpty(newFirmwareVersion) && !newFirmwareVersion.equals(oldFirmwareVersion));
    }

    /* utility API */
    public static String getFirmwareFileName(String firmwareVersion) {
        return String.format("%s.%s", firmwareVersion, FIRMWARE_EXTENSION);
    }

    public static String getTempFirmwareFileName(String firmwareVersion) {
        return String.format("%s.%s", firmwareVersion, TEMP_FIRMWARE_EXTENSION);
    }

    public void subscribeFirmwareDownload(DownloadFirmwareListener downloadListener) {
        if (downloadListener != null) {
            mDownloadFirmwareListeners.add(downloadListener);
        }
    }

    public void unsubscribeFirmwareDownload(DownloadFirmwareListener downloadListener) {
        mDownloadFirmwareListeners.remove(downloadListener);
    }

    public void clearDownloadFirmwareListeners() {
        mDownloadFirmwareListeners.clear();
    }

    public void subscribeFirmwareCheck(CheckFirmwareServerListener checkFirmwareListener) {
        if (checkFirmwareListener != null) {
            mCheckFirmwareListeners.add(checkFirmwareListener);
        }
    }

    public void unsubscribeFirmwareCheck(CheckFirmwareServerListener checkListener) {
        mCheckFirmwareListeners.remove(checkListener);
    }

    public void clearCheckFirmwareListener() {
        mCheckFirmwareListeners.clear();
    }

    /* invoke GetLatestFirmwareListener callback */
    private void handleOnCheckFirmwareSucceed(boolean shouldOta, String fwVersionNumber) {
        for (CheckFirmwareServerListener listener : mCheckFirmwareListeners) {
            listener.onSucceed(shouldOta, fwVersionNumber);
        }
    }

    private void handleOnCheckFirmwareFailed(VolleyError volleyError) {
        if (volleyError != null && volleyError.networkResponse != null) {
            mErrorNetworkStatus = volleyError.networkResponse.statusCode;
        }

        for (CheckFirmwareServerListener listener : mCheckFirmwareListeners) {
            listener.onFailed(mErrorNetworkStatus);
        }
    }

    /* invoke DownloadFirmwareListener callback */
    private void handleOnDownloadFirmwareSucceed(String firmwareVersion) {
        for (DownloadFirmwareListener listener : mDownloadFirmwareListeners) {
            listener.onSucceed(getFirmwareFileName(firmwareVersion));
        }
    }

    private void handleOnDownloadFirmwareFailed(int errorCode) {
        for (DownloadFirmwareListener listener : mDownloadFirmwareListeners) {
            listener.onFailed(errorCode);
        }
    }

    /**
     * reset working status to NotStarted when this sync() completes
     * */
    public void resetWorkStatus() {
        mWorkingStatus.set(STATUS_NOT_STARTED);
        mNewFirmwareInfo = null;
        mErrorNetworkStatus = 0;
    }
}