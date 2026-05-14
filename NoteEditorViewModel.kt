package dev.cipher.notes.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cipher.notes.crypto.CryptoManager
import dev.cipher.notes.crypto.PasswordStrength
import dev.cipher.notes.data.Note
import dev.cipher.notes.data.NoteRepository
import dev.cipher.notes.data.NoteType
import dev.cipher.notes.data.TodoItem
import dev.cipher.notes.utils.JsonUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val note: Note? = null,
    val title: String = "",
    val content: String = "",
    val items: List<TodoItem> = emptyList(),
    val encrypted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repo: NoteRepository,
    private val crypto: CryptoManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: String? = savedStateHandle["noteId"]

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            viewModelScope.launch {
                val note = repo.getNoteById(noteId) ?: return@launch
                _uiState.value = EditorUiState(
                    note = note,
                    title = note.title,
                    content = note.content,
                    items = if (note.type == NoteType.TODO) JsonUtils.jsonToTodoItems(note.itemsJson) else emptyList(),
                    encrypted = note.encrypted
                )
            }
        }
    }

    fun setTitle(t: String) { _uiState.value = _uiState.value.copy(title = t) }
    fun setContent(c: String) { _uiState.value = _uiState.value.copy(content = c) }

    fun addTodoItem(text: String = "") {
        val items = _uiState.value.items.toMutableList()
        items.add(JsonUtils.newTodoItem(text))
        _uiState.value = _uiState.value.copy(items = items)
    }

    fun updateTodoItem(id: String, text: String? = null, done: Boolean? = null) {
        val items = _uiState.value.items.toMutableList()
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val it = items[idx]
            items[idx] = it.copy(text = text ?: it.text, done = done ?: it.done)
            _uiState.value = _uiState.value.copy(items = items)
        }
    }

    fun deleteTodoItem(id: String) {
        val items = _uiState.value.items.filter { it.id != id }
        _uiState.value = _uiState.value.copy(items = items)
    }

    fun save() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val note = state.note ?: return@launch
                val updated = note.copy(
                    title = state.title.trim(),
                    content = if (note.type == NoteType.TEXT) state.content else "",
                    itemsJson = if (note.type == NoteType.TODO) JsonUtils.todoItemsToJson(state.items) else "[]",
                    modifiedAt = System.currentTimeMillis()
                )
                repo.saveNote(updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun encrypt(password: String): PasswordStrength {
        return crypto.passwordStrength(password)
    }

    fun performEncrypt(password: String) {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val note = state.note ?: return@launch
                val payload = when (note.type) {
                    NoteType.TEXT -> mapOf("title" to state.title, "content" to state.content)
                    NoteType.TODO -> mapOf("title" to state.title, "items" to state.items)
                }
                val json = org.json.JSONObject(payload).toString()
                val cipher = crypto.encrypt(json, password)
                val encrypted = note.copy(
                    ciphertext = cipher,
                    encrypted = true,
                    content = "",
                    itemsJson = "[]",
                    modifiedAt = System.currentTimeMillis()
                )
                repo.saveNote(encrypted)
                _uiState.value = _uiState.value.copy(encrypted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            val note = _uiState.value.note ?: return@launch
            repo.deleteNote(note.id)
        }
    }
}
