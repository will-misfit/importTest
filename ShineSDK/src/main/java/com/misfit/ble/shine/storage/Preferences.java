package com.misfit.ble.shine.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.misfit.ble.sdk.GlobalVars;

public class Preferences {
	
	public static SharedPreferences getSharedPreferences(String name) {
		Context context = GlobalVars.getApplicationContext();
		if (context == null)
			return null;
		return context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}
	
}
