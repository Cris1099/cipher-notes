package dev.cipher.notes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cipher.notes.data.Note
import dev.cipher.notes.data.NoteRepository
import dev.cipher.notes.data.NoteType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val sortBy: String = "modified",
    val filterBy: String = "all"
)

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repo: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _searchQuery,
                repo.getAllNotes()
            ) { query, allNotes ->
                applyFiltersAndSort(allNotes, query)
            }.collect { notes ->
                _uiState.value = _uiState.value.copy(notes = notes)
            }
        }
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(filterBy = filter)
    }

    fun setSortBy(sort: String) {
        _uiState.value = _uiState.value.copy(sortBy = sort)
    }

    fun deleteNote(id: String) {
        viewModelScope.launch { repo.deleteNote(id) }
    }

    private fun applyFiltersAndSort(notes: List<Note>, query: String): List<Note> {
        var filtered = notes

        filtered = when (_uiState.value.filterBy) {
            "note"      -> filtered.filter { it.type == NoteType.TEXT }
            "todo"      -> filtered.filter { it.type == NoteType.TODO }
            "encrypted" -> filtered.filter { it.encrypted }
            else        -> filtered
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                (!it.encrypted && it.content.contains(query, ignoreCase = true))
            }
        }

        filtered = when (_uiState.value.sortBy) {
            "created" -> filtered.sortedByDescending { it.createdAt }
            "title"   -> filtered.sortedBy { it.title }
            else      -> filtered.sortedByDescending { it.modifiedAt }
        }

        return filtered
    }
}
