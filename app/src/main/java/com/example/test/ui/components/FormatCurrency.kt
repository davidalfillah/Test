package com.example.test.ui.components

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(value: Int): String {
    return when {
        value >= 1_000_000_000 -> String.format("%.1f Miliar", value / 1_000_000_000f).replace(".0", "")
        value >= 1_000_000 -> String.format("%.1f Juta", value / 1_000_000f).replace(".0", "")
        value >= 1_000 -> String.format("%.1f Ribu", value / 1_000f).replace(".0", "")
        else -> value.toString()
    }
}

fun formatCurrency2(value: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(value).replace(",00", "") // Menghapus ,00 jika ada
}