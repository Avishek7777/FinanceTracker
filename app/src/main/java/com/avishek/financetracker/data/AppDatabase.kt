package com.avishek.financetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Category::class, Transaction::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "finance.db")
                .addCallback(SeedCallback)
                .build()
    }
}

/** Inserts the default categories the first time the database is created. */
private object SeedCallback : RoomDatabase.Callback() {
    private data class Seed(val name: String, val icon: String, val color: Long, val type: TransactionType)

    private val defaults = listOf(
        Seed("Food", "restaurant", 0xFFE57373, TransactionType.EXPENSE),
        Seed("Groceries", "grocery", 0xFF81C784, TransactionType.EXPENSE),
        Seed("Transport", "transport", 0xFF64B5F6, TransactionType.EXPENSE),
        Seed("Shopping", "shopping", 0xFFBA68C8, TransactionType.EXPENSE),
        Seed("Bills", "bills", 0xFFFFB74D, TransactionType.EXPENSE),
        Seed("Health", "health", 0xFFF06292, TransactionType.EXPENSE),
        Seed("Entertainment", "movie", 0xFF4DB6AC, TransactionType.EXPENSE),
        Seed("Other", "other", 0xFF90A4AE, TransactionType.EXPENSE),
        Seed("Salary", "salary", 0xFF43A047, TransactionType.INCOME),
        Seed("Freelance", "work", 0xFF1E88E5, TransactionType.INCOME),
        Seed("Gift", "gift", 0xFFFB8C00, TransactionType.INCOME),
        Seed("Other", "other", 0xFF90A4AE, TransactionType.INCOME),
    )

    override fun onCreate(db: SupportSQLiteDatabase) {
        defaults.forEachIndexed { index, seed ->
            db.execSQL(
                "INSERT INTO categories (name, iconKey, color, type, sortOrder, archived) VALUES (?, ?, ?, ?, ?, 0)",
                arrayOf<Any>(seed.name, seed.icon, seed.color, seed.type.name, index),
            )
        }
    }
}
