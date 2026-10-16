package com.avishek.financetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import androidx.room.Transaction as RoomTransaction

@Dao
interface TransactionDao {
    @RoomTransaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionWithCategory>>

    @RoomTransaction
    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC, createdAt DESC")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<TransactionWithCategory>>

    @RoomTransaction
    @Query("SELECT * FROM transactions ORDER BY date ASC, createdAt ASC")
    suspend fun getAll(): List<TransactionWithCategory>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT categoryId, COUNT(*) AS count FROM transactions GROUP BY categoryId")
    fun observeCategoryUsage(): Flow<List<CategoryUsage>>

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY type, sortOrder, name")
    fun observeActive(): Flow<List<Category>>

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM categories WHERE type = :type")
    suspend fun maxSortOrder(type: TransactionType): Int

    @Insert
    suspend fun insert(category: Category): Long

    @Query("UPDATE categories SET archived = 1 WHERE id = :id")
    suspend fun archive(id: Long)
}
