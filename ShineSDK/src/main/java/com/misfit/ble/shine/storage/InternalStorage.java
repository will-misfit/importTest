package com.misfit.ble.shine.storage;

import android.content.Context;

import com.misfit.ble.sdk.GlobalVars;
import com.misfit.ble.util.Helper;

import java.io.File;

public class InternalStorage {
	
	public static boolean saveBinaryDataToFile(byte[] data, String directoryName, String filename) {
		Context context = GlobalVars.getApplicationContext();
		if (context == null)
			return false;
		
		File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
		File file = new File(directory, filename);
		
		return Helper.saveBinaryDataToFile(data, file);
	}
	
	public static boolean saveTextToFile(String text, String directoryName, String filename) {
		Context context = GlobalVars.getApplicationContext();
		if (context == null)
			return false;
		
		File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
		File file = new File(directory, filename);
		
		return Helper.saveTextToFile(text, file);
	}
	
	public static File[] getFiles(String directoryName) {
		Context context = GlobalVars.getApplicationContext();
		if (context == null)
			return null;
		
		File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
		File[] files = directory.listFiles();
		return files;
	}

	public static boolean saveTextToCacheFile(String text, String filename) {
		Context context = GlobalVars.getApplicationContext();
		if (context == null)
			return false;

		File directory = context.getCacheDir();
		File file = new File(directory, filename);
		return Helper.saveTextToFile(text, file);
	}

	public static String readTextFromCacheFile(String filename) {
		Context context = GlobalVars.getApplicationContext();
		if (context == null)
			return null;

		File directory = context.getCacheDir();
		File file = new File(directory, filename);
		return Helper.readTextFile(file);
	}
}
