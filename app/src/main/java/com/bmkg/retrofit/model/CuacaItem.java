package com.bmkg.retrofit.model;

public class CuacaItem {
    public String datetime;
    public double t;            // Suhu (bisa desimal)
    public double tcc;          // Tutupan Awan (Total Cloud Cover)
    public double tp;           // Presipitasi (Precipitation)
    public double hu;           // Kelembapan (Humidity)
    public double ws;           // Kecepatan Angin (Wind Speed)
    public String weather_desc;
    public String weather_desc_en;
    public String image;
    public String local_datetime;
    public String vs_text;      // Jarak Pandang (Visibility Text)
    public String wd;           // Arah Angin (Wind Direction)

    // Metode helper untuk mendapatkan jam dari local_datetime
    public String getHour() {
        if (local_datetime != null && local_datetime.length() >= 16) {
            // contoh: "2025-11-23 12:00:00" -> "12:00"
            return local_datetime.substring(11, 16);
        }
        return "N/A";
    }
}