package com.bmkg.retrofit.model;

public class DailyForecast {
    private String dateLabel;      // contoh: "23 Nov"
    private int maxTemp;           // suhu maksimum
    private int minTemp;           // suhu minimum
    private String description;    // "Cerah Berawan"
    private String iconUrl;        // URL SVG dari BMKG
    private int rainChance;        // % peluang hujan (perkiraan sederhana)

    public DailyForecast(String dateLabel, int maxTemp, int minTemp,
                         String description, String iconUrl, int rainChance) {
        this.dateLabel = dateLabel;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.description = description;
        this.iconUrl = iconUrl;
        this.rainChance = rainChance;
    }

    public String getDateLabel() { return dateLabel; }
    public int getMaxTemp() { return maxTemp; }
    public int getMinTemp() { return minTemp; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public int getRainChance() { return rainChance; }
}
