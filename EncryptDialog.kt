package dev.cipher.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.cipher.notes.crypto.PasswordStrength

@Composable
fun EncryptDialog(
    onEncrypt: (String) -> Unit,
    onDismiss: () -> Unit,
    vm: NoteEditorViewModel
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var strength by remember { mutableStateOf(PasswordStrength.WEAK) }

    LaunchedEffect(password) {
        strength = vm.encrypt(password)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .verticalScroll(rememberScrollState()),
        title = { Text("Encrypt note", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Set a password to encrypt this note with AES-256-GCM. You'll need it to view the note again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                // Strength indicator
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Strength:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            strength.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (strength) {
                                PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                                PasswordStrength.MODERATE -> MaterialTheme.colorScheme.tertiary
                                PasswordStrength.STRONG -> MaterialTheme.colorScheme.secondary
                                PasswordStrength.VERY_STRONG -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                    LinearProgressIndicator(
                        progress = { strength.fraction },
                        modifier = Modifier.fillMaxWidth(),
                        color = when (strength) {
                            PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                            PasswordStrength.MODERATE -> MaterialTheme.colorScheme.tertiary
                            PasswordStrength.STRONG -> MaterialTheme.colorScheme.secondary
                            PasswordStrength.VERY_STRONG -> MaterialTheme.colorScheme.primary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                    Text(
                        "Passwords don't match",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEncrypt(password) },
                enabled = password.isNotEmpty() && password == confirmPassword && strength != PasswordStrength.WEAK
            ) {
                Text("Encrypt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
