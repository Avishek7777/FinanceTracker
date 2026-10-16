package com.avishek.financetracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Currency
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val currencyKey = stringPreferencesKey("currency_code")

    /** Defaults to the currency of the phone's region, falling back to USD. */
    val currencyCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[currencyKey] ?: defaultCurrencyCode()
    }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { it[currencyKey] = code }
    }

    private fun defaultCurrencyCode(): String =
        runCatching { Currency.getInstance(Locale.getDefault()).currencyCode }.getOrDefault("USD")
}
