package com.example.test.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.ui.dataType.Address
import com.example.test.ui.dataType.Branch
import com.example.test.ui.dataType.BranchLevel
import com.example.test.ui.dataType.BranchLocation
import com.example.test.ui.dataType.Member
import com.example.test.ui.dataType.Umkm
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

open class MemberViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    open fun fetchMember(userId: String, onResult: (Member?, Branch?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("members")
            .whereEqualTo("userId", userId) // ✅ Cari berdasarkan field userId di dalam koleksi "members"
            .limit(1) // 🔹 Hanya ambil satu data (userId unik)
            .get()
            .addOnSuccessListener { documents ->
                val memberDoc = documents.documents.firstOrNull() // ✅ Ambil dokumen pertama jika ada

                if (memberDoc != null) {
                    val member = memberDoc.toObject(Member::class.java)

                    // 🔹 Jika member punya branchId, ambil data cabangnya
                    if (!member?.branchId.isNullOrEmpty()) {
                        db.collection("branches").document(member!!.branchId)
                            .get()
                            .addOnSuccessListener { branchDoc ->
                                val branch = branchDoc.toObject(Branch::class.java)
                                onResult(member, branch) // ✅ Callback dengan member & branch
                            }
                            .addOnFailureListener {
                                onResult(member, null) // ❌ Jika gagal mengambil branch, tetap kirim member
                            }
                    } else {
                        onResult(member, null) // 🔹 Jika member tidak punya branch
                    }
                } else {
                    onResult(null, null) // ❌ Jika member tidak ditemukan
                }
            }
            .addOnFailureListener {
                onResult(null, null) // ❌ Jika query gagal, kembalikan null
            }
    }



    fun registerMember(
        userId: String, // Tambahkan userId sebagai parameter
        fullName: String, nik: String, birthDate: String, gender: String, religion: String,
        education: String, phone: String, street: String, village: String, subDistrict: String,
        city: String, province: String, postalCode: String, jobTitle: String, job: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        // 1️⃣ Cek apakah NIK sudah terdaftar
        db.collection("members").whereEqualTo("nik", nik).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    onResult(false, "NIK sudah terdaftar!")
                    return@addOnSuccessListener
                }

                // 2️⃣ Cari atau buat cabang berdasarkan lokasi
                findOrCreateBranchHierarchy(subDistrict, city, province) { branchId, branchLevel ->

                    // 3️⃣ Generate Member ID sebelum menyimpan data
                    generateMemberId { memberId ->
                        val newMember = Member(
                            userId = userId,  // Simpan userId di dalam data member
                            memberId = memberId,
                            fullName = fullName,
                            nik = nik,
                            birthDate = birthDate,
                            gender = gender,
                            religion = religion,
                            education = education,
                            phone = phone,
                            address = Address(street, village, subDistrict, city, province, postalCode),
                            branchId = branchId,
                            branchLevel = branchLevel,
                            job = job,
                            jobTitle = jobTitle
                        )

                        // 4️⃣ Simpan Data Member
                        db.collection("members").document(memberId)
                            .set(newMember)
                            .addOnSuccessListener { onResult(true, "Pendaftaran berhasil!") }
                            .addOnFailureListener { onResult(false, "Pendaftaran gagal!") }
                    }
                }
            }
            .addOnFailureListener { onResult(false, "Terjadi kesalahan!") }
    }

    fun registerUmkm(
        memberId: String,
        name: String,
        businessType: String,
        description: String,
        street: String,
        village: String,
        subDistrict: String,
        city: String,
        province: String,
        postalCode: String,
        contact: String,
        onResult: (Boolean, String) -> Unit
    ) {
        generateUmkmId { umkmId ->
            val newUmkm = hashMapOf(
                "umkmId" to umkmId,
                "memberId" to memberId,
                "name" to name,
                "businessType" to businessType,
                "description" to description,
                "address" to mapOf(
                    "street" to street,
                    "village" to village,
                    "subDistrict" to subDistrict,
                    "city" to city,
                    "province" to province,
                    "postalCode" to postalCode
                ),
                "contact" to contact,
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("umkm").document(umkmId).set(newUmkm)
                .addOnSuccessListener {
                    db.collection("members").document(memberId)
                        .update("umkmIds", com.google.firebase.firestore.FieldValue.arrayUnion(umkmId))
                        .addOnSuccessListener { onResult(true, "UMKM berhasil didaftarkan!") }
                        .addOnFailureListener { onResult(false, "Gagal memperbarui data member!") }
                }
                .addOnFailureListener { onResult(false, "Gagal mendaftarkan UMKM!") }
        }
    }


    private fun generateUmkmId(callback: (String) -> Unit) {
        val prefix = "GUMKM"
        val dbRef = db.collection("umkm")

        dbRef.orderBy("umkmId", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(1).get()
            .addOnSuccessListener { documents ->
                val lastId = if (!documents.isEmpty) {
                    val lastUmkmId = documents.documents[0].getString("umkmId") ?: ""
                    val lastNumber = lastUmkmId.removePrefix(prefix).toIntOrNull() ?: 0
                    lastNumber + 1
                } else {
                    1
                }
                val newUmkmId = "$prefix${String.format("%03d", lastId)}"
                callback(newUmkmId)
            }
            .addOnFailureListener {
                callback("${prefix}001") // Jika gagal mendapatkan data, mulai dari 001
            }
    }


    fun getMemberByUserId(userId: String, callback: (Member?) -> Unit) {
        db.collection("members").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val member = documents.documents[0].toObject(Member::class.java)
                    callback(member)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }





    private fun generateMemberId(callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("members").orderBy("memberId", Query.Direction.DESCENDING).limit(1).get()
            .addOnSuccessListener { documents ->
                val lastId = if (!documents.isEmpty) {
                    val lastMember = documents.documents[0]
                    val lastNumber = lastMember.getString("memberId")?.removePrefix("GRIB")?.toIntOrNull() ?: 0
                    lastNumber + 1
                } else {
                    1
                }

                val newMemberId = "GRIB%03d".format(lastId)
                callback(newMemberId)
            }
            .addOnFailureListener { callback("GRIB001") }
    }



    private fun findOrCreateBranchHierarchy(
        subDistrict: String, city: String, province: String,
        callback: (String, BranchLevel) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        // 1️⃣ Cari atau buat DPP (Pusat)
        db.collection("branches").whereEqualTo("level", "DPP").get()
            .addOnSuccessListener { dppDocs ->
                val dppId = if (!dppDocs.isEmpty) dppDocs.documents[0].id else createBranch("DPP", "Pusat", BranchLocation(), BranchLevel.DPP)

                // 2️⃣ Cari atau buat DPD (Provinsi)
                db.collection("branches").whereEqualTo("location.province", province).whereEqualTo("level", "DPD").get()
                    .addOnSuccessListener { dpdDocs ->
                        val dpdId = if (!dpdDocs.isEmpty) dpdDocs.documents[0].id else createBranch(dppId, province, BranchLocation(province = province), BranchLevel.DPD)

                        // 3️⃣ Cari atau buat DPC (Kabupaten/Kota)
                        db.collection("branches").whereEqualTo("location.city", city).whereEqualTo("level", "DPC").get()
                            .addOnSuccessListener { dpcDocs ->
                                val dpcId = if (!dpcDocs.isEmpty) dpcDocs.documents[0].id else createBranch(dpdId, city, BranchLocation(city = city, province = province), BranchLevel.DPC)

                                // 4️⃣ Cari atau buat PAC (Kecamatan)
                                db.collection("branches").whereEqualTo("location.subDistrict", subDistrict).whereEqualTo("level", "PAC").get()
                                    .addOnSuccessListener { pacDocs ->
                                        val pacId = if (!pacDocs.isEmpty) pacDocs.documents[0].id else createBranch(dpcId, subDistrict, BranchLocation(subDistrict, city, province), BranchLevel.PAC)

                                        // Callback dengan PAC yang ditemukan/dibuat
                                        callback(pacId, BranchLevel.PAC)
                                    }
                            }
                    }
            }
    }

    // Fungsi untuk Membuat Branch Baru
    private fun createBranch(
        parentId: String, name: String, location: BranchLocation, level: BranchLevel
    ): String {
        val db = FirebaseFirestore.getInstance()
        val newBranchId = db.collection("branches").document().id

        val newBranch = Branch(
            branchId = newBranchId,
            name = name,
            location = location,
            level = level
        )

        db.collection("branches").document(newBranchId).set(newBranch)
        return newBranchId
    }


}

