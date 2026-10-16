package com.avishek.financetracker.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avishek.financetracker.data.Category
import com.avishek.financetracker.data.TransactionType
import com.avishek.financetracker.ui.common.CategoryBadge
import com.avishek.financetracker.ui.common.CategoryColors
import com.avishek.financetracker.ui.common.CategoryIcons
import com.avishek.financetracker.ui.common.iconFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var selectedType by rememberSaveable { mutableStateOf(TransactionType.EXPENSE) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var toRemove by remember { mutableStateOf<Category?>(null) }
    val visible = categories.filter { it.type == selectedType }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New category") },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = selectedType.ordinal) {
                TransactionType.entries.forEach { type ->
                    Tab(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        text = { Text(if (type == TransactionType.EXPENSE) "Expense" else "Income") },
                    )
                }
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(visible, key = { it.id }) { category ->
                    ListItem(
                        headlineContent = { Text(category.name) },
                        leadingContent = { CategoryBadge(category.iconKey, category.color) },
                        trailingContent = {
                            // Always keep at least one category per type so adding stays possible.
                            if (visible.size > 1) {
                                IconButton(onClick = { toRemove = category }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove ${category.name}")
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddCategoryDialog(
            type = selectedType,
            onDismiss = { showAdd = false },
            onAdd = { name, icon, color ->
                viewModel.addCategory(name, icon, color, selectedType)
                showAdd = false
            },
        )
    }

    toRemove?.let { category ->
        AlertDialog(
            onDismissRequest = { toRemove = null },
            title = { Text("Remove \"${category.name}\"?") },
            text = { Text("It will no longer be offered when adding transactions. Past transactions keep it.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeCategory(category.id)
                    toRemove = null
                }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { toRemove = null }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCategoryDialog(
    type: TransactionType,
    onDismiss: () -> Unit,
    onAdd: (name: String, iconKey: String, color: Long) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var iconKey by rememberSaveable { mutableStateOf("other") }
    var color by rememberSaveable { mutableStateOf(CategoryColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == TransactionType.EXPENSE) "New expense category" else "New income category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NamePreviewRow(name = name, iconKey = iconKey, color = color, onNameChange = { name = it.take(24) })
                Text("Icon", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CategoryIcons.keys.forEach { key ->
                        val selected = key == iconKey
                        IconButton(onClick = { iconKey = key }) {
                            Icon(
                                iconFor(key),
                                contentDescription = key,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Text("Colour", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryColors.forEach { c ->
                        Surface(
                            onClick = { color = c },
                            shape = CircleShape,
                            color = Color(c),
                            border = if (c == color) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                            modifier = Modifier.size(32.dp),
                        ) {}
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name, iconKey, color) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun NamePreviewRow(name: String, iconKey: String, color: Long, onNameChange: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        CategoryBadge(iconKey, color)
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Name") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
    }
}
