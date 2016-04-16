package com.misfit.syncdemo.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {

    public static String readFile(String path) throws IOException {
        InputStream inputStream = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        while (reader.ready()) {
            String lineStr = reader.readLine();
            builder.append(lineStr);
        }
        return builder.toString();
    }
}