package com.example.test.ui.components

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
    options: List<String>,
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSheet = true }
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedValue.ifEmpty { "Pilih $label" },
                    color = if (selectedValue.isEmpty()) Color.Gray else Color.Black
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false }
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
    onVillageSelected: (String) -> Unit
) {
    Column {
        CustomDropdown(
            label = "Provinsi",
            options = provinces,
            selectedValue = selectedProvince,
            onSelected = { province ->
                onProvinceSelected(province)
                onCitySelected("") // Reset pilihan di bawahnya
                onSubDistrictSelected("")
                onVillageSelected("")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedProvince.isNotEmpty()) {
            CustomDropdown(
                label = "Kota/Kabupaten",
                options = cities[selectedProvince] ?: emptyList(),
                selectedValue = selectedCity,
                onSelected = { city ->
                    onCitySelected(city)
                    onSubDistrictSelected("")
                    onVillageSelected("")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedCity.isNotEmpty()) {
            CustomDropdown(
                label = "Kecamatan",
                options = subDistricts[selectedCity] ?: emptyList(),
                selectedValue = selectedSubDistrict,
                onSelected = { subDistrict ->
                    onSubDistrictSelected(subDistrict)
                    onVillageSelected("")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedSubDistrict.isNotEmpty()) {
            CustomDropdown(
                label = "Kelurahan/Desa",
                options = villages[selectedSubDistrict] ?: emptyList(),
                selectedValue = selectedVillage,
                onSelected = { onVillageSelected(it) }
            )
        }
    }
}





