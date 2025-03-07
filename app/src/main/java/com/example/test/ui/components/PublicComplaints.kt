package com.example.test.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.test.R

@Composable
fun PublicComplaints() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(color = MaterialTheme.colorScheme.surfaceContainerLowest)
    ){
        Box(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
            .fillMaxWidth()
            .clickable(onClick = {})
            .clip(RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colorScheme.primary),
            contentAlignment = androidx.compose.ui.Alignment.Center
            ){
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(shape = RoundedCornerShape(12.dp))
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(
                                    alpha = 0.3f
                                )
                            ),

                        contentAlignment = Alignment.Center // Memastikan ikon di tengah
                    ) {
                        Image(
                            painter = painterResource(R.drawable._dicons_megaphone_dynamic_color),
                            contentDescription = "Public Complaints",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(4.dp))

                    Column {
                        Text(
                            text = "Sampaikan pengaduan, dan saran anda.",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "Layanan Pengaduan Masyarakat!",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                }
                Image(
                    painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                    contentDescription = "More",
                    modifier = Modifier
                        .size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}