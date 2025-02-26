package com.example.test.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TypingIndicator() {
    Row(modifier = Modifier.padding(8.dp)) {
        Text("Sedang mengetik...", fontSize = 12.sp, color = Color.Gray)
    }
}

