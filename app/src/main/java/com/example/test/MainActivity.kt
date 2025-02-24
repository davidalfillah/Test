package com.example.test

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.test.ui.MainScreen
import com.example.test.ui.screens.User
import com.example.test.ui.theme.TestTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit




class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId

    private val _authState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val authState: StateFlow<FirebaseUser?> = _authState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}"

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

    // Menyimpan sesi ke Firestore
    private suspend fun saveSession(user: FirebaseUser) {
        val sessionData = mapOf(
            "user_id" to user.uid,
            "device" to deviceInfo,
            "ip_address" to getPublicIpAddress(),
            "login_time" to System.currentTimeMillis(),
            "expires_at" to System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 jam sesi
        )

        firestore.collection("sessions").document(user.uid)
            .set(sessionData, SetOptions.merge()).await()
    }

    // Menghapus sesi saat logout
    private suspend fun deleteSession(userId: String) {
        firestore.collection("sessions").document(userId).delete().await()
    }


    fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(User::class.java)
                    Log.d("AuthRepository", "User ditemukan: $userData") // Debug
                    _user.value = userData
                } else {
                    Log.d("AuthRepository", "User tidak ditemukan di Firestore")
                }
            }
            .addOnFailureListener { exception ->
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

    suspend fun checkSession(userId: String): String? {
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
                    val isProfileComplete = document.getBoolean("isProfileComplete") ?: false
                    onSuccess(isProfileComplete) // Kirim status profil ke UI
                } else {
                    val newUser = hashMapOf(
                        "uid" to user.uid,
                        "phone" to user.phoneNumber,
                        "profilePicUrl" to document.getString("profilePicUrl"),
                        "role" to document.getString("role"),
                        "isProfileComplete" to false,
                        "createdAt" to System.currentTimeMillis()
                    )
                    userRef.set(newUser).addOnSuccessListener {
                        onSuccess(false) // User baru, arahkan ke profil setup
                    }.addOnFailureListener {
                        onError("Gagal menyimpan data pengguna.")
                    }
                }
            }.addOnFailureListener {
                onError("Gagal mengambil data pengguna.")
            }
        } else {
            onError("Pengguna tidak ditemukan.")
        }
    }

    /**
     * Logout pengguna
     */

    suspend fun logout() {
        val userId = _user.value?.uid ?: return // Jika user null, tidak perlu lanjut

        deleteSession(userId) // Hapus sesi dari Firestore
        auth.signOut()

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

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    val authState = authRepository.authState
    val user = authRepository.user

    init {
        fetchUserData()
    }

    fun sendOtp(phoneNumber: String, activity: Activity, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        authRepository.sendOtp(phoneNumber, activity, onSuccess, onError)
    }

    fun resendOtp(phoneNumber: String, activity: Activity, onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) {
        authRepository.sendOtp(phoneNumber, activity, onSuccess, onError)
    }

    fun fetchUserData() {
        authRepository.fetchUserData()
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






class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // ✅ Aktifkan mode Edge-to-Edge
        super.onCreate(savedInstanceState)

        setContent {
            TestTheme {
                val authViewModel = remember { AuthViewModel() } // ✅ Gunakan remember agar tidak reset saat recomposition
                MainScreen(authViewModel)
            }
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

