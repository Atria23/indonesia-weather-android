// File: com/bmkg/retrofit/NotificationSettingsActivity.java
package com.bmkg.retrofit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu; // <-- IMPORT BARU
import android.view.MenuInflater; // <-- IMPORT BARU
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // <-- IMPORT BARU
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bmkg.retrofit.model.NotificationRule;
import com.bmkg.retrofit.utils.RuleManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NotificationSettingsActivity extends AppCompatActivity {

    private RecyclerView rvRules;
    private TextView tvEmptyRules;
    private NotificationRuleAdapter adapter;
    private List<NotificationRule> ruleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvRules = findViewById(R.id.rvNotificationRules);
        tvEmptyRules = findViewById(R.id.tvEmptyRules);
        FloatingActionButton fabAddRule = findViewById(R.id.fabAddRule);

        rvRules.setLayoutManager(new LinearLayoutManager(this));

        fabAddRule.setOnClickListener(v -> {
            startActivity(new Intent(this, AddRuleActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRules();
    }

    private void loadRules() {
        ruleList = RuleManager.getRules(this);
        checkEmptyState(); // Panggil checkEmptyState untuk mengatur visibilitas

        // Inisialisasi atau update adapter
        if (adapter == null) {
            adapter = new NotificationRuleAdapter(ruleList, this);
            rvRules.setAdapter(adapter);
        } else {
            adapter.updateRules(ruleList); // Buat metode ini di adapter Anda jika perlu
        }
    }

    // ===============================================
    // ===== KODE BARU UNTUK MENU DAN POPUP INFO =====
    // ===============================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menampilkan menu (tombol info) di toolbar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notification_menu, menu);
        return true;
    }

    private void showInfoDialog() {
        // 1. Buat builder untuk AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Inflate (ubah XML menjadi View) layout kustom kita
        LayoutInflater inflater = this.getLayoutInflater();
        // Pastikan Anda punya layout bernama dialog_info_layout.xml
        View dialogView = inflater.inflate(R.layout.dialog_info_layout, null);

        // 3. Set view kustom ini sebagai konten dari dialog
        builder.setView(dialogView);

        // 4. Tambahkan tombol aksi (button)
        builder.setPositiveButton("Mengerti", (dialog, which) -> {
            dialog.dismiss(); // Menutup dialog
        });

        // 5. Buat dan tampilkan AlertDialog
        AlertDialog dialog = builder.create();

        // 6. (PENTING) Atur latar belakang dialog agar transparan
        //    sehingga drawable rounded corner kita yang terlihat.
        if (dialog.getWindow() != null) {
            // Pastikan Anda punya drawable bernama dialog_rounded_background.xml
            // Jika tidak, Anda bisa menggunakan android.R.color.transparent
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        }

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Menangani klik pada item di toolbar
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            // Tombol kembali di toolbar
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_info) {
            // Tombol info yang baru kita buat
            showInfoDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // ===============================================
    // ================= AKHIR KODE BARU ===============
    // ===============================================

    public void checkEmptyState() {
        if (ruleList.isEmpty()) {
            rvRules.setVisibility(View.GONE);
            tvEmptyRules.setVisibility(View.VISIBLE);
        } else {
            rvRules.setVisibility(View.VISIBLE);
            tvEmptyRules.setVisibility(View.GONE);
        }
    }
}