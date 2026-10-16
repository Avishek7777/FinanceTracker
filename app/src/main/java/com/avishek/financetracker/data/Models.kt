package com.avishek.financetracker.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate

enum class TransactionType { EXPENSE, INCOME }

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    /** ARGB colour, e.g. 0xFF2E7D32. */
    val color: Long,
    val type: TransactionType,
    val sortOrder: Int = 0,
    /** Archived categories are hidden from pickers but keep their past transactions. */
    val archived: Boolean = false,
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("date")],
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Amount in hundredths (12.50 -> 1250). Always positive; [type] gives the direction. */
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: Long,
    val note: String = "",
    val date: LocalDate,
    val createdAt: Long = System.currentTimeMillis(),
)

data class TransactionWithCategory(
    @Embedded val transaction: Transaction,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category,
)

data class CategoryUsage(val categoryId: Long, val count: Int)
