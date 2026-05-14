package dev.cipher.notes.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(private val dao: NoteDao) {

    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    fun searchNotes(query: String): Flow<List<Note>> = dao.searchNotes(query)
    fun getNotesByType(type: NoteType): Flow<List<Note>> = dao.getNotesByType(type)
    fun getEncryptedNotes(): Flow<List<Note>> = dao.getEncryptedNotes()

    suspend fun getNoteById(id: String): Note? = dao.getNoteById(id)

    suspend fun createNote(type: NoteType): Note {
        val note = Note(
            id = UUID.randomUUID().toString(),
            type = type,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        dao.insertNote(note)
        return note
    }

    suspend fun saveNote(note: Note) {
        dao.insertNote(note.copy(modifiedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(id: String) = dao.deleteNoteById(id)
}
