package dev.cipher.notes.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatRelative(ts: Long): String {
        val diff = System.currentTimeMillis() - ts
        return when {
            diff < 60_000L              -> "just now"
            diff < 3_600_000L           -> "${diff / 60_000}m ago"
            diff < 86_400_000L          -> "Today, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))}"
            diff < 172_800_000L         -> "Yesterday"
            else                        -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(ts))
        }
    }

    fun formatFull(ts: Long): String =
        SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(ts))
}
