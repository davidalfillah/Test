package com.example.test.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.test.AdminScreen
import com.example.test.AuthViewModel
import com.example.test.DashboardScreen
import com.example.test.R
import com.example.test.setStatusBarColor
import com.example.test.ui.screens.AccountScreen
import com.example.test.ui.screens.ChatsScreen
import com.example.test.ui.screens.HomeScreen
import com.example.test.ui.screens.LoginScreen
import com.example.test.ui.screens.NewsDetailScreen
import com.example.test.ui.screens.NewsScreen
import com.example.test.ui.screens.OtpScreen
import com.example.test.ui.screens.ProfileSetupScreen
import com.example.test.ui.screens.ShoppingScreen
import com.example.test.ui.screens.StatusScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(authViewModel: AuthViewModel = AuthViewModel()) {
    val systemUiController = rememberSystemUiController()
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "home"
    val showBackButton = currentRoute in listOf("profile", "login", "register", "settings", "details", "news_detail/{newsId}", "news", "status") // Route yang butuh back button
    val titleMap = mapOf(
        "news" to "News",
        "news_detail/{newsId}" to "Detail Berita",
        "shop" to "Toko",
        "status" to ""
    )

    setStatusBarColor(
        color = when (currentRoute) {
            "home" -> MaterialTheme.colorScheme.primary
            "shopping" -> Color.Green
            "profile_setup" -> Color.Yellow
            else -> Color.White
        },
        useDarkIcons = when (currentRoute) { // Gunakan warna ikon status bar yang sesuai
            "home" -> false
            "shopping" -> true
            "chat" -> false
            "news_detail/{newsId}" -> false
            "news" -> false
            "account" -> false
            "profile_setup" -> true
            else -> true
        }
    )



    val titleHeader = titleMap[currentRoute] ?: "Detail"

    Scaffold(
        bottomBar = {
            if (currentRoute !in listOf("login", "register", "news", "status", "news_detail/{newsId}")) {
                BottomNavigationBar(navController)
            }
        }
    ) { PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("news_detail/{newsId}") { backStackEntry ->
                val newsId = backStackEntry.arguments?.getString("newsId") ?: "1"
                NewsDetailScreen(newsId, navController)
            }
            composable("home") { HomeScreen(navController,
                PaddingValues, authViewModel) }
            composable("status") { StatusScreen(navController) }
            composable("profile_setup") { ProfileSetupScreen(navController, authViewModel) }
            composable("news") { NewsScreen(navController, paddingValues = PaddingValues) }
            composable("shopping") { ShoppingScreen(navController, paddingValues = PaddingValues) }
            composable("chat") { ChatsScreen(navController, paddingValues = PaddingValues) }
            composable("otp_screen") { OtpScreen(navController, authViewModel) }
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("dashboard") { DashboardScreen(navController, authViewModel) }
            composable("admin") { AdminScreen(navController, authViewModel) }
            composable("account") { AccountScreen(authViewModel,
                 PaddingValues,
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") },
                onProfileClick = { navController.navigate("profile") },
            ) }
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

    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = navController.currentDestination?.route == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                    }
                },

            )
        }
    }
}

data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)

@Composable
fun EmptyScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(title)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen()
}