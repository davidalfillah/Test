package com.example.test

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.core.provider.FontRequest
import androidx.core.view.WindowInsetsControllerCompat
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.test.ui.MainScreen
import com.example.test.ui.screens.User
import com.example.test.ui.theme.TestTheme
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.xendit.Xendit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId

    private val _isProfileComplete = MutableStateFlow(false)
    val isProfileComplete: StateFlow<Boolean> = _isProfileComplete

    private val _authState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()


    fun getPublicIpAddress(): String {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url("https://api64.ipify.org?format=json").build()
            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")
            json.getString("ip")
        } catch (e: Exception) {
            "0.0.0.0"
        }
    }

    fun checkMemberStatus(userId: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("members")
            .whereEqualTo("userId", userId) // ✅ Cari berdasarkan field userId di dalam dokumen members
            .limit(1) // 🔹 Ambil hanya satu hasil, karena userId harus unik
            .get()
            .addOnSuccessListener { documents ->
                onResult(!documents.isEmpty) // ✅ True jika ada hasil, False jika tidak
            }
            .addOnFailureListener {
                onResult(false) // 🔴 Jika query gagal, anggap belum menjadi anggota
            }
    }



    // Menyimpan sesi ke Firestore
    private suspend fun saveSession(user: FirebaseUser) {
        val sessionData = mapOf(
            "user_id" to user.uid,
            "device" to deviceInfo,
            "ip_address" to getPublicIpAddress(),
            "login_time" to Timestamp.now(),
            "expires_at" to Timestamp.now()
        )

        firestore.collection("sessions").document(user.uid)
            .set(sessionData, SetOptions.merge()).await()
    }

    // Menghapus sesi saat logout
    private suspend fun deleteSession(userId: String) {
        firestore.collection("sessions").document(userId).delete().await()
    }

    val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}"

    fun fetchUserData() {
        Log.d("AuthRepository", "fetchUserData dipanggil")

        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(User::class.java)
                    val isProfileComplete = document.getBoolean("isProfileComplete") ?: false

                    userData?.let {
                        val updatedUser = it.copy(isProfileComplete = isProfileComplete)
                        _user.value = updatedUser
                        _isProfileComplete.value = isProfileComplete
                    }
                    Log.d("AuthRepository", "User ditemukan: $_user") // Debugging
                } else {
                    Log.d("AuthRepository", "User tidak ditemukan di Firestore")
                }
            }.addOnFailureListener { exception ->
                Log.e("AuthRepository", "Gagal mengambil data pengguna", exception)
        }
    }



    /**
     * Mengirim OTP ke nomor HP pengguna
     */
    fun sendOtp(phoneNumber: String, activity: Activity, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential, onSuccess = onSuccess, onError = onError)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onError(e.message ?: "Verifikasi gagal")
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _verificationId.value = verificationId
                    onSuccess(false)
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Memverifikasi OTP yang dimasukkan pengguna
     */
    fun verifyOtp(otpCode: String, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        val credential = _verificationId.value?.let { PhoneAuthProvider.getCredential(it, otpCode) }
        if (credential != null) {
            signInWithCredential(credential, onSuccess, onError)
        } else {
            onError("Kode verifikasi tidak valid")
        }
    }

    /**
     * Sign in dengan PhoneAuthCredential dan cek apakah pengguna baru
     */
    private fun signInWithCredential(
        credential: PhoneAuthCredential,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    _authState.value = firebaseUser

                    if (firebaseUser == null) {
                        onError("User tidak ditemukan setelah login")
                        return@addOnCompleteListener
                    }

                    // 🔹 Gunakan Coroutine untuk operasi async Firestore
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val currentSessionId = checkSession(firebaseUser.uid)
                            if (currentSessionId != null) {
                                onError("Akun sedang digunakan di perangkat lain")
                                return@launch
                            }

                            saveSession(firebaseUser) // ✅ Simpan sesi ke Firestore
                            checkUserExists(
                                onSuccess = { isProfileComplete ->
                                    fetchUserData() // ✅ Ambil data user dari Firestore
                                    onSuccess(isProfileComplete) // 🔹 Pastikan ini dipanggil terakhir
                                },
                                onError = onError
                            )
                        } catch (e: Exception) {
                            onError(e.localizedMessage ?: "Terjadi kesalahan saat login")
                        }
                    }
                } else {
                    onError(task.exception?.localizedMessage ?: "Login gagal")
                }
            }
    }

    private suspend fun checkSession(userId: String): String? {
        val sessionDoc = firestore.collection("sessions").document(userId).get().await()
        return sessionDoc.getString("sessionId") // 🔹 Jika null, berarti tidak ada session aktif
    }



    /**
     * Mengecek apakah user sudah ada di Firestore, jika tidak maka buat baru
     */
    private fun checkUserExists(onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userRef = firestore.collection("users").document(user.uid)
            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val isComplete = document.getBoolean("isProfileComplete")
                    Log.d("AuthRepository", "isProfileComplete dari Firestore: $isComplete")
                    _isProfileComplete.value = isComplete ?: false
                    onSuccess(_isProfileComplete.value)
                } else {
                    Log.d("AuthRepository", "Dokumen pengguna tidak ditemukan, membuat data baru...")
                    val newUser = hashMapOf(
                        "uid" to user.uid,
                        "phone" to user.phoneNumber,
                        "isProfileComplete" to false,
                        "isOnline" to false,
                        "createdAt" to Timestamp.now()
                    )
                    userRef.set(newUser).addOnSuccessListener {
                        Log.d("AuthRepository", "User baru dibuat dengan isProfileComplete = false")
                        _isProfileComplete.value = false
                        onSuccess(false)
                    }.addOnFailureListener { error ->
                        onError("Gagal menyimpan data pengguna: ${error.message}")
                    }
                }
            }.addOnFailureListener { error ->
                onError("Gagal mengambil data pengguna: ${error.message}")
            }

        } else {
            onError("Pengguna tidak ditemukan.")
        }
    }

    fun updateUserStatus(isOnline: Boolean, userId: String) {
        val realtimeDbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        val firestoreDbRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        Log.d("FirebaseDB", "Updating status for user: $userId, isOnline: $isOnline")

        val statusData = mapOf(
            "isOnline" to isOnline,
            "lastSeen" to Timestamp.now()
        )

        // ✅ Update status di Firestore
        firestoreDbRef.update(statusData)
            .addOnSuccessListener { Log.d("Firestore", "User status updated in Firestore") }
            .addOnFailureListener { Log.e("Firestore", "Failed to update user status in Firestore", it) }

        // ✅ Update status di Realtime Database
        if (isOnline) {
            realtimeDbRef.setValue(mapOf("isOnline" to true))
            realtimeDbRef.onDisconnect().setValue(mapOf(
                "isOnline" to false,
                "lastSeen" to Timestamp.now()
            ))
        } else {
            realtimeDbRef.setValue(mapOf(
                "isOnline" to false,
                "lastSeen" to Timestamp.now()
            ))
        }
    }








    /**
     * Logout pengguna
     */

    suspend fun logout() {
        val userId = _user.value?.uid ?: return // Jika user null, tidak perlu lanjut

        deleteSession(userId) // Hapus sesi dari Firestore
        auth.signOut()

        updateUserStatus(isOnline = false, userId)

        _authState.value = null
        _user.value = null
    }


    /**
     * Load user saat aplikasi dibuka kembali
     */
    fun loadUser() {
        _authState.value = auth.currentUser
    }
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val authState = authRepository.authState
    val user = authRepository.user
    val isProfileComplete: StateFlow<Boolean> = authRepository.isProfileComplete



    init {
        fetchUserData()
    }
    fun updateUserStatusRealtime(isOnline: Boolean, userId: String) {
        authRepository.updateUserStatus(isOnline, userId)
    }

    fun checkMemberStatus(userId: String, onResult: (Boolean) -> Unit) {
        authRepository.checkMemberStatus(userId, onResult)
    }


    fun sendOtp(phoneNumber: String, activity: Activity, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        authRepository.sendOtp(phoneNumber, activity, onSuccess, onError)
    }

    private fun fetchUserData() {
        authRepository.fetchUserData()
    }

    fun resendOtp(phoneNumber: String, activity: Activity, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        authRepository.sendOtp(phoneNumber, activity, onSuccess, onError)
    }


    fun verifyOtp(otpCode: String, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        authRepository.verifyOtp(otpCode, onSuccess, onError)
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout() // Pastikan repository menangani sign out
            delay(2000) // Simulasi animasi loading logout
            onComplete() // Callback untuk pindah ke halaman login
        }
    }
}



@Composable
fun DashboardScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val user by authViewModel.authState.collectAsState()
    val firestore = FirebaseFirestore.getInstance()
    var role by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    role = document.getString("role")
                }
        }
    }


    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate("home")
        }
    } else {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text("Welcome, ${user?.phoneNumber ?: "User"}")
            if (role == "admin") {
                Button(onClick = { navController.navigate("admin") }) { Text("Go to Admin") }
            }
            Button(onClick = {
                navController.navigate("home")
            }) {
                Text("Logout")
            }
        }
    }
}


@Composable
fun AdminScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val user by authViewModel.authState.collectAsState()
    val firestore = FirebaseFirestore.getInstance()
    var role by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    role = document.getString("role")
                }
        }
    }

    when {
        user == null -> {
            LaunchedEffect(Unit) {
                navController.navigate("home")
            }
        }
        role == null -> {
            CircularProgressIndicator()
        }
        role != "admin" -> {
            LaunchedEffect(Unit) {
                navController.navigate("dashboard")
            }
        }
        else -> {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text("Admin Panel", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Button(onClick = { navController.navigate("dashboard") }) { Text("Back to Dashboard") }
            }
        }
    }
}


class AuthViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}






class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
//            val firebaseAppCheck = FirebaseAppCheck.getInstance()
//            firebaseAppCheck.installAppCheckProviderFactory(
//                DebugAppCheckProviderFactory.getInstance()
//            )
        } catch (e: DatabaseException) {
            Log.e("Firebase", "Persistence sudah diaktifkan sebelumnya")
        }
        super.onCreate(savedInstanceState)

        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs
        )
        val config = FontRequestEmojiCompatConfig(applicationContext, fontRequest)
        EmojiCompat.init(config)

        val authRepository = AuthRepository()
        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(authRepository)
        )[AuthViewModel::class.java]


        setContent {
            TestTheme {
                MainScreen(authViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            authViewModel.user.collectLatest { user ->
                user?.uid?.let { userId ->
                    Log.d("FirebaseDB", "User $userId is now ONLINE")
                    authViewModel.updateUserStatusRealtime(true, userId) // ✅ Pastikan hanya dipanggil jika user valid
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        val userId = authViewModel.user.value?.uid
        if (userId != null) {
            Log.d("FirebaseDB", "User $userId is now OFFLINE")
            authViewModel.updateUserStatusRealtime(false, userId) // ✅ Set offline saat aplikasi ditutup
        }
    }
}





@SuppressLint("ComposableNaming")
@Composable
fun setStatusBarColor(color: Color, useDarkIcons: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(color, useDarkIcons) { // Tambahkan useDarkIcons sebagai dependency
            val window = (view.context as Activity).window
            window.statusBarColor = color.toArgb()

            // Atur warna ikon status bar (gelap/terang)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = useDarkIcons
        }
    }
}
