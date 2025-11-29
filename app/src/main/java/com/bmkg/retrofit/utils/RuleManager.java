package com.bmkg.retrofit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.bmkg.retrofit.model.NotificationRule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RuleManager {
    private static final String PREFS_NAME = "NotificationRulesPrefs";
    private static final String KEY_RULES = "rules";
    private static SharedPreferences sharedPreferences;
    private static final Gson gson = new Gson(); // Dijadikan final karena tidak akan diubah

    private static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void saveRules(Context context, List<NotificationRule> rules) {
        init(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(rules);
        editor.putString(KEY_RULES, json);
        editor.apply();
    }

    public static List<NotificationRule> getRules(Context context) {
        init(context);
        String json = sharedPreferences.getString(KEY_RULES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<NotificationRule>>() {}.getType();
        List<NotificationRule> rules = gson.fromJson(json, type);
        // Mengatasi kasus jika deserialisasi menghasilkan null
        return rules != null ? rules : new ArrayList<>();
    }

    // ==========================================================
    // ===== METODE BARU YANG DIPERLUKAN UNTUK MENGATASI ERROR ====
    // ==========================================================
    /**
     * Memperbarui satu aturan spesifik di dalam daftar yang tersimpan.
     * @param context Context aplikasi.
     * @param ruleToUpdate Objek aturan dengan data yang sudah diperbarui.
     */
    public static void updateRule(Context context, NotificationRule ruleToUpdate) {
        // 1. Ambil semua aturan yang ada saat ini
        List<NotificationRule> currentRules = getRules(context);
        boolean ruleFound = false;

        // 2. Cari aturan yang cocok berdasarkan ID dan perbarui
        for (int i = 0; i < currentRules.size(); i++) {
            if (currentRules.get(i).getId().equals(ruleToUpdate.getId())) {
                currentRules.set(i, ruleToUpdate); // Ganti aturan lama dengan yang baru
                ruleFound = true;
                break; // Keluar dari loop setelah aturan ditemukan
            }
        }

        // 3. Jika aturan ditemukan, simpan kembali seluruh daftar
        if (ruleFound) {
            saveRules(context, currentRules);
        }
    }

    public static NotificationRule getRuleById(Context context, String ruleId) {
        if (ruleId == null || ruleId.isEmpty()) {
            return null;
        }
        List<NotificationRule> allRules = getRules(context);
        for (NotificationRule rule : allRules) {
            if (rule.getId().equals(ruleId)) {
                return rule;
            }
        }
        return null; // Tidak ditemukan
    }
}