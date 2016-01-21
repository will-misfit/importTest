package com.misfit.ble.sample.utils.logger;

import android.os.Environment;

import com.misfit.ble.sample.BuildConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * output log in file
 */
public class FileLogger implements MFLog.LogNode {

    private final static String LOG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/" + BuildConfig.APPLICATION_ID + "/MFLog";

    private static String logName;
    private static boolean useCustomLogName;
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static void writeFile(String type, String tag, String content) {
        String logName = getLogName();
        StringBuilder builder = new StringBuilder();
        builder.append(getyyyy_MM_dd_HHmmssllll())
                .append("  ")
                .append(type)
                .append("/")
                .append(tag)
                .append("：")
                .append(content)
                .append("\r\n");
        writeToFile(LOG_DIR, logName, builder.toString(), true);
    }

    private static String getLogName() {
        if (useCustomLogName) {
            return logName + ".txt";
        } else {
            Calendar calendar = Calendar.getInstance();
            return getyyyy_MM_dd(calendar) + "_log.txt";
        }
    }

    public void setCustomLogName(String newName) {
        useCustomLogName = newName != null && !"".equals(newName);
        logName = newName;
    }

    private static String getyyyy_MM_dd_HHmmssllll() {
        return timeFormatter.format(new Date(System.currentTimeMillis()));
    }

    private static String getyyyy_MM_dd_HHmmss(Calendar calendar) {

        return getyyyy_MM_dd(calendar) + " " + getHHmmss(calendar);
    }

    private static String getHHmmss(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND);
    }

    private static String getyyyy_MM_dd(Calendar calendar) {
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dateString = calendar.get(Calendar.YEAR) + "-";
        if (month < 10) {
            dateString = dateString + "0";
        }
        dateString = dateString + month + "-";
        if (day < 10) {
            dateString = dateString + "0";
        }
        dateString = dateString + day;
        return dateString;
    }

    private static boolean writeToFile(String path, String fileName, String content, boolean isAppend) {
        File dir = new File(path);
        File file = new File(path + "/" + fileName);
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                return false;
            }
        }

        if (!file.isFile()) {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }

        try {
            FileWriter fileWriter = new FileWriter(file, isAppend);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void log(int priority, String tag, String content) {
        String priorityStr = MFLog.getPriorityStr(priority);
        writeFile(priorityStr, tag, content);
    }
}
