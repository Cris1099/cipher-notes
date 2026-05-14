package dev.cipher.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NoteType { TEXT, TODO }

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String,
    val type: NoteType = NoteType.TEXT,
    val title: String = "",
    val content: String = "",          // plaintext content (TEXT notes)
    val itemsJson: String = "[]",      // JSON array of TodoItem (TODO notes)
    val encrypted: Boolean = false,
    val ciphertext: String? = null,    // AES-256-GCM encrypted blob (Base64)
    val tags: String = "",             // comma-separated
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)

data class TodoItem(
    val id: String,
    val text: String,
    val done: Boolean
)
