package com.example.test.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.dataType.Branch
import com.example.test.ui.dataType.Member
import com.example.test.ui.viewModels.MemberViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalCardScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    userId: String,
    memberViewModel: MemberViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    var member by remember { mutableStateOf<Member?>(null) }
    var branch by remember { mutableStateOf<Branch?>(null) }

    LaunchedEffect(userId) {
        memberViewModel.fetchMember(userId) { fetchedMember, fetchedBranch ->
            member = fetchedMember
            branch = fetchedBranch
            isLoading = false
        }
    }

    // State untuk kontrol rotasi kartu
    var isFront by remember { mutableStateOf(true) }
    val rotation = animateFloatAsState(
        targetValue = if (isFront) 0f else 180f,
        animationSpec = tween(durationMillis = 600)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kartu Digital") },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Teks di atas kartu
                if(member != null) {
                    Text(
                        text = "Tap Kartu untuk melihat sebaliknya",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (member != null) {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .height(480.dp)
                            .graphicsLayer {
                                rotationY = rotation.value
                                cameraDistance = 12f * density
                            }
                            .clickable { isFront = !isFront },
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (rotation.value <= 90f) {
                                // Depan Kartu
                                FrontCard(member = member!!)
                            } else {
                                // Belakang Kartu
                                BackCard(member = member!!)
                            }
                        }
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }

    }
}

@Composable
fun FrontCard(member: Member) {
    var branch by remember { mutableStateOf<Branch?>(null) }
    Log.d("FotoURL", "URL Foto: ${member.fotoUrl}")
    val decodedUrl = Uri.decode(member.fotoUrl)

    Log.d("FotoURLDecode", "URL Foto: ${decodedUrl}")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {
        // Background dekoratif atas (putih)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.White)
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomEnd = 40.dp,
                        bottomStart = 0.dp
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp)
                .padding(horizontal = 16.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header dengan Logo dan Tulisan
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_grib),
                    contentDescription = "Logo Organisasi",
                    modifier = Modifier
                        .size(70.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "KARTU ANGGOTA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "GRIB JAYA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                // Foto dan Informasi dengan background primary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 4.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            Color.Blue
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            AsyncImage(
                                model = member.fotoUrl,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )

                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Nama dan ID
                        Text(
                            text = member.fullName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ID: ${member.memberId}",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,

                            )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Jabatan dengan background biru
                Text(
                    text = member.jobTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Blue, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }
        }

        // Footer dengan background putih (diposisikan di bawah)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White, shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "Berlaku selama menjadi anggota GRIB JAYA",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun CardInfoItem(label: String, value: String) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val boldPattern = Regex("""\*\*(.*?)\*\*""")
        val matches = boldPattern.findAll(value)

        matches.forEach { match ->
            // Teks sebelum teks bold
            append(value.substring(currentIndex, match.range.first))

            // Teks bold
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }

            // Update indeks
            currentIndex = match.range.last + 1
        }

        // Teks setelah teks bold (jika ada)
        if (currentIndex < value.length) {
            append(value.substring(currentIndex))
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = annotatedString,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun BackCard(member: Member) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.White
            )
            .graphicsLayer {
                rotationY = 180f
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo_grib),
                contentDescription = "Logo Organisasi",
                modifier = Modifier
                    .size(80.dp)
            )
            Text(
                text = "KARTU TANDA ANGGOTA \n" + "GRIB JAYA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            CardInfoItem("1.", "Pemegang Kartu ini adalah **Anggota terdaftar** e-KTA Grib Jaya")
            CardInfoItem("2.", "Kartu ini adalah **Identitas Resmi** dari Anggota Grib Jaya")
            CardInfoItem("3.", "**Dilarang** menggunakan kartu ini dalam kegiatan yang melanggar hukum")
            CardInfoItem("4.", "Jika menemukan kartu ini harap dikembalikan ke:")
            CardInfoItem("", "Sekertariat DPP Grib Jaya")
            CardInfoItem("", "Jl. ")

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column{
                    Text(text = "Scan disini", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.Blue)
                    ) {
                        Text(
                            text = "QR Code",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(text = "DEWAN PEMIMPIN PUSAT", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    Text(text = "KETUA UMUM", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "HERCULES ROSARIO MARSHAL", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color.Black)
                }
            }
        }
    }
}