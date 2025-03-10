package com.example.test.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInputField(
    type: InputType,
    label: String,
    description: String? = null,
    placeholder: String = "",
    options: List<String> = emptyList(),
    selectedOption: String = "",
    showLabel: Boolean = true,
    required: Boolean = false,
    errorMessage: String? = null,
    onValueChange: (String) -> Unit = {},
    onDateValueChange: (Timestamp) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {

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
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (type) {
            InputType.TEXT -> {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = { onValueChange(it) },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = !errorMessage.isNullOrEmpty(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = if (!errorMessage.isNullOrEmpty()) Color.Red else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (!errorMessage.isNullOrEmpty()) Color.Red else Color.Gray,
                        errorBorderColor = Color.Red
                    )
                )
            }

            InputType.CHECKBOX -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onValueChange((selectedOption.toBoolean().not()).toString()) }
                ) {
                    Checkbox(
                        checked = selectedOption.toBoolean(),
                        onCheckedChange = { onValueChange(it.toString()) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Setuju", fontSize = 14.sp)
                }
            }

            InputType.RADIO -> {
                Column {
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onValueChange(option) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { onValueChange(option) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option, fontSize = 14.sp)
                        }
                    }
                }
            }

            InputType.SELECT -> {
                Box {
                    OutlinedTextField(
                        value = selectedOption.ifEmpty { "Pilih..." },
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        placeholder = { Text(placeholder) },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = if (errorMessage.isNullOrEmpty()) Color.Red else Color.Gray,
                            disabledTrailingIconColor = if (errorMessage.isNullOrEmpty()) Color.Red else Color.Gray
                        ),
                        enabled = false
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        options.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                onValueChange(option)
                                expanded = false
                            })
                        }
                    }
                }
            }

            InputType.DATE -> {
                var showDatePicker by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = selectedOption.ifEmpty { placeholder },
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    readOnly = true,
                    placeholder = { Text(placeholder) },
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = if (!errorMessage.isNullOrEmpty()) Color.Red else Color.Gray,
                        disabledTrailingIconColor = if (!errorMessage.isNullOrEmpty()) Color.Red else Color.Gray
                    ),
                    enabled = false
                )

                if (showDatePicker) {
                    DatePickerModal(
                        onDateSelected = { timestamp ->
                            if (timestamp != null) {
                                onDateValueChange(timestamp)
                            }
                            showDatePicker = false
                        },
                        onDismiss = { showDatePicker = false }
                    )
                }
            }


            InputType.SWITCH -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(if (selectedOption.toBoolean()) "Aktif" else "Nonaktif", fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = selectedOption.toBoolean(),
                        onCheckedChange = { onValueChange(it.toString()) }
                    )
                }
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
}

enum class InputType { TEXT, CHECKBOX, RADIO, SELECT, SWITCH, DATE }



