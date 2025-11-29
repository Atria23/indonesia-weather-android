// File: com/bmkg/retrofit/WeatherHourAdapter.java
package com.bmkg.retrofit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bmkg.retrofit.model.CuacaItem;
import com.bumptech.glide.Glide; // Menggunakan Glide lebih direkomendasikan daripada setImageResource manual

import java.util.List;
import java.util.Locale;

public class WeatherHourAdapter extends RecyclerView.Adapter<WeatherHourAdapter.ViewHolder> {

    private final List<CuacaItem> hourlyForecasts;
    private final Context context;
    private int textColor;

    public WeatherHourAdapter(List<CuacaItem> hourlyForecasts, Context context) {
        this.hourlyForecasts = hourlyForecasts;
        this.context = context;
        this.textColor = ContextCompat.getColor(context, R.color.text_on_dark_bg); // Warna default
    }

    public void setTextColor(int color) {
        this.textColor = color;
        notifyDataSetChanged(); // Panggil notifyDataSetChanged agar warna di semua item ter-update
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CuacaItem forecast = hourlyForecasts.get(position);

        holder.tvJam.setText(forecast.getHour()); // Gunakan helper method yang lebih aman

        // =================================================================
        //          PERBAIKAN UTAMA ADA DI BARIS INI
        // =================================================================
        // Gunakan "%.0f" karena forecast.t adalah double
        holder.tvSuhu.setText(String.format(Locale.getDefault(), "%.0f°", forecast.t));
        // =================================================================

        // Gunakan Glide untuk memuat ikon dari URL (lebih baik daripada resource manual)
        Glide.with(context)
                .load(forecast.image)
                .into(holder.ivIcon);


        // Logika untuk mengatur tema item
        holder.tvJam.setTextColor(textColor);
        holder.tvSuhu.setTextColor(textColor);
        holder.ivIcon.setColorFilter(textColor);

        // Atur Background Pill berdasarkan warna teks
        if (textColor == ContextCompat.getColor(context, R.color.text_on_light_bg)) {
            holder.container.setBackgroundResource(R.drawable.pill_background_light);
        } else {
            holder.container.setBackgroundResource(R.drawable.pill_background_dark);
        }
    }

    @Override
    public int getItemCount() {
        return hourlyForecasts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJam, tvSuhu;
        ImageView ivIcon;
        LinearLayout container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Pastikan ID ini sesuai dengan file layout item_hourly_forecast.xml Anda
            tvJam = itemView.findViewById(R.id.tvJam);
            tvSuhu = itemView.findViewById(R.id.tvSuhu);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            container = itemView.findViewById(R.id.container);
        }
    }
}