package com.avishek.financetracker.ui.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.avishek.financetracker.data.Category
import com.avishek.financetracker.data.FinanceRepository
import com.avishek.financetracker.data.SettingsRepository
import com.avishek.financetracker.data.Transaction
import com.avishek.financetracker.data.TransactionType
import com.avishek.financetracker.ui.common.Money
import com.avishek.financetracker.ui.common.appContainer
import com.avishek.financetracker.ui.common.applyKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddEditForm(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val categoryId: Long? = null,
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
)

data class AddEditUiState(
    val form: AddEditForm = AddEditForm(),
    val isEditing: Boolean = false,
    /** Categories for the selected type, most-used first. */
    val categories: List<Category> = emptyList(),
    val currencyCode: String = "USD",
) {
    val canSave: Boolean
        get() = (Money.parseToMinor(form.amountText) ?: 0L) > 0L &&
            form.categoryId != null && categories.any { it.id == form.categoryId }
}

class AddEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: FinanceRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>(ARG_ID) ?: NEW_ID
    private var original: Transaction? = null

    private val form = MutableStateFlow(AddEditForm())

    val uiState: StateFlow<AddEditUiState> = combine(
        form,
        repository.activeCategories,
        repository.categoryUsage,
        settings.currencyCode,
    ) { form, categories, usage, currency ->
        val counts = usage.associate { it.categoryId to it.count }
        AddEditUiState(
            form = form,
            isEditing = transactionId != NEW_ID,
            categories = categories
                .filter { it.type == form.type }
                .sortedByDescending { counts[it.id] ?: 0 },
            currencyCode = currency,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddEditUiState())

    init {
        if (transactionId != NEW_ID) {
            viewModelScope.launch {
                repository.getTransaction(transactionId)?.let { tx ->
                    original = tx
                    form.value = AddEditForm(
                        type = tx.type,
                        amountText = Money.minorToInput(tx.amountMinor),
                        categoryId = tx.categoryId,
                        note = tx.note,
                        date = tx.date,
                    )
                }
            }
        }
    }

    fun onKey(key: String) = form.update { it.copy(amountText = applyKey(it.amountText, key)) }

    fun onTypeChange(type: TransactionType) = form.update {
        if (it.type == type) it else it.copy(type = type, categoryId = null)
    }

    fun onCategorySelected(id: Long) = form.update { it.copy(categoryId = id) }

    fun onNoteChange(note: String) = form.update { it.copy(note = note) }

    fun onDateChange(date: LocalDate) = form.update { it.copy(date = date) }

    fun save(onDone: () -> Unit) {
        val state = uiState.value
        val amount = Money.parseToMinor(state.form.amountText) ?: return
        val categoryId = state.form.categoryId ?: return
        viewModelScope.launch {
            val base = original ?: Transaction(amountMinor = 0, type = state.form.type, categoryId = 0, date = state.form.date)
            repository.save(
                base.copy(
                    amountMinor = amount,
                    type = state.form.type,
                    categoryId = categoryId,
                    note = state.form.note.trim(),
                    date = state.form.date,
                ),
            )
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val tx = original ?: return
        viewModelScope.launch {
            repository.delete(tx)
            onDone()
        }
    }

    companion object {
        const val ARG_ID = "id"
        const val NEW_ID = -1L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                AddEditViewModel(createSavedStateHandle(), container.financeRepository, container.settingsRepository)
            }
        }
    }
}
