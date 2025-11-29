package com.bmkg.retrofit;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bmkg.retrofit.api.ApiClient;
import com.bmkg.retrofit.api.ApiService;
import com.bmkg.retrofit.model.CuacaItem;
import com.bmkg.retrofit.model.DataItem;
import com.bmkg.retrofit.model.NotificationRule;
import com.bmkg.retrofit.model.ResponseData;
import com.bmkg.retrofit.utils.NotificationHelper;
import com.bmkg.retrofit.utils.NotificationState; // <-- IMPORT HELPER BARU
import com.bmkg.retrofit.utils.RuleManager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;

public class WeatherCheckWorker extends Worker {

    private static final String TAG = "WeatherCheckWorker";
    private final Context context;

    public WeatherCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker sedang berjalan...");
        List<NotificationRule> rules = RuleManager.getRules(context);
        for (NotificationRule rule : rules) {
            if (rule.isEnabled()) {
                checkWeatherForRule(rule);
            }
        }
        return Result.success();
    }

    private void checkWeatherForRule(NotificationRule rule) {
        // Cek dulu apakah sudah waktunya untuk memeriksa aturan ini
        if (!isWithinCheckingWindow(rule)) {
            return; // Jika belum, lewati aturan ini
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        try {
            Response<ResponseData> response = api.getWeatherByAdm4(rule.getLocationCode()).execute();

            if (response.isSuccessful() && response.body() != null && !response.body().data.isEmpty()) {
                DataItem item = response.body().data.get(0);
                if (item.cuaca == null || item.cuaca.isEmpty() || item.cuaca.get(0).isEmpty()) {
                    Log.w(TAG, "Data cuaca kosong untuk lokasi: " + rule.getLocationName());
                    return;
                }

                CuacaItem currentForecast = item.cuaca.get(0).get(0);
                String currentCondition = currentForecast.weather_desc;
                String forecastHour = currentForecast.getHour();
                String lastNotifiedCondition = NotificationState.getLastNotifiedCondition(context, rule.getId());

                Log.d(TAG, "Pengecekan untuk " + rule.getLocationName() + ": Aturan=" + rule.getWeatherCondition() +
                        " | Prakiraan=" + currentCondition + " @ " + forecastHour +
                        " | Notif Terakhir=" + lastNotifiedCondition);

                // ==========================================================
                //          LOGIKA NOTIFIKASI BARU
                // ==========================================================

                // Kondisi 1: Cuaca cocok dengan aturan DAN belum pernah ada notifikasi / notifikasi terakhir berbeda
                if (currentCondition.equalsIgnoreCase(rule.getWeatherCondition())) {
                    if (lastNotifiedCondition == null || !lastNotifiedCondition.equals(currentCondition)) {
                        sendNotification(rule, currentForecast, "Peringatan Cuaca");
                    }
                }

                // Kondisi 2: Cuaca TIDAK cocok lagi dengan aturan, TAPI notifikasi terakhir cocok
                // Ini berarti ada perubahan cuaca dari yang diharapkan.
                else if (lastNotifiedCondition != null && lastNotifiedCondition.equalsIgnoreCase(rule.getWeatherCondition())) {
                    sendNotification(rule, currentForecast, "Pembaruan Cuaca");
                }

            } else {
                Log.e(TAG, "Panggilan API gagal untuk: " + rule.getLocationCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saat menjalankan panggilan API sinkron", e);
        }
    }

    // Fungsi baru untuk mengirim notifikasi dan menyimpan status
    private void sendNotification(NotificationRule rule, CuacaItem forecast, String type) {
        String title = type + ": " + forecast.weather_desc;
        String message = "Prakiraan cuaca untuk " + rule.getLocationName() + " pada pukul " + forecast.getHour() +
                " adalah " + forecast.weather_desc + ".";

        NotificationHelper.showNotification(context, title, message);
        NotificationState.saveLastNotifiedCondition(context, rule.getId(), forecast.weather_desc); // Simpan status terakhir
        Log.i(TAG, "NOTIFIKASI (" + type + ") DIKIRIM untuk aturan: " + rule.getId() + " | Kondisi: " + forecast.weather_desc);
    }

    // Fungsi baru untuk memeriksa apakah saat ini berada dalam rentang waktu pengecekan
    private boolean isWithinCheckingWindow(NotificationRule rule) {
        if (rule.getNotificationTime() == null || rule.getNotificationTime().isEmpty()) {
            return true; // Jika tidak ada waktu, selalu periksa
        }

        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date ruleTime = timeFormat.parse(rule.getNotificationTime());

            Calendar now = Calendar.getInstance();
            Date currentTime = now.getTime();

            // Atur waktu mulai pengecekan: 1 jam sebelum waktu aturan
            Calendar startTime = Calendar.getInstance();
            startTime.setTime(ruleTime);
            startTime.add(Calendar.HOUR_OF_DAY, -1);

            // Atur waktu akhir pengecekan: sedikit setelah waktu aturan (misal, 15 menit)
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(ruleTime);
            endTime.add(Calendar.MINUTE, 15);

            // Cek apakah waktu saat ini berada di antara waktu mulai dan waktu akhir
            // Ini hanya membandingkan jam dan menit, bukan tanggal
            Calendar targetTime = Calendar.getInstance();
            targetTime.setTime(ruleTime);
            now.set(Calendar.YEAR, targetTime.get(Calendar.YEAR));
            now.set(Calendar.MONTH, targetTime.get(Calendar.MONTH));
            now.set(Calendar.DAY_OF_MONTH, targetTime.get(Calendar.DAY_OF_MONTH));

            boolean isInWindow = now.after(startTime) && now.before(endTime);

            if (!isInWindow) {
                Log.v(TAG, "Di luar jendela pengecekan untuk " + rule.getLocationName() +
                        ". Jendela: " + timeFormat.format(startTime.getTime()) + " - " + timeFormat.format(endTime.getTime()) +
                        ". Saat Ini: " + timeFormat.format(currentTime));
            }

            return isInWindow;

        } catch (ParseException e) {
            Log.e(TAG, "Gagal mem-parsing waktu aturan: " + rule.getNotificationTime(), e);
            return false;
        }
    }
}