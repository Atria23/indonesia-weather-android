package com.bmkg.retrofit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bmkg.retrofit.model.DailyForecast;
import java.util.List;
import java.util.Locale;

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {

    private final List<DailyForecast> dailyForecasts;
    private final Context context;
    private int textColor;

    public DailyForecastAdapter(List<DailyForecast> dailyForecasts, Context context) {
        this.dailyForecasts = dailyForecasts;
        this.context = context;
        this.textColor = ContextCompat.getColor(context, R.color.text_on_light_bg);
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_forecast_vertical, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyForecast forecast = dailyForecasts.get(position);

        holder.tvDayName.setText(forecast.getDateLabel());
        holder.tvTempMax.setText(String.format(Locale.getDefault(), "%d°", forecast.getMaxTemp()));
        holder.tvTempMin.setText(String.format(Locale.getDefault(), "%d°", forecast.getMinTemp()));
        holder.ivIcon.setImageResource(getWeatherIconResource(forecast.getDescription()));

        // ======================================================
        // PERUBAHAN UTAMA: TAMBAHKAN BARIS INI
        // Menerapkan tint ke ikon agar warnanya sama dengan teks
        // ======================================================
        holder.ivIcon.setColorFilter(textColor);

        // Terapkan warna teks yang sudah di-set dari Activity
        holder.tvDayName.setTextColor(textColor);
        holder.tvTempMax.setTextColor(textColor);
        holder.tvTempMin.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return dailyForecasts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvTempMax, tvTempMin;
        ImageView ivIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvTempMax = itemView.findViewById(R.id.tvTempMax);
            tvTempMin = itemView.findViewById(R.id.tvTempMin);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }

    private int getWeatherIconResource(String weatherDescription) {
        String desc = weatherDescription.toLowerCase();
        if (desc.contains("hujan") || desc.contains("rain")) return R.drawable.rainy_icon;
        if (desc.contains("badai") || desc.contains("storm")) return R.drawable.stormy_icon;
        if (desc.contains("berawan") || desc.contains("cloudy")) return R.drawable.cloudy_icon;
        return R.drawable.sunny_icon;
    }
}