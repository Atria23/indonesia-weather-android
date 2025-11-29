// File: com/bmkg/retrofit/utils/NotificationHelper.java
package com.bmkg.retrofit.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.bmkg.retrofit.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "WeatherAlertChannel";
    private static final String CHANNEL_NAME = "Peringatan Cuaca";

    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Membuat channel notifikasi (wajib untuk Android 8.0 Oreo ke atas)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel untuk notifikasi peringatan cuaca");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // --- INI PERUBAHANNYA ---
        // Kita ganti dari ic_launcher_foreground menjadi ic_notification yang baru kita buat.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24) // <-- Ikon yang benar
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Agar teks panjang bisa dibaca
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Notifikasi hilang saat di-klik

        if (notificationManager != null) {
            // Gunakan ID unik agar notifikasi bisa di-update jika perlu,
            // atau gunakan System.currentTimeMillis() untuk menampilkan notifikasi baru setiap saat.
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}