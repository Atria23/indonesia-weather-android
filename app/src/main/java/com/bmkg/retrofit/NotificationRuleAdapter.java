package com.bmkg.retrofit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bmkg.retrofit.model.NotificationRule;
import com.bmkg.retrofit.utils.NotificationHelper;
import com.bmkg.retrofit.utils.RuleManager;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class NotificationRuleAdapter extends RecyclerView.Adapter<NotificationRuleAdapter.ViewHolder> {

    // Kata kunci 'final' dihapus agar list bisa di-update
    private List<NotificationRule> rules;
    private final Context context;

    public NotificationRuleAdapter(List<NotificationRule> rules, Context context) {
        this.rules = rules;
        this.context = context;
    }

    // ===================================================================
    // ===== METODE BARU UNTUK MEMPERBARUI DATA DAN MENGATASI ERROR ======
    // ===================================================================
    public void updateRules(List<NotificationRule> newRules) {
        this.rules = newRules; // Mengarahkan adapter ke list yang baru
        notifyDataSetChanged(); // Memberi tahu RecyclerView untuk me-refresh tampilannya
    }
    // ===================================================================


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_rule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationRule rule = rules.get(position);

        holder.tvLocation.setText(rule.getLocationName());
        holder.tvCondition.setText("Kondisi: " + rule.getWeatherCondition());

        // Menampilkan atau menyembunyikan info waktu dan tanggal
        String timeDateInfo = formatTimeDateInfo(rule.getNotificationTime(), rule.getNotificationDate());
        if (timeDateInfo.isEmpty()) {
            holder.tvTime.setVisibility(View.GONE);
        } else {
            holder.tvTime.setText(timeDateInfo);
            holder.tvTime.setVisibility(View.VISIBLE);
        }

        // Atur status switch tanpa memicu listener saat pertama kali di-bind
        holder.switchEnable.setOnCheckedChangeListener(null);
        holder.switchEnable.setChecked(rule.isEnabled());

        // Listener untuk seluruh kartu, untuk mengedit
        holder.cardRule.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddRuleActivity.class);
            intent.putExtra("EDIT_RULE", rule);
            context.startActivity(intent);
        });

        // Listener untuk tombol tes notifikasi
        holder.ivTest.setOnClickListener(v -> {
            String title = "Tes Notifikasi";
            String message = "Ini adalah notifikasi tes untuk " + rule.getLocationName();
            NotificationHelper.showNotification(context, title, message);
            Toast.makeText(context, "Notifikasi tes dikirim!", Toast.LENGTH_SHORT).show();
        });

        // Listener untuk switch, untuk mengaktifkan/menonaktifkan aturan
        holder.switchEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rule.setEnabled(isChecked);
            // Simpan perubahan status ke RuleManager
            RuleManager.updateRule(context, rule);
        });

        // Listener untuk tombol hapus
        holder.ivDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Aturan")
                    .setMessage("Apakah Anda yakin ingin menghapus aturan untuk " + rule.getLocationName() + "?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        // Gunakan getAdapterPosition() untuk keamanan saat item dihapus
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            rules.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                            RuleManager.saveRules(context, rules);

                            // Cek jika list menjadi kosong setelah dihapus
                            if (context instanceof NotificationSettingsActivity) {
                                ((NotificationSettingsActivity) context).checkEmptyState();
                            }
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    /**
     * Menggabungkan informasi waktu dan tanggal menjadi satu string yang rapi.
     */
    private String formatTimeDateInfo(String time, String date) {
        if ((time == null || time.isEmpty()) && (date == null || date.isEmpty())) {
            return ""; // Tidak ada info waktu atau tanggal
        }

        StringBuilder info = new StringBuilder();
        if (time != null && !time.isEmpty()) {
            info.append("Pukul ").append(time);
        }

        if (date != null && !date.isEmpty()) {
            if (info.length() > 0) {
                info.append(" • "); // Pemisah jika ada waktu dan tanggal
            }
            info.append(date);
        }
        return info.toString();
    }


    @Override
    public int getItemCount() {
        return rules.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocation, tvCondition, tvTime;
        SwitchCompat switchEnable;
        ImageView ivDelete, ivTest;
        MaterialCardView cardRule;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Pastikan ID ini cocok dengan yang ada di layout item_notification_rule.xml Anda
            tvLocation = itemView.findViewById(R.id.tvRuleLocation);
            tvCondition = itemView.findViewById(R.id.tvRuleCondition);
            tvTime = itemView.findViewById(R.id.tvRuleTime);
            switchEnable = itemView.findViewById(R.id.switchEnableRule);
            ivDelete = itemView.findViewById(R.id.ivDeleteRule);
            ivTest = itemView.findViewById(R.id.ivTestRule);
            cardRule = itemView.findViewById(R.id.cardRule);
        }
    }
}