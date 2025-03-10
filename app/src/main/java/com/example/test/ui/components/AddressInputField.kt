package com.example.test.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.ui.dataTest.cities
import com.example.test.ui.dataTest.provinces
import com.example.test.ui.dataTest.subDistricts
import com.example.test.ui.dataTest.villages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdown(
    label: String,
    description: String? = null,
    required: Boolean = false,
    disabled: Boolean = false,
    showLabel: Boolean = true,
    errorMessage: String? = "",
    options: List<String>,
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        if (showLabel) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                if (required) {
                    Text(
                        text = " *",
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (description != null) {
            Text(
                text = description,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!disabled) Modifier.clickable { showSheet = true }
                    else Modifier
                )
                .border(
                    width = 1.dp,
                    color = if (errorMessage.isNullOrEmpty()) {
                        if (disabled) Color.LightGray else Color.Gray
                    } else {
                        Color.Red
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    color = if (disabled) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent
                )

                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedValue.ifEmpty { "Pilih $label" },
                    color = when {
                        disabled -> Color.Gray
                        selectedValue.isEmpty() -> Color.LightGray
                        else -> Color.Black
                    },
                    fontWeight = FontWeight.Normal
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = if (errorMessage.isNullOrEmpty()) {
                        if (disabled) Color.Gray else Color.Black
                    } else {
                        Color.Red
                    },
                )
            }
        }
        // Tampilkan pesan error jika ada
        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Cari $label...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(min = 100.dp, max = 400.dp)
                ) {
                    val filteredOptions = options.filter { it.contains(searchText, ignoreCase = true) }
                    items(filteredOptions) { option ->
                        Text(
                            text = option,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    onSelected(option)
                                    showSheet = false
                                }
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun AddressInputField(
    selectedProvince: String,
    selectedCity: String,
    selectedSubDistrict: String,
    selectedVillage: String,
    onProvinceSelected: (String) -> Unit,
    onCitySelected: (String) -> Unit,
    onSubDistrictSelected: (String) -> Unit,
    onVillageSelected: (String) -> Unit,
    errorMessageProvince: String? = null,
    errorMessageCity: String? = null,
    errorMessageSubDistrict: String? = null,
    errorMessageVillage: String? = null
) {
    Column {
        CustomDropdown(
            label = "Provinsi",
            options = provinces,
            required = true,
            selectedValue = selectedProvince,
            errorMessage = errorMessageProvince,
            onSelected = { province ->
                onProvinceSelected(province)
                onCitySelected("") // Reset pilihan di bawahnya
                onSubDistrictSelected("")
                onVillageSelected("")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomDropdown(
            label = "Kota/Kabupaten",
            options = cities[selectedProvince] ?: emptyList(),
            selectedValue = selectedCity,
            required = true,
            errorMessage = errorMessageCity,
            disabled = selectedProvince.isEmpty(),
            onSelected = { city ->
                onCitySelected(city)
                onSubDistrictSelected("")
                onVillageSelected("")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomDropdown(
            label = "Kecamatan",
            options = subDistricts[selectedCity] ?: emptyList(),
            selectedValue = selectedSubDistrict,
            required = true,
            errorMessage = errorMessageSubDistrict,
            disabled = selectedCity.isEmpty(),
            onSelected = { subDistrict ->
                onSubDistrictSelected(subDistrict)
                onVillageSelected("")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomDropdown(
            label = "Kelurahan/Desa",
            options = villages[selectedSubDistrict] ?: emptyList(),
            selectedValue = selectedVillage,
            required = true,
            errorMessage = errorMessageVillage,
            disabled = selectedSubDistrict.isEmpty(),
            onSelected = { onVillageSelected(it) }
        )
    }
}





