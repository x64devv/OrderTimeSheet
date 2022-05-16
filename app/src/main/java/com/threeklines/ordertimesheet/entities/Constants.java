package com.threeklines.ordertimesheet.entities;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import com.threeklines.ordertimesheet.R;

import java.security.SecureRandom;
import java.util.ArrayList;

public class Constants {
    public static final SecureRandom SECURE_RANDOM= new SecureRandom();
    public static final byte[] SALT = new byte[16];
    public static final ArrayList<String> OPTIONS =  new ArrayList<>();

    {
        Constants.SECURE_RANDOM.nextBytes(SALT);
        OPTIONS.add("AM Tea");
        OPTIONS.add("Lunch");
        OPTIONS.add("PM Tea");
        OPTIONS.add("Reassignment");
        OPTIONS.add("Other");
    }
    private static final String PREFS_NAME = "TIMESHEET";


    public static boolean setCurrentOrderState(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean setLastOrder(Context context, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("latest_order", value);
        return editor.commit();
    }

    public static boolean setLastOrder(Context context, long value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("last_update", value);
        return editor.commit();
    }

    public static boolean setLastCompleted(Context context, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("latest_completed", value);
        return editor.commit();
    }

    public static boolean getCurrentOrderState(Context context, String key){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(key, false);
    }

    public static String getLastOrder(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getString("latest_order", "zero");
    }
    public static long getLastUpdate(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getLong("last_update", 0);
    }
    public static String getLastCompleted(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getString("latest_completed", "zero");
    }

    public static boolean setBreakStart(Context context, String key, long value, int reason) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key + "_start_break", value);
        editor.putInt(key + "_reason", reason);
        return editor.commit();
    }

    public static Long getBreakStart(Context context, String key){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getLong(key + "_start_break", -1);
    }


    public static boolean setProcessStart(Context context, String key, long value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key + "_start_process", value);
        return editor.commit();
    }

    public static Long getProcessStart(Context context, String key){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getLong(key + "_start_process", -1);
    }
    public static int getReason(Context context, String key){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key + "_reason", -1);
    }

    public static boolean deletePref(Context context, String key){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        return editor.commit();
    }


}
