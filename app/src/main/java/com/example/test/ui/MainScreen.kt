package com.example.test.ui


import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.test.AdminScreen
import com.example.test.AuthRepository
import com.example.test.AuthViewModel
import com.example.test.DashboardScreen
import com.example.test.R
import com.example.test.setStatusBarColor
import com.example.test.ui.screens.AboutGribScreen
import com.example.test.ui.screens.AccountScreen
import com.example.test.ui.screens.ChatDetailScreen
import com.example.test.ui.screens.ChatsScreen
import com.example.test.ui.screens.CustomCamera
import com.example.test.ui.screens.DigitalCardScreen
import com.example.test.ui.screens.DonationDetailScreen
import com.example.test.ui.screens.DonationInputScreen
import com.example.test.ui.screens.DonationScreen
import com.example.test.ui.screens.FullscreenImageScreen
import com.example.test.ui.screens.HomeKtaScreen
import com.example.test.ui.screens.HomeScreen
import com.example.test.ui.screens.LoginScreen
import com.example.test.ui.screens.MemberProfileScreen
import com.example.test.ui.screens.NewsDetailScreen
import com.example.test.ui.screens.NewsScreen
import com.example.test.ui.screens.OtpScreen
import com.example.test.ui.screens.PaymentScreen
import com.example.test.ui.screens.ProductCategoryScreen
import com.example.test.ui.screens.ProfileSetupScreen
import com.example.test.ui.screens.RegistrationScreen
import com.example.test.ui.screens.RegistrationUmkmScreen
import com.example.test.ui.screens.SearchScreen
import com.example.test.ui.screens.ShoppingScreen
import com.example.test.ui.screens.StatusScreen
import com.example.test.ui.screens.SuccessScreen
import com.example.test.ui.screens.UploadKtpScreen
import com.example.test.ui.screens.saveBitmapToCache
import com.example.test.ui.viewModels.ChatViewModel
import com.example.test.ui.viewModels.MemberViewModel
import com.example.test.ui.viewModels.NewsViewModel
import com.example.test.ui.viewModels.PaymentViewModel

//Email : hellogrib430@gmail.com
//Pass : Hell0@#$

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MainScreen(authViewModel: AuthViewModel = AuthViewModel(AuthRepository())) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"
    var errorMessage by remember { mutableStateOf<String?>(null) }

    setStatusBarColor(
        color = when (currentRoute) {
            "home" -> colorScheme.primary
            "aboutGrib" -> colorScheme.background
            "shopping" -> colorScheme.background
            "chat" -> colorScheme.primary
            "news_detail/{newsId}" -> colorScheme.primary
            "account" -> colorScheme.primary
            else -> colorScheme.primary
        },
        useDarkIcons = when (currentRoute) { // Gunakan warna ikon status bar yang sesuai
            "home" -> false
            "shopping" -> true
            "login" -> true
            "chat" -> false
            "digitalCard/{member}" -> true
            "biodataMember/{memberJson}" -> true
            "news_detail/{newsId}" -> true
            "news" -> false
            "homeKta/{userId}" -> true
            "aboutGrib" -> true
            "account" -> false
            "profile_setup" -> true
            else -> false
        }
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(
                    "home",
                    "shopping",
                    "chat",
                    "account",
                )
            ) {
                BottomNavigationBar(navController)
            }
        }
    ) { PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",

            ) {

            composable("searchNews") {
                SearchScreen(
                    navController,
                    NewsViewModel()
                )
            }

            composable("productCategories") {
                ProductCategoryScreen(
                    navController,
                    paddingValues = PaddingValues
                )
            }

            composable(
                route = "fullscreen/{startIndex}",
                arguments = listOf(navArgument("startIndex") { type = NavType.IntType })
            ) { backStackEntry ->

                // Ambil previousBackStackEntry untuk mendapatkan savedStateHandle
                val previousEntry = navController.previousBackStackEntry

                // Ambil daftar gambar dari savedStateHandle
                val imageUrls = previousEntry
                    ?.savedStateHandle
                    ?.get<List<Map<String, String>>>("imageUrls") ?: emptyList()

                // Konversi ke Pair agar mudah digunakan
                val imageData = imageUrls.map { Pair(it["url"] ?: "", it["title"] ?: "No Title") }

                // Ambil startIndex dari argument
                val startIndex = backStackEntry.arguments?.getInt("startIndex") ?: 0

                FullscreenImageScreen(navController, imageData, startIndex)
            }



            composable(
                route = "payment/{userId}/{relatedId}/{relatedType}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("relatedId") { type = NavType.StringType },
                    navArgument("relatedType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val relatedId = backStackEntry.arguments?.getString("relatedId") ?: ""
                val relatedType = backStackEntry.arguments?.getString("relatedType") ?: ""
                val viewModel = viewModel<PaymentViewModel>()

                // Tampilkan Toast di thread UI
                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        navController.context.showToast(it)
                        errorMessage = null // Reset setelah ditampilkan
                    }
                }

                PaymentScreen(
                    viewModel = viewModel,
                    userId = userId,
                    relatedId = relatedId,
                    relatedType = relatedType,
                    onPaymentSuccess = { transactionId, _ ->
                        navController.navigate("payment_success/$transactionId") {
                            popUpTo("payment/{userId}/{relatedId}/{relatedType}") {
                                inclusive = false
                            }
                        }
                    },
                    onPaymentError = { error ->
                        errorMessage = error // Set error untuk ditampilkan di UI
                    }
                )
            }

            composable(
                route = "payment_success/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                PaymentSuccessScreen(transactionId = transactionId)
            }
            composable("customCamera") {
                CustomCamera(navController) { capturedBitmap ->
                    val context = navController.context
                    var bitmap = capturedBitmap
                    var imageUri = saveBitmapToCache(context, capturedBitmap)
                }
            }
            composable("donation_input/{title}") { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: "general"
                DonationInputScreen(
                    title = title,
                    navController = navController
                )
            }
            composable("donation_detail/{donationId}") { backStackEntry ->
                val donationId =
                    backStackEntry.arguments?.getString("donationId") ?: return@composable
                DonationDetailScreen(navController, donationId, authViewModel = authViewModel)
            }

            // Rute baru untuk DonationPage
            composable("donations") {
                DonationScreen(
                    paddingValues = PaddingValues,
                    onGeneralDonationClick = { amount ->
                        println("Donasi umum: Rp $amount")
                    },
                    onCharityDonationClick = { charity, amount ->
                        println("Donasi untuk ${charity.title}: Rp $amount")
                    },
                    navController = navController // Tambahkan untuk navigasi kembali
                )
            }
            composable("aboutGrib") { backStackEntry ->
                AboutGribScreen(navController)
            }
            composable("news_detail/{newsId}") { backStackEntry ->
                val newsId = backStackEntry.arguments?.getString("newsId") ?: "1"
                NewsDetailScreen(newsId, navController)
            }
            composable("home") {
                HomeScreen(
                    navController,
                    PaddingValues, authViewModel
                )
            }
            composable("status") { StatusScreen(navController) }
            composable("biodataMember/{memberJson}") { backStackEntry ->
                val memberJson = backStackEntry.arguments?.getString("memberJson") ?: ""
                MemberProfileScreen(
                    navController = navController, memberJson, paddingValues = PaddingValues
                )
            }
            composable("homeKta/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                HomeKtaScreen(
                    navController = navController,
                    paddingValues = PaddingValues,
                    userId = userId
                )
            }
            composable("uploadKtp") {
                UploadKtpScreen(
                    navController,
                    MemberViewModel()
                )
            }

            composable(
                "registerGrib?nik={nik}&name={name}&address={address}&imageUrl={imageUrl}&birthDate={birthDate}",
                arguments = listOf(
                    navArgument("nik") { defaultValue = "" },
                    navArgument("name") { defaultValue = "" },
                    navArgument("address") { defaultValue = "" },
                    navArgument("imageUrl") { nullable = true },
                    navArgument("birthDate") { defaultValue = "" }
                )
            ) { backStackEntry ->
                val nik = backStackEntry.arguments?.getString("nik") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val address = backStackEntry.arguments?.getString("address") ?: ""
                val imageUrl = backStackEntry.arguments?.getString("imageUrl")
                val birthDate = backStackEntry.arguments?.getString("birthDate") ?: ""

                RegistrationScreen(
                    paddingValues = PaddingValues,
                    navController = navController,
                    authViewModel = authViewModel,
                    nik = nik,
                    fullName = name,
                    street = address,
                    imageUrl = imageUrl,
                    birthDate = birthDate
                )
            }
            composable("registerUmkm") {
                RegistrationUmkmScreen(
                    paddingValues = PaddingValues,
                    navController,
                    authViewModel = authViewModel
                )
            }
            composable("profile_setup") {
                ProfileSetupScreen(
                    navController,
                    authViewModel,
                    paddingValues = PaddingValues
                )
            }
            composable(
                "success?nextScreen={nextScreen}",
                arguments = listOf(navArgument("nextScreen") { defaultValue = "home" })
            ) { backStackEntry ->
                val nextScreen = backStackEntry.arguments?.getString("nextScreen") ?: "home"
                SuccessScreen(navController, nextScreen)
            }
            composable("news") { NewsScreen(navController, paddingValues = PaddingValues) }
            composable("shopping") { ShoppingScreen(navController, paddingValues = PaddingValues) }
            composable("chat") {
                ChatsScreen(
                    navController,
                    paddingValues = PaddingValues,
                    chatViewModel = ChatViewModel(),
                    authViewModel = authViewModel
                )
            }
            composable("chat_detail/{chatId}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                ChatDetailScreen(navController, ChatViewModel(), authViewModel, chatId)
            }
            composable("digitalCard/{member}") { backStackEntry ->
                val memberData = backStackEntry.arguments?.getString("member") ?: ""
                DigitalCardScreen(
                    navController = navController,
                    memberData = memberData,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                "otp_screen/{phoneNumber}",
                arguments = listOf(navArgument("phoneNumber") { var type = NavType.StringType })
            ) { backStackEntry ->
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                OtpScreen(navController, phoneNumber, authViewModel, paddingValues = PaddingValues)
            }


            composable("login") {
                LoginScreen(
                    navController,
                    authViewModel,
                    paddingValues = PaddingValues
                )
            }
            composable("dashboard") { DashboardScreen(navController, authViewModel) }
            composable("admin") { AdminScreen(navController, authViewModel) }
            composable("account") { AccountScreen(navController, authViewModel, PaddingValues) }
        }
    }


}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("shopping", "Marketplace", Icons.Default.ShoppingCart),
        BottomNavItem("chat", "Pesan", ImageVector.vectorResource(R.drawable.baseline_chat_24)),
        BottomNavItem("account", "Akun", Icons.Default.Person)
    )

    val currentRoute = navController.currentDestination?.route

    NavigationBar(
        containerColor = colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(elevation = 16.dp, shape = RectangleShape)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = isSelected,
                colors = NavigationBarItemColors(
                    selectedIconColor = colorScheme.onPrimary,
                    selectedTextColor = colorScheme.primary,
                    unselectedIconColor = colorScheme.onSurfaceVariant,
                    unselectedTextColor = colorScheme.onSurfaceVariant,
                    selectedIndicatorColor = colorScheme.primary,
                    disabledIconColor = colorScheme.primary,
                    disabledTextColor = colorScheme.primary
                ),
                onClick = {
                    if (!isSelected) { // Cegah klik ulang jika sudah di route yang sama
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}


data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)


@RequiresApi(Build.VERSION_CODES.R)
@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen()
}


fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun PaymentSuccessScreen(transactionId: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pembayaran Berhasil!", style = MaterialTheme.typography.titleLarge)
        Text("Transaction ID: $transactionId", style = MaterialTheme.typography.bodyMedium)
    }
}