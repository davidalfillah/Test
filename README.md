# Hello GRIB - Aplikasi Keanggotaan

## 📌 Deskripsi
Aplikasi Android ini dikembangkan untuk **Organisasi Masyarakat GRIB (Gerakan Rakyat Indonesia Bersatu)** dengan fitur utama seperti **iuran bulanan, sistem donasi, dan sistem iklan**. Aplikasi ini menggunakan **Xendit** sebagai solusi pembayaran.

> **Catatan:** Aplikasi ini masih dalam tahap pengembangan, sehingga masih banyak fitur yang belum sempurna atau mengalami perubahan.

---

## 📷 Screenshot Aplikasi

| Beranda | Menu Pembayaran | Form Donasi |
|---------|---------------|-------------|
| ![Screenshot 1](screenshots/screenshot1.png) | ![Screenshot 2](screenshots/screenshot2.png) | ![Screenshot 3](screenshots/screenshot3.png) |

---

## 📥 Unduh Aplikasi Debug
Anda dapat mengunduh dan menguji aplikasi versi debug melalui tautan berikut:

🔗 **[Download APK Debug](https://example.com/debug-apk)** *(Ganti dengan link asli setelah diunggah)*

---

## 🔑 Informasi Login & OTP Test
Untuk melakukan pengujian login dengan nomor telepon dan OTP, silakan gunakan detail berikut:

- **Nomor Telepon:** `+6281234567890`
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

📧 **Email:** [email@example.com](mailto:email@example.com)  
📱 **WhatsApp:** +62xxxxxxxxxxx  

Kami sangat menghargai masukan dan dukungan Anda! 🚀

