# Hello GRIB - Aplikasi Keanggotaan

## 📌 Deskripsi
Aplikasi Android ini dikembangkan untuk **Organisasi Masyarakat GRIB (Gerakan Rakyat Indonesia Bersatu)** dengan fitur utama seperti **iuran bulanan, sistem donasi, dan sistem iklan**. Aplikasi ini menggunakan **Xendit** sebagai solusi pembayaran.

> **Catatan:** Aplikasi ini masih dalam tahap pengembangan, sehingga masih banyak fitur yang belum sempurna atau mengalami perubahan.

---

## 📸 Screenshot Aplikasi

### **Sebelum Login**
| Splash Screen | Home | Marketplace |
|--------------|------|------------|
| ![Splash Screen](screenshots/Screenshot_20250307_210611.png) | ![Home](screenshots/Screenshot_20250307_210641.png) | ![Marketplace](screenshots/Screenshot_20250307_210658.png) |

| Chat | Akun |
|------|------|
| ![Chat](screenshots/Screenshot_20250307_210715.png) | ![Akun](screenshots/Screenshot_20250307_210725.png) |

### **Proses Login**
| Input No HP | Input OTP | Status Sukses Login |
|------------|----------|----------------------|
| ![Input No HP](screenshots/Screenshot_20250307_210737.png) | ![Input OTP](screenshots/Screenshot_20250307_210756.png) | ![Status Sukses Login](screenshots/Screenshot_20250307_210845.png) |

### **Setelah Login**
| Home | Marketplace | Chat | Akun |
|------|------------|------|------|
| ![Home](screenshots/Screenshot_20250307_210855.png) | ![Marketplace](screenshots/Screenshot_20250307_210658.png) | ![Chat](screenshots/Screenshot_20250307_211133.png) | ![Akun](screenshots/Screenshot_20250307_211142.png) |

| News | News Detail | Register UMKM | KTA |
|------|------------|---------------|-----|
| ![News](screenshots/Screenshot_20250307_210907.png) | ![News Detail](screenshots/Screenshot_20250307_210920.png) | ![Register UMKM](screenshots/Screenshot_20250307_210934.png) | ![KTA](screenshots/Screenshot_20250307_210946.png) |

| Biodata Member | Kartu Anggota Digital | Donasi | Donasi Detail |
|---------------|----------------------|--------|--------------|
| ![Biodata Member](screenshots/Screenshot_20250307_210956.png) | ![Kartu Anggota Digital](screenshots/Screenshot_20250307_211008.png) | ![Donasi](screenshots/Screenshot_20250307_211024.png) | ![Donasi Detail](screenshots/Screenshot_20250307_211033.png) |

| Input Donasi | Tentang Hello GRIB | Pengurus Pusat | Ajak Teman |
|-------------|-------------------|---------------|-----------|
| ![Input Donasi](screenshots/Screenshot_20250307_211042.png) | ![Tentang Hello GRIB](screenshots/Screenshot_20250307_211106.png) | ![Pengurus Pusat](screenshots/Screenshot_20250307_211115.png) | ![Ajak teman](screenshots/Screenshot_20250307_211155.png) |


## 📥 Unduh Aplikasi Debug
Anda dapat mengunduh dan menguji aplikasi versi debug melalui tautan berikut:

🔗 **[Download APK Debug](https://drive.google.com/file/d/108ZVZrxZaLA1zMI5k3OT3EMO0PYjUgOG/view?usp=drive_link)**

---

## 🔑 Informasi Login & OTP Test
Untuk melakukan pengujian login dengan nomor telepon dan OTP, silakan gunakan detail berikut:

- **Nomor Telepon:** `+6283822158268`
- **Kode OTP:** `000000`

---

## 💳 Integrasi Xendit
Sistem pembayaran dalam aplikasi ini menggunakan **Xendit** dan dapat ditemukan di dalam kode berikut:

📂 **Test/app/src/main/java/com/example/test/ui/viewModels/PaymentViewModel.kt**

Pada file ini, implementasi pembayaran Xendit telah dilakukan, termasuk Virtual Account, QRIS, dan metode pembayaran lainnya yang didukung oleh Xendit.

> **Catatan:** Implementasi pembayaran masih dalam proses pengerjaan dan belum sepenuhnya selesai.

---

## ☁️ Callback dengan Cloud Functions
Aplikasi ini juga menggunakan **Firebase Cloud Functions** untuk menangani callback dari pembayaran Xendit dan berbagai fitur lainnya.

---

## 🚀 Fitur yang Sudah Tersedia
✅ Registrasi & Login dengan OTP (Firebase Authentication)  
✅ Sistem iuran bulanan untuk anggota  
✅ Donasi dengan kategori proyek  
✅ Sistem pemasangan iklan dalam aplikasi  
✅ Integrasi Xendit untuk pembayaran  
✅ Callback menggunakan Firebase Cloud Functions  

---

## 🔧 Fitur yang Masih Dalam Pengembangan
🔹 Notifikasi real-time untuk pembayaran  
🔹 Dashboard administrasi  
🔹 Peningkatan UI/UX  
🔹 Website versi pendamping  
🔹 Penyempurnaan sistem pembayaran dengan Xendit  

---

## 📱 Halaman yang Sudah Dibuat
Aplikasi **Hello GRIB** telah memiliki beberapa halaman utama, yaitu:
- **Registrasi & Login**
- **Home**
- **Donations**
- **Chat**
- **Account Setting**
- **News**
- **KTA**
- **Dan lain-lain**

---

## 📩 Kontak & Kontribusi
Jika Anda ingin memberikan saran, laporan bug, atau berkontribusi dalam pengembangan aplikasi ini, silakan hubungi:

📧 **Email:** [syaifulasifuddin7@gmail.com](mailto:syaifulasifuddin7@gmail.com)  
📱 **WhatsApp:** +6283822158268

Kami sangat menghargai masukan dan dukungan Anda! 🚀
