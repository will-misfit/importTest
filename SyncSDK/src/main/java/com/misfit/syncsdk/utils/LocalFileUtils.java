package com.misfit.syncsdk.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Util class to process local file read/write
 */
public class LocalFileUtils {

    public static Context getContext() {
        return ContextUtils.getInstance().getContext();
    }

    public static boolean write(String fileName, String content) {
        try {
            FileOutputStream fos = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static FileOutputStream getOutputStream(String fileName) {
        try {
            FileOutputStream fos = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            return fos;
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    
    public static FileOutputStream getOutputStreamForAppend(String fileName) {
        try {
            FileOutputStream fos = getContext().openFileOutput(fileName, Context.MODE_APPEND);
            return fos;
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    
    public static byte[] read(String fileName) {
        try {
            final long fileSize = fileSize(fileName);    
            if (fileSize <= 0) {
                return null;
            }

            FileInputStream fis = getContext().openFileInput(fileName);
            byte[] data = new byte[(int) fileSize];
            fis.read(data);
            fis.close();
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean isFileExist(String fileName){
        File file = getContext().getFileStreamPath(fileName);
        return file.exists();
    }
    
    public static long fileSize(String fileName) {
        File file = getContext().getFileStreamPath(fileName);
        if (file.isFile() && file.exists()) {
            return file.length();
        }
        return -1;
    }
    
    public static boolean delete(String fileName) {
        boolean result = getContext().deleteFile(fileName);
        return result;
    }
    
    public static boolean rename(String oldFileName, String newFileName) {
        return getContext().getFileStreamPath(oldFileName).renameTo(
                getContext().getFileStreamPath(newFileName));
    }
    
    public static String getMD5String(byte[] inputData) {
        if (inputData == null || inputData.length == 0) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytesOfHash = md.digest(inputData);
            StringBuilder sb = new StringBuilder();
            for (byte b : bytesOfHash) {
                sb.append(String.format("%02x", b));
            }
            // Convert md5 bytes to string
            // http://stackoverflow.com/questions/3752981/convert-md5-array-to-string-java
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
