package com.bmkg.retrofit.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationState {
    private static final String PREFS_NAME = "NotificationStatePrefs";

    // Menyimpan kondisi cuaca terakhir yang dinotifikasikan untuk sebuah aturan
    public static void saveLastNotifiedCondition(Context context, String ruleId, String condition) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(ruleId, condition).apply();
    }

    // Mengambil kondisi cuaca terakhir yang dinotifikasikan
    public static String getLastNotifiedCondition(Context context, String ruleId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(ruleId, null); // Default null jika belum pernah ada
    }
}