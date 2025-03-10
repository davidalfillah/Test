package com.example.test.ui.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.test.ui.dataType.Address
import com.example.test.ui.dataType.Branch
import com.example.test.ui.dataType.BranchLevel
import com.example.test.ui.dataType.BranchLocation
import com.example.test.ui.dataType.Member
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream

open class MemberViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _member = MutableLiveData<Member?>()
    val member: LiveData<Member?> get() = _member

    fun setMember(member: Member) {
        _member.value = member
    }

    fun uploadImageToFirebase(
        context: Context, uri: Uri, label: String,
        onSuccess: (String) -> Unit, onFailure: (String) -> Unit
    ) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val fileName = "${label + System.currentTimeMillis()}.jpg"
        val fileRef = storageRef.child("$label/$fileName")

        // Upload file ke Firebase Storage
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                    Toast.makeText(context, "Upload berhasil: $downloadUri", Toast.LENGTH_LONG).show()
                    Log.d("Upload", "Download URL: $downloadUri")
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Upload gagal")
                Toast.makeText(context, "Upload gagal: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("Upload", "Gagal upload: ${exception.message}")
            }
    }

    fun registerMember(
        context: Context,
        fotoUri: Uri, ktpUri: Uri,
        userId: String,
        fullName: String, nik: String, birthDate: Timestamp, gender: String, religion: String,
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

                // 2️⃣ Upload Foto KTP
                uploadImageToFirebase(context, ktpUri, "KTP",
                    onSuccess = { ktpUrl ->
                        // 3️⃣ Upload Foto KTA
                        uploadImageToFirebase(context, fotoUri, "KTA",
                            onSuccess = { fotoUrl ->

                                // 4️⃣ Cari atau buat cabang berdasarkan lokasi
                                findOrCreateBranchHierarchy(subDistrict, city, province) { branchId, branchLevel ->

                                    // 5️⃣ Generate Member ID sebelum menyimpan data
                                    generateMemberId { memberId ->
                                        val newMember = Member(
                                            userId = userId,
                                            memberId = memberId,
                                            fullName = fullName,
                                            nik = nik,
                                            birthDate = birthDate,
                                            joinDateDay = Timestamp.now(),
                                            gender = gender,
                                            religion = religion,
                                            education = education,
                                            phone = phone,
                                            address = Address(
                                                street,
                                                village,
                                                subDistrict,
                                                city,
                                                province,
                                                postalCode
                                            ),
                                            branchId = branchId,
                                            branchLevel = branchLevel,
                                            job = job,
                                            jobTitle = jobTitle,
                                            fotoUrl = fotoUrl,
                                            ktpUrl = ktpUrl
                                        )

                                        // 6️⃣ Simpan Data Member
                                        db.collection("members").document(memberId)
                                            .set(newMember)
                                            .addOnSuccessListener { onResult(true, "Pendaftaran berhasil!") }
                                            .addOnFailureListener { onResult(false, "Pendaftaran gagal!") }
                                    }
                                }
                            },
                            onFailure = { errorMessage ->
                                onResult(false, "Gagal upload Foto KTA: $errorMessage")
                            }
                        )
                    },
                    onFailure = { errorMessage ->
                        onResult(false, "Gagal upload Foto KTP: $errorMessage")
                    }
                )
            }
            .addOnFailureListener { onResult(false, "Terjadi kesalahan!") }
    }


    open fun fetchMember(userId: String, onResult: (Member?, Branch?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("members")
            .whereEqualTo(
                "userId",
                userId
            ) // ✅ Cari berdasarkan field userId di dalam koleksi "members"
            .limit(1) // 🔹 Hanya ambil satu data (userId unik)
            .get()
            .addOnSuccessListener { documents ->
                val memberDoc =
                    documents.documents.firstOrNull() // ✅ Ambil dokumen pertama jika ada

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
                                onResult(
                                    member,
                                    null
                                ) // ❌ Jika gagal mengambil branch, tetap kirim member
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
                        .update(
                            "umkmIds",
                            com.google.firebase.firestore.FieldValue.arrayUnion(umkmId)
                        )
                        .addOnSuccessListener { onResult(true, "UMKM berhasil didaftarkan!") }
                        .addOnFailureListener { onResult(false, "Gagal memperbarui data member!") }
                }
                .addOnFailureListener { onResult(false, "Gagal mendaftarkan UMKM!") }
        }
    }


    private fun generateUmkmId(callback: (String) -> Unit) {
        val prefix = "GUMKM"
        val dbRef = db.collection("umkm")

        dbRef.orderBy("umkmId", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(1)
            .get()
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
                    val lastNumber =
                        lastMember.getString("memberId")?.removePrefix("GRIB")?.toIntOrNull() ?: 0
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
                val dppId = if (!dppDocs.isEmpty) dppDocs.documents[0].id else createBranch(
                    "DPP",
                    "Pusat",
                    BranchLocation(),
                    BranchLevel.DPP
                )

                // 2️⃣ Cari atau buat DPD (Provinsi)
                db.collection("branches").whereEqualTo("location.province", province)
                    .whereEqualTo("level", "DPD").get()
                    .addOnSuccessListener { dpdDocs ->
                        val dpdId = if (!dpdDocs.isEmpty) dpdDocs.documents[0].id else createBranch(
                            dppId,
                            province,
                            BranchLocation(province = province),
                            BranchLevel.DPD
                        )

                        // 3️⃣ Cari atau buat DPC (Kabupaten/Kota)
                        db.collection("branches").whereEqualTo("location.city", city)
                            .whereEqualTo("level", "DPC").get()
                            .addOnSuccessListener { dpcDocs ->
                                val dpcId =
                                    if (!dpcDocs.isEmpty) dpcDocs.documents[0].id else createBranch(
                                        dpdId,
                                        city,
                                        BranchLocation(city = city, province = province),
                                        BranchLevel.DPC
                                    )

                                // 4️⃣ Cari atau buat PAC (Kecamatan)
                                db.collection("branches")
                                    .whereEqualTo("location.subDistrict", subDistrict)
                                    .whereEqualTo("level", "PAC").get()
                                    .addOnSuccessListener { pacDocs ->
                                        val pacId =
                                            if (!pacDocs.isEmpty) pacDocs.documents[0].id else createBranch(
                                                dpcId,
                                                subDistrict,
                                                BranchLocation(subDistrict, city, province),
                                                BranchLevel.PAC
                                            )

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


    private val functions = FirebaseFunctions.getInstance()

    data class KTPData(
        val nik: String = "",
        val nama: String = "",
        val tempatTanggalLahir: String = "",
        val jenisKelamin: String = "",
        val alamat: String = ""
    )

    fun scanKTP(
        bitmap: Bitmap,
        onResult: (KTPData) -> Unit, // Callback untuk hasil sukses
        onError: (String) -> Unit    // Callback untuk error
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("KTP", "No authenticated user found")
            onError("Pengguna belum login. Silakan login terlebih dahulu.")
            return
        }

        // Log informasi pengguna dan token
        user.getIdToken(false).addOnSuccessListener { result ->
            Log.d("KTP", "User UID: ${user.uid}, Token: ${result.token}")
        }.addOnFailureListener { e ->
            Log.e("KTP", "Failed to get token: ${e.message}")
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        val bitmapIt = scaleBitmapDown(bitmap, 640)
        bitmapIt.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        Log.d("KTP", "Base64 length: ${base64Image.length}")

        val data = hashMapOf("image" to base64Image)
        Log.d("KTP", "Sending data keys: ${data.keys}, image length: ${data["image"]?.length}")

        functions
            .getHttpsCallable("scanKTP")
            .call(data)
            .addOnSuccessListener { result ->
                val resultData = result.data as? Map<*, *>
                val ktpData = KTPData(
                    nik = resultData?.get("nik") as? String ?: "",
                    nama = resultData?.get("nama") as? String ?: "",
                    tempatTanggalLahir = resultData?.get("tempatTanggalLahir") as? String ?: "",
                    jenisKelamin = resultData?.get("jenisKelamin") as? String ?: "",
                    alamat = resultData?.get("alamat") as? String ?: ""
                )
                onResult(ktpData)
            }
            .addOnFailureListener { e ->
                Log.e("KTP", "Function call failed: ${e.message}")
                onError("Error: ${e.message}")
            }
    }
}

private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val originalWidth = bitmap.width
    val originalHeight = bitmap.height
    var resizedWidth = maxDimension
    var resizedHeight = maxDimension
    if (originalHeight > originalWidth) {
        resizedHeight = maxDimension
        resizedWidth =
            (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
    } else if (originalWidth > originalHeight) {
        resizedWidth = maxDimension
        resizedHeight =
            (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
    } else if (originalHeight == originalWidth) {
        resizedHeight = maxDimension
        resizedWidth = maxDimension
    }
    return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false)
}
