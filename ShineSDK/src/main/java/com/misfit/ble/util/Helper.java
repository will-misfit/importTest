package com.misfit.ble.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Helper {
	
	private static String readRawFile(String path) {
		InputStream in = Helper.class.getClassLoader().getResourceAsStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		StringBuffer textBuffer = new StringBuffer(); 
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				textBuffer.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (textBuffer.length() > 0 && textBuffer.charAt(textBuffer.length() - 1) == '\n') {
			textBuffer.deleteCharAt(textBuffer.length() - 1);
		}
		return textBuffer.toString();
	}
	
	public static String readTextFile(File file) {
		if (file == null || !file.exists())
			return null;

		StringBuffer stringBuffer = new StringBuffer();
		
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			
			char[] chars = new char[1024];
			int length = 0;
			while ((length = reader.read(chars)) != -1) {
				stringBuffer.append(chars, 0, length);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return stringBuffer.toString();
	}
	
	public static boolean saveTextToFile(String text, File file) {
		boolean result = true;
		
		OutputStreamWriter writer = null;
		try {
			if (file.exists() == false) {
				file.createNewFile();
			}
			writer = new OutputStreamWriter(new FileOutputStream(file));
			writer.write(text);
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
					result = false;
				}
			}
		}
		return result;
	}
	
	public static boolean saveBinaryDataToFile(byte[] data, File file) {
		boolean result = true;
		
		FileOutputStream outStream = null;
		try {
			if (file.exists() == false) {
				file.createNewFile();
			}
			outStream = new FileOutputStream(file);
			outStream.write(data);
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					result = false;
				}
			}
		}
		return result;
	}
	
	public static String getDeviceName() {
		String manufacturer = getDeviceManufacturer();
		String model = getDeviceModel();
		return manufacturer + "_" + model;
	}
	
	public static String getDeviceManufacturer() {
		String manufacturer = Build.MANUFACTURER;
		manufacturer = (manufacturer != null) ? manufacturer : "unknown";
		return manufacturer;
	}
	
	public static String getDeviceModel() {
		String model = Build.MODEL;
		model = (model != null) ? model : "unknown";
		return model;
	}
	
	public static boolean isInternetConnectionAvailable(Context context) {
		if (context == null)
			return false;
		
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}

		NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}

		return false;
	}
}
