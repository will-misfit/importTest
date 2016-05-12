package com.misfit.ble.shine.log;

import android.bluetooth.BluetoothDevice;

import com.misfit.ble.shine.ShineDevice;
import com.misfit.ble.util.Convertor;
import com.misfit.ble.shine.log.LogEventItem.RequestStartedLog;
import com.misfit.ble.shine.log.LogEventItem.RequestFinishedLog;
import com.misfit.ble.shine.log.LogEventItem.ResponseStartedLog;
import com.misfit.ble.shine.log.LogEventItem.ResponseFinishedLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bruyu on 4/12/15.
 * a class with static helper methods to facilitate log session process
 */
public class LogUtilHelper {

    public enum LogEventItemPosition { RequestStarted, RequestFinished, ResponseStarted, ResponseFinished }

    public static void appendScanMisfitDevice(LogEventItem logEventItem,
                                              String deviceName,
                                              String macAddr,
                                              String serialNumber,
                                              byte[] scanRecord) {
        JSONObject json = getMisfitDeviceJson(deviceName, macAddr, serialNumber, scanRecord);
        if (json != null && logEventItem != null) {
            logEventItem.mResponseFinishedLog = new LogEventItem.ResponseFinishedLog(0, json);
        }
    }

    public static JSONObject getMisfitDeviceJson(String deviceName, String address, String serialNumber, byte[] scanRecord) {
        try {
            JSONObject json = new JSONObject();
            json.put("deviceName", deviceName);
            json.put("address", address);
            json.put("serialNumber", serialNumber);
            json.put("data", Convertor.bytesToString(scanRecord));
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void appendScanRawResult(LogEventItem logEventItem,
                                           String deviceName,
                                           String address,
                                           int rssi,
                                           String serialNumber,
                                           LogEventItemPosition itemPosition) {
        JSONObject json = LogUtilHelper.getRawDeviceJson(deviceName, address, rssi, serialNumber);
        if (json != null) {
            switch(itemPosition) {
                case RequestStarted:
                    logEventItem.mRequestStartedLog = new RequestStartedLog(json);
                    break;
                case RequestFinished:
                    logEventItem.mRequestFinishedLog = new RequestFinishedLog(0, json);
                    break;
                case ResponseStarted:
                    logEventItem.mResponseStartedLog = new ResponseStartedLog(0, json);
                    break;
                case ResponseFinished:
                    logEventItem.mResponseFinishedLog = new ResponseFinishedLog(0, json);
                    break;
                default:
                    break;
            }
        }
    }

    public static JSONObject getRawDeviceJson(String deviceName, String address, int rssi, String serialNumber) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", deviceName);
            json.put("address", address);
            json.put("rssi", rssi);
            json.put("serialNumber", serialNumber);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LogEventItem makeConnectedDeviceEventItem(ShineDevice device, LogEventItemPosition eventItemPosition) {
        String deviceName = device.getName();
        String macAddr = device.getAddress();
        String serialNum = device.getSerialNumber();

        LogEventItem logEventItem = new LogEventItem(LogEventItem.EVENT_CONNECTED_DEVICE);
        appendScanRawResult(logEventItem, deviceName, macAddr, 0, serialNum, eventItemPosition);
        return logEventItem;
    }

    public static LogEventItem makeConnectedBluetoothDeviceEventItem(BluetoothDevice device, LogEventItemPosition eventItemPosition) {
        String deviceName = device.getName();
        String macAddr = device.getAddress();

        LogEventItem logEventItem = new LogEventItem(LogEventItem.EVENT_CONNECTED_BLUETOOTH_DEVICE);
        appendScanRawResult(logEventItem, deviceName, macAddr, 0, "", eventItemPosition);
        return logEventItem;
    }

    public static LogEventItem makeScanResultEventItem(String deviceName, String macAddr, String serialNum, byte[] scanRecord) {
        LogEventItem logEventItem = new LogEventItem(LogEventItem.EVENT_SCAN_RESULT);
        appendScanMisfitDevice(logEventItem, deviceName, macAddr, serialNum, scanRecord);
        return logEventItem;
    }
}