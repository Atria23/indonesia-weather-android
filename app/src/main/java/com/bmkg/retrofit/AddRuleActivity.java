package com.bmkg.retrofit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.bmkg.retrofit.model.NotificationRule;
import com.bmkg.retrofit.utils.RuleManager;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddRuleActivity extends AppCompatActivity {

    private TextView tvSelectedLocation, tvSelectedTime, tvSelectedDate;
    private AutoCompleteTextView autoCompleteWeatherCondition;
    private String selectedLocationCode;
    private String selectedLocationName;
    private String selectedTime = null;
    private String selectedDate = null;
    private boolean isEditMode = false;
    private NotificationRule ruleToEdit;

    // Konstanta untuk teks default
    private final String DEFAULT_TIME_TEXT = "Waktu belum diatur (berlaku sepanjang hari)";
    private final String DEFAULT_DATE_TEXT = "Berlaku setiap hari (pilih untuk tanggal spesifik)";


    private final ActivityResultLauncher<Intent> locationPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedLocationCode = result.getData().getStringExtra("KODE_WILAYAH_RESULT");
                    selectedLocationName = result.getData().getStringExtra("NAMA_WILAYAH_RESULT");
                    if (selectedLocationName == null) selectedLocationName = "Lokasi Pilihan";
                    tvSelectedLocation.setText(selectedLocationName);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rule);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        autoCompleteWeatherCondition = findViewById(R.id.autoCompleteWeatherCondition);
        Button btnSelectLocation = findViewById(R.id.btnSelectLocation);
        Button btnSelectTime = findViewById(R.id.btnSelectTime);
        Button btnSelectDate = findViewById(R.id.btnSelectDate);
        Button btnSaveRule = findViewById(R.id.btnSaveRule);

        // <-- TOMBOL BARU UNTUK MENGOSONGKAN PILIHAN -->
        Button btnClearTime = findViewById(R.id.btnClearTime);
        Button btnClearDate = findViewById(R.id.btnClearDate);


        setupWeatherConditionDropdown();

        if (getIntent().hasExtra("EDIT_RULE")) {
            isEditMode = true;
            ruleToEdit = (NotificationRule) getIntent().getSerializableExtra("EDIT_RULE");
            if (ruleToEdit != null) {
                populateUiWithRuleData(ruleToEdit);
                toolbar.setTitle("Edit Aturan");
                btnSaveRule.setText("Simpan Perubahan");
            }
        }

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }

        btnSelectLocation.setOnClickListener(v -> locationPickerLauncher.launch(new Intent(this, LocationPickerActivity.class)));
        btnSelectTime.setOnClickListener(v -> showTimePickerDialog());
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        btnSaveRule.setOnClickListener(v -> saveRule());

        // <-- SET LISTENER UNTUK TOMBOL HAPUS -->
        btnClearTime.setOnClickListener(v -> {
            selectedTime = null;
            tvSelectedTime.setText(DEFAULT_TIME_TEXT);
        });

        btnClearDate.setOnClickListener(v -> {
            selectedDate = null;
            tvSelectedDate.setText(DEFAULT_DATE_TEXT);
        });
    }

    private void saveRule() {
        String condition = autoCompleteWeatherCondition.getText().toString().trim();
        if (TextUtils.isEmpty(selectedLocationCode) || TextUtils.isEmpty(condition)) {
            Toast.makeText(this, "Lokasi dan Kondisi Cuaca harus diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<NotificationRule> rules = RuleManager.getRules(this);
        NotificationRule savedRule = null;

        if (isEditMode && ruleToEdit != null) {
            boolean found = false;
            for (int i = 0; i < rules.size(); i++) {
                if (rules.get(i).getId().equals(ruleToEdit.getId())) {
                    savedRule = new NotificationRule(
                            ruleToEdit.getId(), selectedLocationCode, selectedLocationName,
                            condition, selectedTime, selectedDate, ruleToEdit.isEnabled()
                    );
                    rules.set(i, savedRule);
                    found = true;
                    break;
                }
            }
            if (!found) {
                String id = UUID.randomUUID().toString();
                savedRule = new NotificationRule(id, selectedLocationCode, selectedLocationName, condition, selectedTime, selectedDate, true);
                rules.add(savedRule);
            }
        } else {
            String id = UUID.randomUUID().toString();
            savedRule = new NotificationRule(id, selectedLocationCode, selectedLocationName, condition, selectedTime, selectedDate, true);
            rules.add(savedRule);
        }

        RuleManager.saveRules(this, rules);
        Toast.makeText(this, "Aturan berhasil disimpan", Toast.LENGTH_SHORT).show();

        if (savedRule != null) {
            triggerImmediateCheck(savedRule.getId());
        }

        finish();
    }

    private void triggerImmediateCheck(String ruleId) {
        Data inputData = new Data.Builder()
                .putString("IMMEDIATE_CHECK_RULE_ID", ruleId)
                .build();
        OneTimeWorkRequest immediateCheckWork = new OneTimeWorkRequest.Builder(WeatherCheckWorker.class)
                .setInputData(inputData)
                .build();
        WorkManager.getInstance(this).enqueue(immediateCheckWork);
        Log.d("AddRuleActivity", "Memicu pengecekan segera untuk rule ID: " + ruleId);
    }

    private void populateUiWithRuleData(NotificationRule rule) {
        selectedLocationCode = rule.getLocationCode();
        selectedLocationName = rule.getLocationName();
        tvSelectedLocation.setText(selectedLocationName);
        autoCompleteWeatherCondition.setText(rule.getWeatherCondition(), false);
        selectedTime = rule.getNotificationTime();
        if (selectedTime != null && !selectedTime.isEmpty()) {
            tvSelectedTime.setText("Notifikasi untuk Pukul: " + selectedTime);
        } else {
            tvSelectedTime.setText(DEFAULT_TIME_TEXT);
        }

        selectedDate = rule.getNotificationDate();
        if (selectedDate != null && !selectedDate.isEmpty()) {
            tvSelectedDate.setText("Hanya pada tanggal: " + selectedDate);
        } else {
            tvSelectedDate.setText(DEFAULT_DATE_TEXT);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("locationCode", selectedLocationCode);
        outState.putString("locationName", selectedLocationName);
        outState.putString("time", selectedTime);
        outState.putString("date", selectedDate);
    }

    private void restoreState(Bundle savedInstanceState) {
        selectedLocationCode = savedInstanceState.getString("locationCode");
        selectedLocationName = savedInstanceState.getString("locationName");
        selectedTime = savedInstanceState.getString("time");
        if (selectedLocationName != null) tvSelectedLocation.setText(selectedLocationName);
        if (selectedTime != null) {
            tvSelectedTime.setText("Notifikasi untuk Pukul: " + selectedTime);
        } else {
            tvSelectedTime.setText(DEFAULT_TIME_TEXT);
        }

        selectedDate = savedInstanceState.getString("date");
        if (selectedDate != null) {
            tvSelectedDate.setText("Hanya pada tanggal: " + selectedDate);
        } else {
            tvSelectedDate.setText(DEFAULT_DATE_TEXT);
        }
    }

    private void setupWeatherConditionDropdown() {
        String[] weatherConditions = new String[]{"Cerah", "Cerah Berawan", "Berawan", "Hujan", "Badai", "Kabut"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, weatherConditions);
        autoCompleteWeatherCondition.setAdapter(adapter);
    }

    private void showTimePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    tvSelectedTime.setText("Notifikasi untuk Pukul: " + selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    tvSelectedDate.setText("Hanya pada tanggal: " + selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}