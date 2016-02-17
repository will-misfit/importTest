package com.misfit.syncsdk.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class to process local file read/write
 */
public class LocalFileUtils {

    public static Context getContext() {
        return ContextUtils.getInstance().getContext();
    }

    public static FileOutputStream openFileOutput(String dirName, String fileName) {
        return openFileOutput(dirName, fileName, Context.MODE_PRIVATE);
    }
    
    public static FileOutputStream openFileOutput(String dirName, String fileName, int mode) {
        try {
            File directory = getContext().getDir(dirName, mode);
            File file = new File(directory, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            return fos;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static FileInputStream openFileInput(String dirName, String fileName) {
        try {
            File directory = getContext().getDir(dirName, Context.MODE_PRIVATE);
            File file = new File(directory, fileName);
            if (!file.exists()) {
                return null;
            }
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static byte[] read(String dirName, String fileName) {
        FileInputStream fis = null;
        try {
            final long fileSize = fileSize(dirName, fileName);
            if (fileSize <= 0) {
                return null;
            }

            fis = openFileInput(dirName, fileName);
            byte[] data = new byte[(int) fileSize];
            fis.read(data);
            fis.close();
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    public static boolean isFileExist(String dirName, String fileName){
        File directory = getContext().getDir(dirName, Context.MODE_PRIVATE);
        File file = new File(directory, fileName);
        return file.exists();
    }
    
    public static long fileSize(String dirName, String fileName) {
        File directory = getContext().getDir(dirName, Context.MODE_PRIVATE);
        File file = new File(directory, fileName);
        if (file.isFile() && file.exists()) {
            return file.length();
        }
        return -1;
    }
    
    public static boolean delete(String dirName, String fileName) {
        File directory = getContext().getDir(dirName, Context.MODE_PRIVATE);
        File file = new File(directory, fileName);
        if (file.exists()) {
            return file.delete();
        }
        return true;
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

    public static File[] getFiles(String directoryName) {
        File directory = ContextUtils.getInstance().getContext().getDir(directoryName, Context.MODE_PRIVATE);
        return directory.exists() ? directory.listFiles() : null;
    }
}
