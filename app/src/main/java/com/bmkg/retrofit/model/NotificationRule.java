package com.bmkg.retrofit.model;

import java.io.Serializable;

public class NotificationRule implements Serializable {
    private String id;
    private String locationCode;
    private String locationName;
    private String weatherCondition;
    private String notificationTime;
    private String notificationDate; // <-- FIELD BARU untuk menyimpan tanggal (format "yyyy-MM-dd")
    private boolean isEnabled;

    // --- Konstruktor diperbarui untuk menyertakan tanggal ---
    public NotificationRule(String id, String locationCode, String locationName, String weatherCondition, String notificationTime, String notificationDate, boolean isEnabled) {
        this.id = id;
        this.locationCode = locationCode;
        this.locationName = locationName;
        this.weatherCondition = weatherCondition;
        this.notificationTime = notificationTime;
        this.notificationDate = notificationDate; // <-- INISIALISASI FIELD BARU
        this.isEnabled = isEnabled;
    }

    // Getter untuk semua field
    public String getId() { return id; }
    public String getLocationCode() { return locationCode; }
    public String getLocationName() { return locationName; }
    public String getWeatherCondition() { return weatherCondition; }
    public String getNotificationTime() { return notificationTime; }
    public String getNotificationDate() { return notificationDate; } // <-- GETTER BARU
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
}