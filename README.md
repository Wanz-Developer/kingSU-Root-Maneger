# kingSU - All-in-One Root Manager & Kernel Console

kingSU adalah aplikasi manajemen hak akses sistem tingkat tinggi dan alat kustomisasi perangkat Android modern yang dibangun menggunakan **Jetpack Compose**. Aplikasi ini dirancang sebagai platform hybrid serba otomatis yang menggabungkan fitur kontrol terbaik untuk efisiensi kustomisasi sistem secara *systemless*.

## 📸 Antarmuka Aplikasi (Screenshots)

| Dashboard UI | Terminal & Console | App Freezer & Tweaks |
| :---: | :---: | :---: |
| <img src="Screenshot_2026-10-05-02-34-5.png" width="250" alt="Dashboard"/> | <img src="Screenshot_2026-10-05-02-35-0.png" width="250" alt="Terminal"/> | <img src="Screenshot_2026-10-05-02-35-1.png" width="250" alt="Freezer"/> |

| Modul Manager | Penyamaran Root | Installer Screen |
| :---: | :---: | :---: |
| <img src="Screenshot_2026-10-05-02-35-2.png" width="250" alt="Modules"/> | <img src="Screenshot_2026-10-05-02-35-3.png" width="250" alt="Stealth"/> | <img src="Screenshot_2026-10-05-02-35-4.png" width="250" alt="Installer"/> |

## 🚀 Fitur Utama

* **Mesin Analisis Kernel Pintar (`GkiKernelEngine`)**: Secara dinamis mengidentifikasi rilis kernel perangkat (GKI 2.0, GKI 1.0, atau Legacy Non-GKI) untuk memberikan rekomendasi metode root terbaik yang aman.
* **Otomatisasi Flashing Multi-Platform**: Membakar framework root secara lokal dan otomatis memproduksi skrip otomasi `flash_kingsu.sh` (untuk HP kedua via Bugjaeger) dan `flash_kingsu.bat` (untuk PC Windows) di folder `/sdcard/Download/`.
* **Master Kontrol Penyamaran (`Stealth Engine`)**: Sistem pelindung *Universal Cloaking* yang fleksibel mendeteksi ekosistem root aktif untuk menyembunyikan status modifikasi perangkat dari deteksi aplikasi sensitif/perbankan.
* **Console & Utilitas Sistem**: Dilengkapi dengan Terminal Shell interaktif bawaan dengan makro cepat, modul Pembeku Aplikasi (*App Freezer*) berbasis perintah `pm`, serta *Hardware & Memory Monitor* secara real-time.

## 🛠️ Arsitektur Proyek
* **UI Framework**: Jetpack Compose (Modern & Responsif)
* **Backend Logika**: Kotlin Coroutines & Asynchronous Shell Executor
* **Direktori Modul Target**: Universal `/data/adb/modules/`
