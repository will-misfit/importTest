package com.misfit.ble.sample.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    private final static String CONFIG_NAME = "config";

    public static SharedPreferences getSharedPreferences(Context ctx, String name) {
        return ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void writeConfig(Context ctx, String key, String val) {
        write(ctx, CONFIG_NAME, key, val);
    }

    public static void writeConfig(Context ctx, String key, long val) {
        write(ctx, CONFIG_NAME, key, val);
    }

    public static void writeConfig(Context ctx, String key, int val) {
        write(ctx, CONFIG_NAME, key, val);
    }

    public static void writeConfig(Context ctx, String key, boolean val) {
        write(ctx, CONFIG_NAME, key, val);
    }

    public static String readConfig(Context ctx, String key, String def) {
        return read(ctx, CONFIG_NAME, key, def);
    }

    public static long readConfig(Context ctx, String key, long def) {
        return read(ctx, CONFIG_NAME, key, def);
    }

    public static int readConfig(Context ctx, String key, int def) {
        return read(ctx, CONFIG_NAME, key, def);
    }

    public static boolean readConfig(Context ctx, String key, boolean def) {
        return read(ctx, CONFIG_NAME, key, def);
    }

    public static void clear(Context ctx, String name) {
        getSharedPreferences(ctx, name).edit().clear().commit();
    }

    public static void write(Context ctx, String name, String key, String val) {
        getSharedPreferences(ctx, name).edit().putString(key, val).commit();
    }

    public static void write(Context ctx, String name, String key, boolean val) {
        getSharedPreferences(ctx, name).edit().putBoolean(key, val).commit();
    }

    public static void write(Context ctx, String name, String key, int val) {
        getSharedPreferences(ctx, name).edit().putInt(key, val).commit();
    }

    public static void write(Context ctx, String name, String key, long val) {
        getSharedPreferences(ctx, name).edit().putLong(key, val).commit();
    }

    public static String read(Context ctx, String name, String key, String def) {
        return getSharedPreferences(ctx, name).getString(key, def);
    }

    public static boolean read(Context ctx, String name, String key, boolean def) {
        return getSharedPreferences(ctx, name).getBoolean(key, def);
    }

    public static int read(Context ctx, String name, String key, int def) {
        return getSharedPreferences(ctx, name).getInt(key, def);
    }

    public static long read(Context ctx, String name, String key, long def) {
        return getSharedPreferences(ctx, name).getLong(key, def);
    }
}
