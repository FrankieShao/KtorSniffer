package io.github.frankieshao.ktorsniffer.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * @author Frank
 * @created 5/2/25
 */

fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val time = instant.toLocalDateTime(TimeZone.currentSystemDefault()).time
    return "$date - $time"
}

@Composable
fun colorByHttpCode(code: Int?): Color {
    return when (code) {
        in 200..299 -> Color(0xFF009E60)
        in 400..499 -> Color.Red
        in 500..599 -> Color.Red
        else -> LocalContentColor.current
    }
}