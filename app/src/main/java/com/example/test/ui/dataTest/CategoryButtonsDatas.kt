package com.example.test.ui.dataTest

import androidx.annotation.DrawableRes
import com.example.test.R


data class CategoryButton(
    val title: String,
    @DrawableRes val icon: Int
)

object CategoryButtons {
    val categoryButton = listOf(
        CategoryButton("Olahraga", R.drawable.baseline_wallet_24),
        CategoryButton("Teknologi", R.drawable.baseline_history_24),
        CategoryButton("Musik", R.drawable.baseline_notifications_24),
        CategoryButton("Film", R.drawable.baseline_arrow_forward_ios_24),
        CategoryButton("Film", R.drawable.baseline_arrow_forward_ios_24),
        CategoryButton("Film", R.drawable.baseline_arrow_forward_ios_24),
        CategoryButton("Film", R.drawable.baseline_arrow_forward_ios_24)
    )
}