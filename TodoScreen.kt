package dev.cipher.notes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.cipher.notes.data.TodoItem
import dev.cipher.notes.utils.DateUtils
import dev.cipher.notes.utils.JsonUtils

@Composable
fun TodoScreen(
    noteId: String,
    onBack: () -> Unit,
    vm: NoteEditorViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by vm.uiState.collectAsState()
    val note = uiState.note ?: return

    var showEncryptDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val completedCount = uiState.items.count { it.done }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = { vm.save(); onBack() }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showEncryptDialog = true }) {
                    Icon(Icons.Rounded.Lock, contentDescription = "Encrypt")
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TextField(
                value = uiState.title,
                onValueChange = vm::setTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Checklist title…", style = MaterialTheme.typography.headlineSmall) },
                textStyle = MaterialTheme.typography.headlineSmall,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        DateUtils.formatFull(note.modifiedAt),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(6.dp, 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "$completedCount/${uiState.items.size} done",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(6.dp, 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
                if (uiState.encrypted) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "🔒 Encrypted",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(6.dp, 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    TodoItemRow(
                        item = item,
                        onToggle = { vm.updateTodoItem(item.id, done = !item.done) },
                        onTextChange = { vm.updateTodoItem(item.id, text = it) },
                        onDelete = { vm.deleteTodoItem(item.id) }
                    )
                }
                item {
                    Button(
                        onClick = { vm.addTodoItem() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("+ Add item", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showEncryptDialog) {
        EncryptDialog(
            onEncrypt = { password ->
                vm.performEncrypt(password)
                showEncryptDialog = false
                onBack()
            },
            onDismiss = { showEncryptDialog = false },
            vm = vm
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete checklist?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.delete()
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TodoItemRow(
    item: TodoItem,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
            .clickable { onToggle() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = item.done,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(20.dp)
        )

        TextField(
            value = item.text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .height(32.dp),
            placeholder = { Text("Item…", style = MaterialTheme.typography.bodySmall) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
