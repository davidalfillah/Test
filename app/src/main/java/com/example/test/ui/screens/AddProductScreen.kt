package com.example.test.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import coil3.compose.AsyncImage
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.example.test.ui.components.CustomDropdown
import com.example.test.ui.components.CustomInputField
import com.example.test.ui.components.InputType
import com.example.test.ui.dataType.ProductCategory
import com.example.test.ui.dataType.ProductLocation
import com.example.test.ui.dataType.ProductVariant
import com.example.test.ui.dataType.Product
import com.example.test.ui.dataType.Subcategory
import com.example.test.ui.viewModels.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Save Button
// Change I'm trying to make
// I can see that `addProduct` is indeed a suspend function. I'll modify the Button's onClick handler to use a coroutine scope:
// I will wrap the viewModel.addProduct call in a coroutine scope.


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: ProductViewModel
) {
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var categories by remember { mutableStateOf<List<ProductCategory>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }
    var selectedSubcategory by remember { mutableStateOf<Subcategory?>(null) }


    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }


    var variants by remember { mutableStateOf<List<ProductVariant>>(emptyList()) }
    var showVariantDialog by remember { mutableStateOf(false) }

    var errorProductName by remember { mutableStateOf("") }
    var errorDescription by remember { mutableStateOf("") }
    var errorSelectedCategory by remember { mutableStateOf("") }
    var errorPrice by remember { mutableStateOf("") }
    var errorStock by remember { mutableStateOf("") }
    var errorAddress by remember { mutableStateOf("") }
    var errorCity by remember { mutableStateOf("") }
    var errorProvince by remember { mutableStateOf("") }

    // Loading kategori saat komponen dimuat
    LaunchedEffect(Unit) {
        viewModel.fetchCategories { fetchedCategories ->
            categories = fetchedCategories
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newImages = uris.filterNot { newUri ->
            selectedImages.any { existingUri ->
                existingUri.toString() == newUri.toString()
            }
        }
        selectedImages = selectedImages + newImages
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> 
        selectedVideos = uris 
    }

    val context = LocalContext.current
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Produk") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(selectedImages) { uri ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f) // Rasio KTP 4:3
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(8.dp)
                            )
                    ) {

                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImages.filter { it != uri }.also { selectedImages = it } },
                            modifier = Modifier
                                .size(24.dp)  // Mengubah ukuran tombol menjadi lebih kecil
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus Foto",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)  // Mengubah ukuran icon menjadi lebih kecil
                            )
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(color = MaterialTheme.colorScheme.surface)
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = { imagePickerLauncher.launch("image/*") }),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Tambah Gambar",
                            modifier = Modifier.align(Alignment.Center)
                        )
                        }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)

            ) {
                CustomInputField(
                    type = InputType.TEXT,
                    label = "Nama Produk",
                    placeholder = "Masukkan nama produk",
                    selectedOption = productName,
                    onValueChange = {
                        productName = it
                        errorProductName = if (it.isEmpty()) "Nama Produk tidak boleh kosong!" else ""
                    },
                    errorMessage = errorProductName,
                    required = true
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Deskripsi",
                    placeholder = "Masukkan deskripsi produk",
                    selectedOption = description,
                    onValueChange = {
                        description = it
                        errorDescription = if (it.isEmpty()) "Deskripsi tidak boleh kosong!" else ""
                    },
                    errorMessage = errorDescription,
                    required = true
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Harga",
                    placeholder = "Masukkan harga produk",
                    selectedOption = price,
                    onValueChange = {
                        price = it
                        errorPrice = if (it.isEmpty() || it.toInt() <= 0) "Harga harus lebih dari 0!" else ""
                    },
                    errorMessage = errorPrice,
                    required = true
                )

                CustomInputField(
                    type = InputType.TEXT,
                    label = "Stok",
                    placeholder = "Masukkan stok produk",
                    selectedOption = stock,
                    onValueChange = {
                        stock = it
                        errorStock = if (it.isEmpty() || it.toInt() <= 0) "Stok harus lebih dari 0!" else ""
                    },
                    errorMessage = errorStock,
                    required = true
                )

                CustomDropdown(
                    label = "Kategori",
                    required = true,
                    options = categories.map { it.name },
                    selectedValue = selectedCategory?.name ?: "",
                    errorMessage = errorSelectedCategory,
                    onSelected = { selectedName ->
                        selectedCategory = categories.find { it.name == selectedName }
                        errorSelectedCategory = if (selectedName.isEmpty()) "Kategori tidak boleh kosong!" else ""
                    }
                )

                CustomDropdown(
                    label = "Sub Kategori",
                    disabled = selectedCategory == null,
                    required = true,
                    options = selectedCategory?.subcategories?.map { it.name } ?: emptyList(),
                    selectedValue = selectedSubcategory?.name ?: "",
                    errorMessage = errorSelectedCategory,
                    onSelected = { selectedName ->
                        selectedSubcategory = selectedCategory?.subcategories?.find { it.name == selectedName }
                        errorSelectedCategory = if (selectedName.isEmpty()) "Sub Kategori tidak boleh kosong!" else ""
                    }
                )
            }


            Button(
               onClick = {
                   isSubmitting = true
                   errorProductName = if (productName.isEmpty() || productName.length < 3) "Nama produk harus lebih dari 2 karakter!" else ""
                   errorDescription = if (description.isEmpty() || description.length < 10) "Deskripsi produk harus lebih dari 9 karakter!" else ""
                   errorPrice = if (price.isEmpty() || price.toDoubleOrNull() == null || price.toDouble() <= 0) "Harga produk harus lebih dari 0!" else ""
                   errorStock = if (stock.isEmpty() || stock.toInt() <= 0) "Stok harus lebih dari 0!" else ""
                   errorSelectedCategory = if (selectedCategory == null) "Kategori tidak boleh kosong!" else if (selectedSubcategory == null) "Sub Kategori tidak boleh kosong!" else ""

                   if (errorProductName.isNotEmpty() || errorDescription.isNotEmpty() || errorPrice.isNotEmpty() || errorStock.isNotEmpty() || errorSelectedCategory.isNotEmpty()) {
                           isSubmitting = false
                           return@Button
                   }

                   val product = Product(
                       name = productName,
                       description = description,
                       categoryId = selectedCategory?.id ?: "",
                       subcategoryId = selectedSubcategory?.id ?: "",
                       price = price.toDoubleOrNull() ?: 0.0,
                       stock = stock.toIntOrNull() ?: 0,
                       sellerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                       location = ProductLocation(
                           address = address,
                           city = city,
                           province = province
                       ),
                   )
                   viewModel.viewModelScope.launch {
                       viewModel.addProduct(product, selectedImages)
                           .onSuccess {
                               Toast.makeText(context, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                               navController.popBackStack()
                           }
                           .onFailure { exception ->
                               Toast.makeText(context, exception.message ?: "Gagal menambahkan produk", Toast.LENGTH_SHORT).show()
                           }
                       isSubmitting = false
                   }

               },
               modifier = Modifier
                   .fillMaxWidth()
                   .padding(16.dp),
               enabled = !isSubmitting
           ) {
               Text(if (isSubmitting) "Menyimpan..." else "Simpan Produk")
           }
        }
    }
}