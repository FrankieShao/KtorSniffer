package io.github.frankieshao.ktorsniffer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Centralized style definitions for the KtorSniffer UI.
 * Contains text styles for various UI elements.
 */
object Style {
    object Text {
        val BodyNormal = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.5.sp,
            color = Color.Black
        )
        val BodyHead = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.6.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp,
            color = Color.Black
        )
        val BodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp,
            color = Color.Black
        )
        val Headline = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.5.sp,
            color = Color.Black
        )
    }
}
