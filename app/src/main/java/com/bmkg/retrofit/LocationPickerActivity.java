package com.bmkg.retrofit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bmkg.retrofit.api_wilayah.WilayahApiClient;
import com.bmkg.retrofit.api_wilayah.WilayahApiService;
import com.bmkg.retrofit.model_wilayah.Kabupaten;
import com.bmkg.retrofit.model_wilayah.Kecamatan;
import com.bmkg.retrofit.model_wilayah.Kelurahan;
import com.bmkg.retrofit.model_wilayah.Provinsi;
import com.bmkg.retrofit.model_wilayah.SearchableLocation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationPickerActivity extends AppCompatActivity {

    private enum State { GLOBAL_SEARCH, DRILL_DOWN }
    private State currentState = State.GLOBAL_SEARCH;

    private RecyclerView rvLokasi;
    private TextView tvTitle;
    private LocationAdapter adapter;
    private WilayahApiService apiService;
    private SearchView searchView;

    private List<LocationAdapter.LocationItem> globalMasterList = new ArrayList<>();
    private List<LocationAdapter.LocationItem> drillDownMasterList = new ArrayList<>();

    private String selectedProvinsiId, selectedKabupatenId, selectedKecamatanId;
    private String selectedProvinsiName, selectedKabupatenName, selectedKecamatanName;

    // CACHING: Konstanta untuk SharedPreferences
    private static final String PREFS_NAME = "location_cache";
    private static final String KEY_LOCATION_DATA = "location_data";
    private static final String KEY_LAST_CACHE_TIMESTAMP = "last_cache_timestamp";
    private static final long CACHE_DURATION_MILLIS = TimeUnit.DAYS.toMillis(1); // Cache berlaku selama 1 hari

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTitle = findViewById(R.id.tvTitle);
        rvLokasi = findViewById(R.id.rvLokasi);
        searchView = findViewById(R.id.searchView);

        rvLokasi.setLayoutManager(new LinearLayoutManager(this));
        apiService = WilayahApiClient.getClient().create(WilayahApiService.class);
        adapter = new LocationAdapter(new ArrayList<>(), this::onItemClicked);
        rvLokasi.setAdapter(adapter);

        setupSearchView();

        // CACHING: Coba muat dari cache terlebih dahulu
        if (!loadFromCache()) {
            // Jika gagal memuat dari cache (atau cache kedaluwarsa), muat dari API
            loadGlobalSearchDataFromApi();
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    private void filterList(String text) {
        List<LocationAdapter.LocationItem> listToFilter = (currentState == State.GLOBAL_SEARCH) ? globalMasterList : drillDownMasterList;
        List<LocationAdapter.LocationItem> filteredList = new ArrayList<>();
        for (LocationAdapter.LocationItem item : listToFilter) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.updateData(filteredList);
    }

    // CACHING: Ganti nama fungsi menjadi lebih spesifik
    private void loadGlobalSearchDataFromApi() {
        tvTitle.setText("Cari Lokasi");
        Toast.makeText(this, "Memuat data lokasi dari server...", Toast.LENGTH_SHORT).show();

        apiService.getProvinsi().enqueue(new Callback<List<Provinsi>>() {
            @Override
            public void onResponse(Call<List<Provinsi>> call, Response<List<Provinsi>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showErrorAndFinish(null);
                    return;
                }
                List<Provinsi> provinsiList = response.body();
                globalMasterList.clear(); // Bersihkan list sebelum diisi data baru
                for (Provinsi p : provinsiList) {
                    globalMasterList.add(new SearchableLocation(p.id, p.nama, "Provinsi", SearchableLocation.Type.PROVINSI));
                }

                AtomicInteger countdown = new AtomicInteger(provinsiList.size());
                for (Provinsi provinsi : provinsiList) {
                    apiService.getKabupaten(provinsi.id).enqueue(new Callback<List<Kabupaten>>() {
                        @Override
                        public void onResponse(Call<List<Kabupaten>> call, Response<List<Kabupaten>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                for (Kabupaten k : response.body()) {
                                    globalMasterList.add(new SearchableLocation(k.id, k.nama, provinsi.nama, SearchableLocation.Type.KABUPATEN));
                                }
                            }
                            if (countdown.decrementAndGet() == 0) {
                                // CACHING: Simpan data yang berhasil dimuat ke cache
                                saveToCache(globalMasterList);
                                adapter.updateData(new ArrayList<>(globalMasterList));
                                Toast.makeText(LocationPickerActivity.this, "Data siap.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<List<Kabupaten>> call, Throwable t) {
                            if (countdown.decrementAndGet() == 0) {
                                // Tetap tampilkan data provinsi yang berhasil dimuat meskipun beberapa kabupaten gagal
                                adapter.updateData(new ArrayList<>(globalMasterList));
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Provinsi>> call, Throwable t) {
                showErrorAndFinish(t);
            }
        });
    }

    // --- LOGIKA KLIK DAN DRILL-DOWN (Tidak ada perubahan) ---
    private void onItemClicked(LocationAdapter.LocationItem item) {
        if (!(item instanceof SearchableLocation)) return;
        SearchableLocation location = (SearchableLocation) item;

        currentState = State.DRILL_DOWN;
        searchView.setQuery("", false); // Kosongkan search view saat drill-down

        switch (location.getType()) {
            case PROVINSI:
                selectedProvinsiId = location.getId();
                selectedProvinsiName = location.getName();
                loadKabupaten(selectedProvinsiId);
                break;
            case KABUPATEN:
                String kabId = location.getId();
                // Asumsi ID provinsi selalu 2 digit pertama
                selectedProvinsiId = kabId.substring(0, 2);
                selectedKabupatenId = kabId;
                selectedKabupatenName = location.getName();
                loadKecamatan(selectedKabupatenId);
                break;
            case KECAMATAN:
                // Asumsi ID kabupaten adalah 4 digit pertama
                selectedKabupatenId = location.getId().substring(0, 4);
                selectedKecamatanId = location.getId();
                selectedKecamatanName = location.getName();
                loadKelurahan(selectedKecamatanId);
                break;
            case KELURAHAN:
                String provIdForCode = selectedKabupatenId.substring(0,2);
                String kabIdForCode = selectedKabupatenId.substring(2);
                String kecIdForCode = selectedKecamatanId.substring(4);
                String kelIdForCode = location.getId().substring(6);
                String kodeWilayah = provIdForCode + "." + kabIdForCode + "." + kecIdForCode + "." + kelIdForCode;

                Intent resultIntent = new Intent();
                resultIntent.putExtra("KODE_WILAYAH_RESULT", kodeWilayah);
                resultIntent.putExtra("NAMA_WILAYAH_RESULT", location.getName());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
        }
    }


    private void loadKabupaten(String provinsiId) {
        tvTitle.setText(selectedProvinsiName);
        apiService.getKabupaten(provinsiId).enqueue(new Callback<List<Kabupaten>>() {
            @Override
            public void onResponse(Call<List<Kabupaten>> call, Response<List<Kabupaten>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LocationAdapter.LocationItem> locations = new ArrayList<>();
                    for (Kabupaten k : response.body()) {
                        locations.add(new SearchableLocation(k.id, k.nama, selectedProvinsiName, SearchableLocation.Type.KABUPATEN));
                    }
                    updateDrillDownList(locations);
                } else { showErrorAndFinish(null); }
            }
            @Override
            public void onFailure(Call<List<Kabupaten>> call, Throwable t) { showErrorAndFinish(t); }
        });
    }

    private void loadKecamatan(String kabupatenId) {
        tvTitle.setText(selectedKabupatenName);
        apiService.getKecamatan(kabupatenId).enqueue(new Callback<List<Kecamatan>>() {
            @Override
            public void onResponse(Call<List<Kecamatan>> call, Response<List<Kecamatan>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LocationAdapter.LocationItem> locations = new ArrayList<>();
                    for (Kecamatan k : response.body()) {
                        locations.add(new SearchableLocation(k.id, k.nama, selectedKabupatenName, SearchableLocation.Type.KECAMATAN));
                    }
                    updateDrillDownList(locations);
                } else { showErrorAndFinish(null); }
            }
            @Override
            public void onFailure(Call<List<Kecamatan>> call, Throwable t) { showErrorAndFinish(t); }
        });
    }

    private void loadKelurahan(String kecamatanId) {
        tvTitle.setText(selectedKecamatanName);
        apiService.getKelurahan(kecamatanId).enqueue(new Callback<List<Kelurahan>>() {
            @Override
            public void onResponse(Call<List<Kelurahan>> call, Response<List<Kelurahan>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LocationAdapter.LocationItem> locations = new ArrayList<>();
                    for (Kelurahan k : response.body()) {
                        locations.add(new SearchableLocation(k.id, k.nama, selectedKecamatanName, SearchableLocation.Type.KELURAHAN));
                    }
                    updateDrillDownList(locations);
                } else { showErrorAndFinish(null); }
            }
            @Override
            public void onFailure(Call<List<Kelurahan>> call, Throwable t) { showErrorAndFinish(t); }
        });
    }

    private void updateDrillDownList(List<LocationAdapter.LocationItem> data) {
        drillDownMasterList.clear();
        drillDownMasterList.addAll(data);
        adapter.updateData(new ArrayList<>(drillDownMasterList));
        searchView.setQuery("", false);
    }
    // --- AKHIR DARI LOGIKA KLIK ---

    @Override
    public void onBackPressed() {
        if (currentState == State.DRILL_DOWN) {
            currentState = State.GLOBAL_SEARCH;
            tvTitle.setText("Cari Lokasi");
            adapter.updateData(new ArrayList<>(globalMasterList));
            searchView.setQuery("", false);
        } else {
            super.onBackPressed();
        }
    }

    private void showErrorAndFinish(Throwable t) {
        if (t != null) {
            Log.e("API_WILAYAH_ERROR", "Gagal memuat data wilayah: ", t);
        } else {
            Log.e("API_WILAYAH_ERROR", "Gagal memuat data wilayah: Respons tidak berhasil atau body kosong.");
        }
        Toast.makeText(this, "Gagal memuat daftar lokasi.", Toast.LENGTH_LONG).show();
        finish();
    }

    // CACHING: Fungsi untuk menyimpan data ke SharedPreferences
    private void saveToCache(List<LocationAdapter.LocationItem> data) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(data);

        editor.putString(KEY_LOCATION_DATA, json);
        editor.putLong(KEY_LAST_CACHE_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
        Log.d("CACHE_LOCATION", "Data lokasi disimpan ke cache.");
    }

    // CACHING: Fungsi untuk memuat data dari SharedPreferences
    private boolean loadFromCache() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastCacheTime = prefs.getLong(KEY_LAST_CACHE_TIMESTAMP, 0);

        // Cek apakah cache ada dan belum kedaluwarsa
        if (lastCacheTime > 0 && (System.currentTimeMillis() - lastCacheTime < CACHE_DURATION_MILLIS)) {
            String json = prefs.getString(KEY_LOCATION_DATA, null);
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<SearchableLocation>>() {}.getType();
                // Kita perlu deserialize sebagai SearchableLocation karena itu class konkretnya
                List<SearchableLocation> cachedList = gson.fromJson(json, type);

                if (cachedList != null && !cachedList.isEmpty()) {
                    globalMasterList.clear();
                    globalMasterList.addAll(cachedList);
                    adapter.updateData(new ArrayList<>(globalMasterList));
                    Toast.makeText(this, "Memuat data lokasi dari cache.", Toast.LENGTH_SHORT).show();
                    Log.d("CACHE_LOCATION", "Data lokasi berhasil dimuat dari cache.");
                    return true;
                }
            }
        }
        Log.d("CACHE_LOCATION", "Cache tidak ditemukan atau sudah kedaluwarsa.");
        return false; // Cache tidak valid atau tidak ada
    }
}