package com.avishek.financetracker.ui.overview

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth

data class CategoryTotal(val category: Category, val amountMinor: Long, val fraction: Float)

data class OverviewUiState(
    val month: YearMonth = YearMonth.now(),
    val incomeMinor: Long = 0,
    val expenseMinor: Long = 0,
    val expenseByCategory: List<CategoryTotal> = emptyList(),
    val transactionCount: Int = 0,
    val currencyCode: String = "USD",
) {
    val balanceMinor: Long get() = incomeMinor - expenseMinor
}

private data class MonthSummary(
    val month: YearMonth,
    val income: Long,
    val expense: Long,
    val byCategory: List<CategoryTotal>,
    val count: Int,
)

@OptIn(ExperimentalCoroutinesApi::class)
class OverviewViewModel(
    repository: FinanceRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())

    private val summary = month.flatMapLatest { m ->
        repository.transactionsIn(m).map { items ->
            val income = items.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amountMinor }
            val expenses = items.filter { it.transaction.type == TransactionType.EXPENSE }
            val expenseTotal = expenses.sumOf { it.transaction.amountMinor }
            val byCategory = expenses
                .groupBy { it.category }
                .map { (category, txs) ->
                    val sum = txs.sumOf { it.transaction.amountMinor }
                    CategoryTotal(category, sum, if (expenseTotal == 0L) 0f else sum.toFloat() / expenseTotal)
                }
                .sortedByDescending { it.amountMinor }
            MonthSummary(m, income, expenseTotal, byCategory, items.size)
        }
    }

    val uiState: StateFlow<OverviewUiState> = combine(summary, settings.currencyCode) { s, currency ->
        OverviewUiState(
            month = s.month,
            incomeMinor = s.income,
            expenseMinor = s.expense,
            expenseByCategory = s.byCategory,
            transactionCount = s.count,
            currencyCode = currency,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OverviewUiState())

    fun previousMonth() = month.update { it.minusMonths(1) }

    fun nextMonth() = month.update { it.plusMonths(1) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                OverviewViewModel(container.financeRepository, container.settingsRepository)
            }
        }
    }
}
