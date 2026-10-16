package com.avishek.financetracker.data

import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) {
    val allTransactions: Flow<List<TransactionWithCategory>> = transactionDao.observeAll()
    val activeCategories: Flow<List<Category>> = categoryDao.observeActive()
    val categoryUsage: Flow<List<CategoryUsage>> = transactionDao.observeCategoryUsage()

    fun transactionsIn(month: YearMonth): Flow<List<TransactionWithCategory>> =
        transactionDao.observeBetween(month.atDay(1), month.atEndOfMonth())

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getById(id)

    suspend fun getAllForExport(): List<TransactionWithCategory> = transactionDao.getAll()

    suspend fun save(transaction: Transaction) {
        if (transaction.id == 0L) transactionDao.insert(transaction) else transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    /** Re-inserts a deleted transaction with its original id (used by "Undo"). */
    suspend fun restore(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun addCategory(name: String, iconKey: String, color: Long, type: TransactionType) {
        val order = categoryDao.maxSortOrder(type) + 1
        categoryDao.insert(Category(name = name, iconKey = iconKey, color = color, type = type, sortOrder = order))
    }

    suspend fun archiveCategory(id: Long) = categoryDao.archive(id)
}
