package com.example.test.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.test.R
import com.example.test.ui.dataType.Branch
import com.example.test.ui.dataType.Member
import com.example.test.ui.viewModels.MemberViewModel
import com.google.common.io.Files.append

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
   var showActionDialog by remember { mutableStateOf(false) }
   var showShareOptions by remember { mutableStateOf(false) }
   var isFront by remember { mutableStateOf(true) }
   val context = LocalContext.current

   LaunchedEffect(userId) {
       memberViewModel.fetchMember(userId) { fetchedMember, fetchedBranch ->
           member = fetchedMember
           branch = fetchedBranch
           isLoading = false
       }
   }

   Scaffold(
       topBar = {
           TopAppBar(
               title = { Text("Kartu Digital") },
               colors = TopAppBarDefaults.topAppBarColors(
                   containerColor = MaterialTheme.colorScheme.background,
                   titleContentColor = MaterialTheme.colorScheme.onBackground,
                   navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                   actionIconContentColor = MaterialTheme.colorScheme.onBackground
               ),
               navigationIcon = {
                   IconButton(onClick = { navController.popBackStack() }) {
                       Icon(
                           imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                           contentDescription = "Back"
                       )
                   }
               },
               actions = {
                   IconButton(onClick = { showShareOptions = true }) {
                       Icon(
                           imageVector = Icons.Default.Share,
                           contentDescription = "Share"
                       )
                   }
                   IconButton(onClick = { showActionDialog = true }) {
                       Icon(
                           imageVector = Icons.Default.Share,
                           contentDescription = "Download"
                       )
                   }
               }
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
               horizontalAlignment = Alignment.CenterHorizontally,
               modifier = Modifier.padding(16.dp)
           ) {
               AnimatedVisibility(
                   visible = !isLoading && member != null,
                   enter = fadeIn(),
                   exit = fadeOut()
               ) {
                   Row(
                       modifier = Modifier
                           .fillMaxWidth()
                           .padding(bottom = 16.dp),
                       horizontalArrangement = Arrangement.Center,
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Text(
                           text = "Tap atau klik kartu untuk membalik",
                           style = MaterialTheme.typography.bodyMedium,
                           color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                       )
                   }
               }

               if (isLoading) {
                   CircularProgressIndicator(
                       modifier = Modifier.size(48.dp),
                       color = MaterialTheme.colorScheme.primary
                   )
               } else if (member != null) {
                   val rotation = animateFloatAsState(
                       targetValue = if (isFront) 0f else 180f,
                       animationSpec = tween(
                           durationMillis = 400,
                           easing = FastOutSlowInEasing
                       )
                   )

                   val density = LocalDensity.current

                   Card(
                       modifier = Modifier
                           .width(300.dp)
                           .height(480.dp)
                           .graphicsLayer {
                               rotationY = rotation.value
                               cameraDistance = 8f * density.density
                           }
                           .clickable { isFront = !isFront },
                       elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                       shape = RoundedCornerShape(16.dp)
                   ) {
                       Box(modifier = Modifier.fillMaxSize()) {
                           if (rotation.value <= 90f) {
                               FrontCard(member = member!!)
                           } else {
                               BackCard(member = member!!)
                           }
                       }
                   }
               }
           }
       }

       if (showActionDialog) {
           AlertDialog(
               onDismissRequest = { showActionDialog = false },
               title = { Text("Pilih Format") },
               text = { Text("Pilih format kartu yang ingin Anda unduh") },
               confirmButton = {
                   Button(onClick = {
                       // TODO: Implement PDF download
                       showActionDialog = false
                   }) {
                       Icon(
                           imageVector = Icons.Default.Lock,
                           contentDescription = null,
                           modifier = Modifier.size(18.dp)
                       )
                       Spacer(modifier = Modifier.width(8.dp))
                       Text("Unduh PDF")
                   }
               },
               dismissButton = {
                   OutlinedButton(onClick = {
                       // TODO: Implement Image download
                       showActionDialog = false
                   }) {
                       Icon(
                           imageVector = Icons.Default.Lock,
                           contentDescription = null,
                           modifier = Modifier.size(18.dp)
                       )
                       Spacer(modifier = Modifier.width(8.dp))
                       Text("Unduh Gambar")
                   }
               }
           )
       }

       if (showShareOptions) {
           AlertDialog(
               onDismissRequest = { showShareOptions = false },
               title = { Text("Bagikan Kartu") },
               text = { Text("Pilih cara berbagi kartu Anda") },
               confirmButton = {
                   Button(onClick = {
                       // TODO: Implement PDF sharing
                       showShareOptions = false
                   }) {
                       Icon(
                           imageVector = Icons.Default.Share,
                           contentDescription = null,
                           modifier = Modifier.size(18.dp)
                       )
                       Spacer(modifier = Modifier.width(8.dp))
                       Text("Bagikan PDF")
                   }
               },
               dismissButton = {
                   OutlinedButton(onClick = {
                       // TODO: Implement Image sharing
                       showShareOptions = false
                   }) {
                       Icon(
                           imageVector = Icons.Default.Share,
                           contentDescription = null,
                           modifier = Modifier.size(18.dp)
                       )
                       Spacer(modifier = Modifier.width(8.dp))
                       Text("Bagikan Gambar")
                   }
               }
           )
       }
   }
}

// Keep existing FrontCard and BackCard components

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
            append(value.substring(currentIndex, match.range.first))

            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }

            currentIndex = match.range.last + 1
        }

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