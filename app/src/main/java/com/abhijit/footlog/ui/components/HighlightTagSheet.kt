package com.abhijit.footlog.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhijit.footlog.ui.theme.FootlogColors

private val categories = listOf("Cafe", "Shop", "Viewpoint", "Custom")
private val defaultEmojis = mapOf(
    "Cafe" to "☕", "Shop" to "🛍️", "Viewpoint" to "🏔️", "Custom" to "📍"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightTagSheet(
    onDismiss: () -> Unit,
    onConfirm: (category: String, emoji: String, name: String, note: String?) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val routeColor = if (isDark) FootlogColors.routeLineDark else FootlogColors.routeLineLight
    val onPrimary = if (isDark) FootlogColors.backgroundDark else FootlogColors.backgroundLight
    val textPrimary = if (isDark) FootlogColors.textPrimaryDark else FootlogColors.textPrimaryLight
    val textSecondary = if (isDark) FootlogColors.textSecondaryDark else FootlogColors.textSecondaryLight

    var selectedCategory by remember { mutableStateOf("Cafe") }
    var emoji by remember { mutableStateOf("☕") }
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Tag this spot", style = MaterialTheme.typography.titleMedium, color = textPrimary)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            emoji = defaultEmojis[cat] ?: "📍"
                        },
                        label = { Text(cat) }
                    )
                }
            }

            OutlinedTextField(
                value = emoji,
                onValueChange = { if (it.length <= 2) emoji = it },
                label = { Text("Emoji") },
                modifier = Modifier.width(80.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = routeColor,
                    unfocusedBorderColor = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight,
                    focusedTextColor = textPrimary, unfocusedTextColor = textPrimary
                )
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = routeColor,
                    unfocusedBorderColor = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight,
                    focusedTextColor = textPrimary, unfocusedTextColor = textPrimary
                )
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Short note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = routeColor,
                    unfocusedBorderColor = if (isDark) FootlogColors.borderDark else FootlogColors.borderLight,
                    focusedTextColor = textPrimary, unfocusedTextColor = textPrimary
                )
            )

            Button(
                onClick = { onConfirm(selectedCategory, emoji, name, note.ifBlank { null }) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = routeColor, contentColor = onPrimary)
            ) { Text("Add to route") }
        }
    }
}
