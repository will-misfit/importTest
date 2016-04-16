package com.misfit.syncdemo.reproduce;

import com.misfit.syncsdk.model.SdkResourceSettings;
import com.misfit.syncsdk.utils.MLog;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SettingsParser {
    private final static String TAG = "SettingsParser";

    public static List<SdkResourceSettings> parse(String path) throws IOException {
        List<SdkResourceSettings> settings = new ArrayList<>();
        MLog.i(TAG, "read settings, path=" + path);
        InputStream inputStream = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        while (reader.ready()) {
            String lineStr = reader.readLine();
            settings.add(parseSettingFromString(lineStr));
        }
        MLog.i(TAG, "settings size=" + settings.size());
        return settings;
    }

    private static SdkResourceSettings parseSettingFromString(String settingStr) {
        String[] properties = settingStr.split("\t");
        long timestamp = Long.valueOf(properties[0]);
        int timezoneOffset = Integer.valueOf(properties[1]);
        int tripleTapType = Integer.valueOf(properties[2]);
        boolean isAutoSleep = Boolean.valueOf(properties[3]);

        return new SdkResourceSettings(timestamp, isAutoSleep, tripleTapType, timezoneOffset);
    }
}
