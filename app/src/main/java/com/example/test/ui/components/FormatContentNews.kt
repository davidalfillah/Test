package com.example.test.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun formatNewsContent(newsText: String): AnnotatedString {
    return buildAnnotatedString {
        var tempText = newsText

        // Regex untuk mencari teks yang di-bold menggunakan [b]...[/b]
        val boldRegex = "\\[b](.*?)\\[/b]".toRegex()
        var lastIndex = 0

        for (match in boldRegex.findAll(newsText)) {
            val beforeText = newsText.substring(lastIndex, match.range.first)
            append(beforeText) // Tambahkan teks sebelum bold

            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1]) // Tambahkan teks yang di-bold
            }

            lastIndex = match.range.last + 1
        }

        append(newsText.substring(lastIndex)) // Tambahkan sisa teks
    }
}