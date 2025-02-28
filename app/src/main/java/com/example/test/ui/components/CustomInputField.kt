package com.example.test.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInputField(
    type: InputType,
    label: String,
    placeholder: String = "",
    options: List<String> = emptyList(),
    selectedOption: String = "",
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))

        when (type) {
            InputType.TEXT -> {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = { onValueChange(it) },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }

            InputType.CHECKBOX -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onValueChange((selectedOption.toBoolean().not()).toString()) }
                        .padding(8.dp)
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
                            RadioButton(selected = selectedOption == option, onClick = { onValueChange(option) })
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
                            disabledBorderColor = Color.Gray,
                            disabledTrailingIconColor = Color.Gray
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
    }
}

enum class InputType { TEXT, CHECKBOX, RADIO, SELECT, SWITCH }


