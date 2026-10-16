package com.avishek.financetracker.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.avishek.financetracker.data.Category
import com.avishek.financetracker.data.FinanceRepository
import com.avishek.financetracker.data.SettingsRepository
import com.avishek.financetracker.data.TransactionType
import com.avishek.financetracker.ui.common.appContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val repository: FinanceRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    val currencyCode: StateFlow<String> =
        settings.currencyCode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val categories: StateFlow<List<Category>> =
        repository.activeCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCurrency(code: String) {
        viewModelScope.launch { settings.setCurrencyCode(code) }
    }

    fun addCategory(name: String, iconKey: String, color: Long, type: TransactionType) {
        viewModelScope.launch { repository.addCategory(name.trim(), iconKey, color, type) }
    }

    fun removeCategory(id: Long) {
        viewModelScope.launch { repository.archiveCategory(id) }
    }

    /** Writes every transaction as CSV to [uri]. Calls [onResult] with a user-facing message. */
    fun exportCsv(contentResolver: ContentResolver, uri: Uri, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val message = runCatching {
                val rows = repository.getAllForExport()
                val csv = CsvExporter.toCsv(rows, currencyCode.value)
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                        ?: error("Could not open file")
                }
                "Exported ${rows.size} transactions"
            }.getOrElse { "Export failed: ${it.message}" }
            onResult(message)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SettingsViewModel(container.financeRepository, container.settingsRepository)
            }
        }
    }
}
