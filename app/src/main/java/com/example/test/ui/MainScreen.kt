package com.example.test.ui


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.test.ui.screens.AccountScreen
import com.example.test.ui.screens.ChatDetailScreen
import com.example.test.ui.screens.ChatsScreen
import com.example.test.ui.screens.DigitalCardScreen
import com.example.test.ui.screens.DonationDetailScreen
import com.example.test.ui.screens.DonationInputScreen
import com.example.test.ui.screens.DonationScreen
import com.example.test.ui.screens.HomeKtaScreen
import com.example.test.ui.screens.HomeScreen
import com.example.test.ui.screens.LoginScreen
import com.example.test.ui.screens.MemberProfileScreen
import com.example.test.ui.screens.NewsDetailScreen
import com.example.test.ui.screens.NewsScreen
import com.example.test.ui.screens.OtpScreen
import com.example.test.ui.screens.PaymentScreen
import com.example.test.ui.screens.ProfileSetupScreen
import com.example.test.ui.screens.RegistrationScreen
import com.example.test.ui.screens.RegistrationUmkmScreen
import com.example.test.ui.screens.ShoppingScreen
import com.example.test.ui.screens.StatusScreen
import com.example.test.ui.screens.SuccessScreen
import com.example.test.ui.screens.UploadKtpScreen
import com.example.test.ui.viewModels.ChatViewModel

//Email : hellogrib430@gmail.com
//Pass : Hell0@#$

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MainScreen(authViewModel: AuthViewModel = AuthViewModel(AuthRepository())) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"

    setStatusBarColor(
        color = when (currentRoute) {
            "home" -> MaterialTheme.colorScheme.primary
            "shopping" -> MaterialTheme.colorScheme.background
            "chat" -> MaterialTheme.colorScheme.primary
            "news_detail/{newsId}" -> MaterialTheme.colorScheme.primary
            "account" -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.primary
        },
        useDarkIcons = when (currentRoute) { // Gunakan warna ikon status bar yang sesuai
            "home" -> false
            "shopping" -> true
            "login" -> true
            "chat" -> false
            "digitalCard/{member}" -> true
            "biodataMember/{memberJson}" -> true
            "news_detail/{newsId}" -> false
            "news" -> false
            "homeKta/{userId}"-> true
            "account" -> false
            "profile_setup" -> true
            else -> false
        }
    )

    Scaffold(
        bottomBar = {
            if (currentRoute !in listOf(
                    "login",
                    "register",
                    "donations",
                    "news",
                    "status",
                    "success?nextScreen={nextScreen}",
                    "registerGrib",
                    "registerUmkm",
                    "profile_setup",
                    "donation_input/{title}",
                    "digitalCard/{member}",
                    "homeKta/{userId}",
                    "biodataMember/{memberJson}",
                    "uploadKtp",
                    "registerGrib?nik={nik}&name={name}&address={address}&imageUrl={imageUrl}&birthDate={birthDate}",
                    "chat_detail/{chatId}",
                    "kta/{userId}",
                    "donation_detail/{donationId}",
                    "news_detail/{newsId}",
                    "otp_screen/{phoneNumber}"))
            {
                BottomNavigationBar(navController)
            }
        }
    ) { PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",

        ) {
            composable(
                route = "payment/{userId}/{userEmail}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("userEmail") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val userEmail = backStackEntry.arguments?.getString("userEmail") ?: ""
                PaymentScreen(
                    userId = userId,
                    userEmail = userEmail,
                    navController = navController
                )
            }
            composable("donation_input/{title}") { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: "general"
                DonationInputScreen(
                    title = title,
                    navController = navController
                )
            }
            composable("donation_detail/{donationId}") { backStackEntry ->
                val donationId = backStackEntry.arguments?.getString("donationId") ?: return@composable
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
            composable("news_detail/{newsId}") { backStackEntry ->
                val newsId = backStackEntry.arguments?.getString("newsId") ?: "1"
                NewsDetailScreen(newsId, navController)
            }
            composable("home") { HomeScreen(navController,
                PaddingValues, authViewModel) }
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
            composable("registerUmkm") { RegistrationUmkmScreen(paddingValues = PaddingValues, navController, authViewModel = authViewModel) }
            composable("profile_setup") { ProfileSetupScreen(navController, authViewModel, paddingValues = PaddingValues) }
            composable(
                "success?nextScreen={nextScreen}",
                arguments = listOf(navArgument("nextScreen") { defaultValue = "home" })
            ) { backStackEntry ->
                val nextScreen = backStackEntry.arguments?.getString("nextScreen") ?: "home"
                SuccessScreen(navController, nextScreen)
            }
            composable("news") { NewsScreen(navController, paddingValues = PaddingValues) }
            composable("shopping") { ShoppingScreen(navController, paddingValues = PaddingValues) }
            composable("chat") { ChatsScreen(navController, paddingValues = PaddingValues, chatViewModel = ChatViewModel(), authViewModel = authViewModel) }
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
            composable("login") { LoginScreen(navController, authViewModel, paddingValues = PaddingValues) }
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

    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = isSelected,
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