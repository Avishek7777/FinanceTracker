package com.avishek.financetracker

import android.app.Application
import com.avishek.financetracker.data.AppDatabase
import com.avishek.financetracker.data.FinanceRepository
import com.avishek.financetracker.data.SettingsRepository

class FinanceApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Simple manual dependency injection: one place that creates and holds shared objects. */
class AppContainer(app: Application) {
    private val database = AppDatabase.create(app)
    val financeRepository = FinanceRepository(database.transactionDao(), database.categoryDao())
    val settingsRepository = SettingsRepository(app)
}
