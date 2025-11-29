package com.bmkg.retrofit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.os.CancellationSignal;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

// Import untuk API dan Model
import com.bmkg.retrofit.api.ApiClient;
import com.bmkg.retrofit.api.ApiService;
import com.bmkg.retrofit.api_wilayah.WilayahApiClient;
import com.bmkg.retrofit.api_wilayah.WilayahApiService;
import com.bmkg.retrofit.model.CuacaItem;
import com.bmkg.retrofit.model.DailyForecast;
import com.bmkg.retrofit.model.DataItem;
import com.bmkg.retrofit.model.ResponseData;
import com.bmkg.retrofit.model_wilayah.Kabupaten;
import com.bmkg.retrofit.model_wilayah.Kecamatan;
import com.bmkg.retrofit.model_wilayah.Kelurahan;
import com.bmkg.retrofit.model_wilayah.Provinsi;

// Import dari Google Play Services dan AndroidX
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG_MANUAL = "WeatherLog_Manual";
    private static final String TAG_GPS = "WeatherLog_GPS";
    private static final String TAG_API = "WeatherLog_API";

    private TextView tvLokasi, tvSuhu, tvCuaca, tvHourlyTitle, tvDailyTitle, tvDetailsTitle;
    private ImageView ivWeatherBackground, btnChangeLocation, ivNotificationSettings, btnSyncLocation;
    private RecyclerView rvDaily, rvHourly;
    private MaterialCardView bottomPanel;
    private ProgressBar progressBar;
    private GridLayout detailsGrid;


    private WeatherHourAdapter hourlyAdapter;
    private DailyForecastAdapter dailyAdapter;
    private final List<CuacaItem> hourlyList = new ArrayList<>();
    private final List<DailyForecast> dailyList = new ArrayList<>();

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> locationPickerLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> resolutionForResult;

    // Komponen Lokasi
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private String geocodedLocationName = null;
    private boolean isRequestingLocationUpdates = false;

    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_LAST_LOCATION_CODE = "lastLocationCode";
    private static final String WORKER_TAG = "periodicWeatherCheck";

    private WilayahApiService wilayahApi;
    private final List<Provinsi> listProvinsi = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        wilayahApi = WilayahApiClient.getClient().create(WilayahApiService.class);

        fetchProvinsiList();
        initializeViews();
        setupAdapters();
        setupLaunchers();
        loadLastSelectedLocationOrAsk();
        requestNotificationPermission();

        btnSyncLocation.setOnClickListener(v -> tryToGetDeviceLocation());
        btnChangeLocation.setOnClickListener(v -> locationPickerLauncher.launch(new Intent(this, LocationPickerActivity.class)));
        ivNotificationSettings.setOnClickListener(v -> startActivity(new Intent(this, NotificationSettingsActivity.class)));
        scheduleWeatherChecks();
    }

    private void fetchProvinsiList() {
        Log.d(TAG_GPS, "PERSIAPAN: Mengunduh daftar provinsi...");
        wilayahApi.getProvinsi().enqueue(new Callback<List<Provinsi>>() {
            @Override
            public void onResponse(@NonNull Call<List<Provinsi>> call, @NonNull Response<List<Provinsi>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listProvinsi.clear();
                    listProvinsi.addAll(response.body());
                    Log.d(TAG_GPS, "PERSIAPAN SUKSES: " + listProvinsi.size() + " provinsi berhasil dimuat.");
                } else {
                    Log.e(TAG_GPS, "PERSIAPAN GAGAL: Tidak bisa memuat daftar provinsi. Response code: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Provinsi>> call, @NonNull Throwable t) {
                Log.e(TAG_GPS, "PERSIAPAN GAGAL: Koneksi gagal saat memuat daftar provinsi.", t);
            }
        });
    }

    private void initializeViews() {
        bottomPanel = findViewById(R.id.bottomPanel);
        tvHourlyTitle = findViewById(R.id.tvHourlyTitle);
        tvDailyTitle = findViewById(R.id.tvDailyTitle);
        btnSyncLocation = findViewById(R.id.btnSyncLocation);
        progressBar = findViewById(R.id.progressBar);
        ivWeatherBackground = findViewById(R.id.ivWeatherBackground);
        tvLokasi = findViewById(R.id.tvLokasi);
        btnChangeLocation = findViewById(R.id.btnChangeLocation);
        tvSuhu = findViewById(R.id.tvSuhu);
        tvCuaca = findViewById(R.id.tvCuaca);
        rvDaily = findViewById(R.id.rvDaily);
        rvHourly = findViewById(R.id.rvHourly);
        ivNotificationSettings = findViewById(R.id.ivNotificationSettings);
        detailsGrid = findViewById(R.id.detailsGrid);
        tvDetailsTitle = findViewById(R.id.tvDetailsTitle);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        tvLokasi.setVisibility(View.GONE);
        tvSuhu.setVisibility(View.GONE);
        tvCuaca.setVisibility(View.GONE);
        bottomPanel.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        tvLokasi.setVisibility(View.VISIBLE);
        tvSuhu.setVisibility(View.VISIBLE);
        tvCuaca.setVisibility(View.VISIBLE);
        bottomPanel.setVisibility(View.VISIBLE);
    }

    private void tryToGetDeviceLocation() {
        Log.d(TAG_GPS, "===== MEMULAI PROSES LOKASI GPS =====");
        showLoading();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            checkGpsStatusAndFetchLocation();
        }
    }

    private void checkGpsStatusAndFetchLocation() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> fetchLastKnownLocation());
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(((ResolvableApiException) e).getResolution()).build();
                    resolutionForResult.launch(intentSenderRequest);
                } catch (Exception sendEx) { /* Ignore */ }
            }
        });
    }

    private void reverseGeocodeLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String prov = address.getAdminArea();
                String kab = address.getSubAdminArea();
                String kec = address.getLocality();
                String kel = address.getSubLocality();

                if (prov == null || kab == null || kec == null || kel == null) {
                    Toast.makeText(this, "Gagal mendapatkan detail lokasi lengkap dari GPS.", Toast.LENGTH_LONG).show();
                    hideLoading();
                    return;
                }
                this.geocodedLocationName = kel;
                findIdBerantai(prov, kab, kec, kel);
            }
        } catch (IOException e) {
            Log.e("GeocoderError", "GEOCODER GAGAL", e);
            Toast.makeText(this, "Gagal mengidentifikasi lokasi.", Toast.LENGTH_LONG).show();
            hideLoading();
        }
    }

    private void loadWeather(final String adm4Code, final boolean shouldSaveLocation) {
        Log.d(TAG_API, "Memanggil API Cuaca dengan Kode Final: " + adm4Code);
        if (progressBar.getVisibility() == View.GONE) showLoading();

        ApiClient.getClient().create(ApiService.class).getWeatherByAdm4(adm4Code).enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(@NonNull Call<ResponseData> call, @NonNull Response<ResponseData> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().data.isEmpty()) {
                    Log.e("API", "Response error or empty data for code: " + adm4Code);
                    Toast.makeText(WeatherActivity.this, "Gagal memuat data cuaca.", Toast.LENGTH_LONG).show();
                    hideLoading();
                    return;
                }
                if (shouldSaveLocation) {
                    saveLastLocationCode(adm4Code);
                }
                updateUI(response.body().data.get(0));
            }

            @Override
            public void onFailure(@NonNull Call<ResponseData> call, @NonNull Throwable t) {
                Log.e("API ERROR", "Gagal melakukan panggilan API: ", t);
                Toast.makeText(WeatherActivity.this, "Koneksi Gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
                hideLoading();
            }
        });
    }

    private void updateUI(DataItem item) {
        tvLokasi.setText(geocodedLocationName != null ? geocodedLocationName : item.lokasi.desa);
        geocodedLocationName = null;

        hourlyList.clear();
        if (item.cuaca != null) {
            for (List<CuacaItem> inner : item.cuaca) {
                hourlyList.addAll(inner);
            }
        }

        if (!hourlyList.isEmpty()) {
            CuacaItem firstHour = hourlyList.get(0);

            tvSuhu.setText(String.format(Locale.getDefault(), "%.0f°", firstHour.t));
            tvCuaca.setText(firstHour.weather_desc);
            updateThemeAndBackground(firstHour.weather_desc);

            detailsGrid.removeAllViews();
            int currentTextColor = tvHourlyTitle.getCurrentTextColor();

            addDetailItem(R.drawable.ic_wind, String.format(Locale.getDefault(), "%.1f m/s", firstHour.ws), "Kecepatan Angin", currentTextColor);
            addDetailItem(R.drawable.ic_humidity, String.format(Locale.getDefault(), "%.0f %%", firstHour.hu), "Kelembapan", currentTextColor);
            addDetailItem(R.drawable.ic_visibility, firstHour.vs_text, "Jarak Pandang", currentTextColor);
            addDetailItem(R.drawable.ic_compass, firstHour.wd, "Arah Angin", currentTextColor);
            addDetailItem(R.drawable.ic_precipitation, String.format(Locale.getDefault(), "%.1f mm", firstHour.tp), "Presipitasi", currentTextColor);
            addDetailItem(R.drawable.ic_cloud, String.format(Locale.getDefault(), "%.0f %%", firstHour.tcc), "Tutupan Awan", currentTextColor);

        } else {
            updateThemeAndBackground("Cerah");
        }

        hourlyAdapter.notifyDataSetChanged();

        dailyList.clear();
        if (item.cuaca != null) {
            int maxHari = Math.min(item.cuaca.size(), 5);
            for (int d = 0; d < maxHari; d++) {
                List<CuacaItem> hariList = item.cuaca.get(d);
                if (hariList == null || hariList.isEmpty()) continue;

                double maxTemp = -999;
                double minTemp = 999;
                for (CuacaItem c : hariList) {
                    if (c.t > maxTemp) maxTemp = c.t;
                    if (c.t < minTemp) minTemp = c.t;
                }
                CuacaItem rep = hariList.get(hariList.size() / 2);
                String dayName = getDayNameFromDate(rep.local_datetime.substring(0, 10));
                dailyList.add(new DailyForecast(dayName, (int)maxTemp, (int)minTemp, rep.weather_desc, rep.image, 0));
            }
        }
        dailyAdapter.notifyDataSetChanged();
        hideLoading();
    }

    private void addDetailItem(int iconResId, String value, String title, int textColor) {
        View view = getLayoutInflater().inflate(R.layout.item_weather_detail, detailsGrid, false);

        ImageView ivIcon = view.findViewById(R.id.ivDetailIcon);
        TextView tvValue = view.findViewById(R.id.tvDetailValue);
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);

        ivIcon.setImageResource(iconResId);
        ivIcon.setColorFilter(textColor);
        tvValue.setText(value);
        tvValue.setTextColor(textColor);
        tvTitle.setText(title);
        tvTitle.setTextColor(textColor);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        view.setLayoutParams(params);

        detailsGrid.addView(view);
    }

    private void setupAdapters() {
        hourlyAdapter = new WeatherHourAdapter(hourlyList, this);
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHourly.setAdapter(hourlyAdapter);
        dailyAdapter = new DailyForecastAdapter(dailyList, this);
        rvDaily.setLayoutManager(new LinearLayoutManager(this));
        rvDaily.setAdapter(dailyAdapter);
    }

    private void setupLaunchers() {
        resolutionForResult = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        tryToGetDeviceLocation();
                    } else {
                        Toast.makeText(this, "GPS diperlukan untuk lokasi otomatis.", Toast.LENGTH_LONG).show();
                        hideLoading();
                    }
                });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                tryToGetDeviceLocation();
            } else {
                Toast.makeText(this, "Izin lokasi ditolak.", Toast.LENGTH_LONG).show();
                hideLoading();
            }
        });

        locationPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String kodeWilayah = result.getData().getStringExtra("KODE_WILAYAH_RESULT");
                if (kodeWilayah != null) {
                    loadWeather(kodeWilayah, true);
                }
            }
        });

        notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(POST_NOTIFICATIONS);
            }
        }
    }

    private void scheduleWeatherChecks() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(WeatherCheckWorker.class, 1, TimeUnit.HOURS).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    private void loadLastSelectedLocationOrAsk() {
        String lastLocationCode = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_LAST_LOCATION_CODE, null);
        if (lastLocationCode != null) {
            loadWeather(lastLocationCode, true);
        } else {
            tryToGetDeviceLocation();
        }
    }

    private void findIdBerantai(final String namaProvinsi, final String namaKabupaten, final String namaKecamatan, final String namaKelurahan) {
        if (listProvinsi.isEmpty()) {
            Log.e(TAG_GPS, "Pencarian dibatalkan. Daftar provinsi belum siap.");
            Toast.makeText(this, "Data provinsi belum siap, coba lagi.", Toast.LENGTH_LONG).show();
            hideLoading();
            return;
        }

        // TAHAP 1: Cari ID Provinsi
        String idProvinsi = null;
        for (Provinsi prov : listProvinsi) {
            if (namaProvinsi.equalsIgnoreCase(prov.getName())) {
                idProvinsi = prov.getId();
                break;
            }
        }
        if (idProvinsi == null) { /* Handle error */ hideLoading(); return; }
        Log.d(TAG_GPS, "TAHAP 1 SUKSES: ID Provinsi ditemukan -> " + idProvinsi);


        // TAHAP 2: Cari ID Kabupaten
        wilayahApi.getKabupaten(idProvinsi).enqueue(new Callback<List<Kabupaten>>() {
            @Override
            public void onResponse(@NonNull Call<List<Kabupaten>> call, @NonNull Response<List<Kabupaten>> response) {
                if (!response.isSuccessful() || response.body() == null) { hideLoading(); return; }

                String idKabupaten = null;
                for (Kabupaten kab : response.body()) {
                    String cleanedNamaGeocoder = namaKabupaten.toLowerCase().replace("kabupaten ", "").replace("kota ", "");
                    String cleanedNamaJson = kab.getName().toLowerCase().replace("kab. ", "").replace("kota ", "");
                    if (cleanedNamaGeocoder.equalsIgnoreCase(cleanedNamaJson)) {
                        idKabupaten = kab.getId();
                        break;
                    }
                }
                if (idKabupaten == null) { /* Handle error */ hideLoading(); return; }
                Log.d(TAG_GPS, "TAHAP 2 SUKSES: ID Kabupaten ditemukan -> " + idKabupaten);


                // TAHAP 3: Cari ID Kecamatan
                wilayahApi.getKecamatan(idKabupaten).enqueue(new Callback<List<Kecamatan>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Kecamatan>> call, @NonNull Response<List<Kecamatan>> response) {
                        if (!response.isSuccessful() || response.body() == null) { hideLoading(); return; }

                        String idKecamatan = null;
                        for(Kecamatan kec : response.body()) {
                            String cleanedNamaGeocoder = namaKecamatan.toLowerCase().replace("kecamatan ", "");
                            String cleanedNamaJson = kec.getName().toLowerCase();
                            if (cleanedNamaGeocoder.equalsIgnoreCase(cleanedNamaJson)) {
                                idKecamatan = kec.getId();
                                break;
                            }
                        }
                        if (idKecamatan == null) { /* Handle error */ hideLoading(); return; }
                        Log.d(TAG_GPS, "TAHAP 3 SUKSES: ID Kecamatan ditemukan -> " + idKecamatan);


                        // TAHAP 4: Cari ID Kelurahan
                        wilayahApi.getKelurahan(idKecamatan).enqueue(new Callback<List<Kelurahan>>() {
                            @Override
                            public void onResponse(@NonNull Call<List<Kelurahan>> call, @NonNull Response<List<Kelurahan>> response) {
                                if (!response.isSuccessful() || response.body() == null) { hideLoading(); return; }

                                String idKelurahan = null;
                                for (Kelurahan kel : response.body()) {
                                    if (namaKelurahan.equalsIgnoreCase(kel.getName())) {
                                        idKelurahan = kel.getId();
                                        break;
                                    }
                                }

                                if (idKelurahan != null) {
                                    Log.d(TAG_GPS, "TAHAP 4 SUKSES: ID Kelurahan mentah ditemukan -> " + idKelurahan);

                                    // ===== PERBAIKAN FINAL ADA DI SINI =====
                                    String formattedId = idKelurahan;
                                    // Pastikan panjang stringnya adalah 10 karakter
                                    if (idKelurahan.length() == 10 && !idKelurahan.contains(".")) {
                                        String prov = idKelurahan.substring(0, 2);
                                        String kab = idKelurahan.substring(2, 4);
                                        String kec = idKelurahan.substring(4, 6);
                                        String kel = idKelurahan.substring(6);
                                        formattedId = prov + "." + kab + "." + kec + "." + kel;
                                        Log.d(TAG_GPS, "ID berhasil diformat ulang menjadi: " + formattedId);
                                    } else {
                                        Log.w(TAG_GPS, "ID Kelurahan tidak memiliki format yang diharapkan (panjang bukan 10), menggunakan nilai asli.");
                                    }

                                    Log.d(TAG_GPS, "===== SEMUA PROSES LOKASI GPS BERHASIL =====");
                                    loadWeather(formattedId, false);

                                } else {
                                    Log.e(TAG_GPS, "TAHAP 4 GAGAL: ID Kelurahan untuk '" + namaKelurahan + "' tidak ditemukan.");
                                    Toast.makeText(WeatherActivity.this, "Data untuk kelurahan '" + namaKelurahan + "' tidak ditemukan.", Toast.LENGTH_SHORT).show();
                                    hideLoading();
                                }
                            }
                            @Override public void onFailure(@NonNull Call<List<Kelurahan>> call, @NonNull Throwable t) { hideLoading(); }
                        });
                    }
                    @Override public void onFailure(@NonNull Call<List<Kecamatan>> call, @NonNull Throwable t) { hideLoading(); }
                });
            }
            @Override public void onFailure(@NonNull Call<List<Kabupaten>> call, @NonNull Throwable t) { hideLoading(); }
        });
    }

    @SuppressLint("MissingPermission")
    private void fetchLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        reverseGeocodeLocation(location);
                    } else {
                        startLocationUpdates();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Gagal mendapatkan lokasi.", Toast.LENGTH_LONG).show();
                    hideLoading();
                });
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                stopLocationUpdates();
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    reverseGeocodeLocation(lastLocation);
                } else {
                    Toast.makeText(WeatherActivity.this, "Gagal mendapatkan lokasi.", Toast.LENGTH_LONG).show();
                    hideLoading();
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (isRequestingLocationUpdates) return;
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();
        isRequestingLocationUpdates = true;
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        if (!isRequestingLocationUpdates) return;
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isRequestingLocationUpdates = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void updateThemeAndBackground(String weatherDescription) {
        String desc = weatherDescription.toLowerCase();
        int textColor;
        int panelBgColor;
        int panelStrokeColor;
        int backgroundResId;
        int pillBackgroundResId;

        if (desc.contains("berawan") || desc.contains("cloudy")) {
            backgroundResId = R.drawable.cloudy;
            textColor = ContextCompat.getColor(this, R.color.text_on_light_bg);
            panelBgColor = ContextCompat.getColor(this, R.color.panel_bg_light);
            panelStrokeColor = ContextCompat.getColor(this, R.color.panel_stroke_light);
            pillBackgroundResId = R.drawable.pill_background_light;
        } else {
            textColor = ContextCompat.getColor(this, R.color.text_on_dark_bg);
            panelBgColor = ContextCompat.getColor(this, R.color.panel_bg_dark);
            panelStrokeColor = ContextCompat.getColor(this, R.color.panel_stroke_dark);
            pillBackgroundResId = R.drawable.pill_background_dark;

            if (desc.contains("badai") || desc.contains("storm") || desc.contains("thunder")) {
                backgroundResId = R.drawable.stormy;
            } else if (desc.contains("hujan") || desc.contains("rain")) {
                backgroundResId = R.drawable.rainy;
            } else {
                backgroundResId = R.drawable.sunny;
            }
        }

        ivWeatherBackground.setImageResource(backgroundResId);
        tvLokasi.setTextColor(textColor);
        tvSuhu.setTextColor(textColor);
        tvCuaca.setTextColor(textColor);
        btnChangeLocation.setColorFilter(textColor);
        ivNotificationSettings.setColorFilter(textColor);
        btnSyncLocation.setColorFilter(textColor);
        tvCuaca.setBackgroundResource(pillBackgroundResId);
        bottomPanel.setCardBackgroundColor(panelBgColor);
        bottomPanel.setStrokeColor(panelStrokeColor);
        tvHourlyTitle.setTextColor(textColor);
        tvDailyTitle.setTextColor(textColor);
        tvDetailsTitle.setTextColor(textColor);

        if (hourlyAdapter != null) hourlyAdapter.setTextColor(textColor);
        if (dailyAdapter != null) dailyAdapter.setTextColor(textColor);
    }

    private void saveLastLocationCode(String code) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_LAST_LOCATION_CODE, code).apply();
    }

    private String getDayNameFromDate(String dateString) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString);
            if (date != null) {
                return new SimpleDateFormat("EEEE", new Locale("id", "ID")).format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Error";
    }
}