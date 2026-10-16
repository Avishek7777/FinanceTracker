package com.avishek.financetracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.avishek.financetracker.data.FinanceRepository
import com.avishek.financetracker.data.SettingsRepository
import com.avishek.financetracker.data.Transaction
import com.avishek.financetracker.data.TransactionType
import com.avishek.financetracker.data.TransactionWithCategory
import com.avishek.financetracker.ui.common.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DayGroup(
    val date: LocalDate,
    /** Income minus expenses for the day. */
    val netMinor: Long,
    val items: List<TransactionWithCategory>,
)

data class HistoryUiState(
    val query: String = "",
    /** null = show all types. */
    val typeFilter: TransactionType? = null,
    val days: List<DayGroup> = emptyList(),
    val hasAnyTransactions: Boolean = false,
    val currencyCode: String = "USD",
)

class HistoryViewModel(
    private val repository: FinanceRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val typeFilter = MutableStateFlow<TransactionType?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.allTransactions,
        query,
        typeFilter,
        settings.currencyCode,
    ) { all, q, type, currency ->
        val needle = q.trim()
        val filtered = all.filter { item ->
            (type == null || item.transaction.type == type) &&
                (
                    needle.isEmpty() ||
                        item.transaction.note.contains(needle, ignoreCase = true) ||
                        item.category.name.contains(needle, ignoreCase = true)
                    )
        }
        val days = filtered
            .groupBy { it.transaction.date }
            .map { (date, items) ->
                val net = items.sumOf {
                    if (it.transaction.type == TransactionType.INCOME) it.transaction.amountMinor else -it.transaction.amountMinor
                }
                DayGroup(date, net, items)
            }
        HistoryUiState(q, type, days, all.isNotEmpty(), currency)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onTypeFilterChange(value: TransactionType?) {
        typeFilter.value = value
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { repository.delete(transaction) }
    }

    fun undoDelete(transaction: Transaction) {
        viewModelScope.launch { repository.restore(transaction) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                HistoryViewModel(container.financeRepository, container.settingsRepository)
            }
        }
    }
}
