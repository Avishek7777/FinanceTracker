package com.avishek.financetracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avishek.financetracker.ui.common.Money
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Currency
import java.util.Locale

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onOpenCategories: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()
    var showCurrencyPicker by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            viewModel.exportCsv(context.contentResolver, uri) { message ->
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        item {
            ListItem(
                headlineContent = { Text("Currency") },
                supportingContent = { Text(currencyLabel(currencyCode)) },
                leadingContent = { Icon(Icons.Filled.Payments, contentDescription = null) },
                modifier = Modifier.clickable { showCurrencyPicker = true },
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Categories") },
                supportingContent = { Text("Add or remove expense and income categories") },
                leadingContent = { Icon(Icons.Filled.Category, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onOpenCategories),
            )
        }
        item { HorizontalDivider() }
        item {
            ListItem(
                headlineContent = { Text("Export to CSV") },
                supportingContent = { Text("Save all transactions as a spreadsheet file") },
                leadingContent = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
                modifier = Modifier.clickable { exportLauncher.launch("finance-${LocalDate.now()}.csv") },
            )
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            onDismiss = { showCurrencyPicker = false },
            onSelect = {
                viewModel.setCurrency(it)
                showCurrencyPicker = false
            },
        )
    }
}

private fun currencyLabel(code: String): String {
    if (code.isEmpty()) return ""
    val name = runCatching { Currency.getInstance(code).getDisplayName(Locale.getDefault()) }.getOrDefault(code)
    return "$name ($code, ${Money.symbol(code)})"
}

@Composable
private fun CurrencyPickerDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val all = remember {
        Currency.getAvailableCurrencies()
            .map { it.currencyCode to it.getDisplayName(Locale.getDefault()) }
            .sortedBy { it.second }
    }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) all else all.filter { (code, name) -> code.contains(q, true) || name.contains(q, true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose currency") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search, e.g. BDT or Taka") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp).padding(top = 8.dp)) {
                    items(filtered, key = { it.first }) { (code, name) ->
                        Text(
                            "$code — $name",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(code) }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
