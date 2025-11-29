# Aplikasi Prakiraan Cuaca Indonesia

<p align="left">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg" alt="Platform Android">
  <img src="https://img.shields.io/badge/API-21%2B-blue.svg" alt="API 21+">
  <img src="https://img.shields.io/badge/License-MIT-purple.svg" alt="License MIT">
</p>

Proyek ini awalnya dibuat untuk tugas kuliah Pemrograman Mobile, tapi idenya berkembang dari hobi memancing—butuh data cuaca yang akurat sampai ke pelosok! Dari situ, proyek ini jadi ajang buat ngulik lebih dalam soal Android, Retrofit, dan cara kerja API, dengan fokus membuat aplikasi cuaca yang nyaman dan `Indonesia banget`.

---

## ✨ Jadi, Aplikasinya Bisa Ngapain Aja?

*   🎨 **Tampilan Nggak Monoton**
    Biar gak bosen, background dan warna teks di aplikasi ini nyocokin cuaca di luar. Lagi cerah, ya tampilannya cerah. Lagi hujan, ya ikut galau dikit, hehe.

*   🔔 **Notifikasi Sesuai Maumu**
    Mau pergi tapi ragu hujan atau nggak? Atur aja notifikasi sendiri. Misalnya, kamu bisa bikin aturan "Kasih tau aku kalau bakal Hujan". Nanti aplikasi bakal ngabarin kalau prediksinya sesuai.

*   📍 **Deteksi Lokasi Otomatis (GPS)**
    Gak perlu repot ngetik. Buka aplikasi, klik logo GPS, lokasimu langsung kedeteksi, dan cuacanya langsung tampil.

*   🗺️ **Pilih Lokasi Sampai Pelosok**
    Lagi di luar kota atau mau cek cuaca di kampung halaman? Bisa banget. Pilih lokasi manual dari tingkat Provinsi, Kabupaten, Kecamatan, sampai ke Desa/Kelurahan.

---

## 📸 Penampakannya Gimana?

| Cuaca Cerah ☀️ | Cuaca Berawan ☁️ | Cuaca Hujan 🌧️ |
| :---: | :---: | :---: |
| [![cerah](https://drive.google.com/thumbnail?id=17fucjQtEW7S3rcldCDsfqulRo9DTBH-v&sz=w600)](https://drive.google.com/file/d/17fucjQtEW7S3rcldCDsfqulRo9DTBH-v/view) | [![berawan](https://drive.google.com/thumbnail?id=1-55OGvPU5Mi_-AIr_DUgBqQFWsX-T_hr&sz=w600)](https://drive.google.com/file/d/1-55OGvPU5Mi_-AIr_DUgBqQFWsX-T_hr/view) | [![hujan](https://drive.google.com/thumbnail?id=18uL4kDTyYCHXNChU3KlmxHaobhXbRNRp&sz=w600)](https://drive.google.com/file/d/18uL4kDTyYCHXNChU3KlmxHaobhXbRNRp/view) |

| Daftar Aturan Notifikasi 📋 | Tambah Aturan Baru ➕ | Tampilan Detail ℹ️ |
| :---: | :---: | :---: |
| [![daftar aturan](https://drive.google.com/thumbnail?id=1APwyf0bvbh_cxnDB3QdlW7qSgDLq3wJw&sz=w600)](https://drive.google.com/file/d/1APwyf0bvbh_cxnDB3QdlW7qSgDLq3wJw/view) | [![tambah aturan](https://drive.google.com/thumbnail?id=1fYH4SnGAP6Ifj1-pPjZPr6xxdCvYQXRU&sz=w600)](https://drive.google.com/file/d/1fYH4SnGAP6Ifj1-pPjZPr6xxdCvYQXRU/view) | [![detail](https://drive.google.com/thumbnail?id=15wHOgrbxr4z-pu4UyU0t1ZuxRbxEsyj1&sz=w600)](https://drive.google.com/file/d/15wHOgrbxr4z-pu4UyU0t1ZuxRbxEsyj1/view) |

---

## 🛠️ Di Balik Layar (Tech Stack)

Aplikasi ini dibangun pakai beberapa teknologi andalan:

-   **Bahasa**: Tentu saja, **Java**.
-   **Ngobrol sama API**: Pakai **Retrofit 2**, biar urusan narik data dari internet jadi lebih gampang dan rapi.
-   **Nampilin Data**: **RecyclerView** jadi andalan buat nampilin daftar cuaca per jam dan harian.
-   **Kerja di Latar**: **WorkManager** dipake buat ngecek cuaca secara berkala tanpa bikin boros baterai.
-   **Cari Lokasi**: Manfaatin **FusedLocationProvider** dari Google buat dapetin lokasi GPS yang pas.
-   **Tampilan**: Dibantu sama **Material Components** biar kelihatan modern.

---

## 🙏 Makasih Banyak Buat Para Penyedia API!

Aplikasi ini gak bakal ada tanpa data keren dari:

1.  **BMKG**: Buat data cuacanya yang super lengkap. Makasih, [BMKG](https://data.bmkg.go.id/)!
2.  **API Wilayah oleh Ibnux**: Data wilayah se-Indonesia yang rapi banget ini diambil dari proyek _open-source_ [data-indonesia](https://github.com/ibnux/data-indonesia) milik Bang [Ibnux](https://github.com/ibnux). Karyanya ngebantu banget!

---

## 🚀 Mau Coba? Gini Caranya

1.  **Clone dulu repo ini:**
    ```bash
    git clone https://github.com/Atria23/indonesia-weather-android.git
    ```

2.  Buka project-nya pakai **Android Studio**.

3.  Tungguin Gradle-nya selesai *sync*. Sabar ya, kadang emang suka lama.

4.  Langsung aja klik tombol **Run** ke emulator atau HP Android-mu.

Voila! Selamat mencoba.

Ada bug atau punya ide keren? Jangan ragu buat buka *issue* ya!

---

Dibuat oleh [Atria23](https://github.com/Atria23).
