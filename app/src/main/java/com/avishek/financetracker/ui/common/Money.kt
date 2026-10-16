package com.avishek.financetracker.ui.common

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object Money {
    private val symbolCache = ConcurrentHashMap<String, String>()

    /** Formats hundredths as e.g. "৳1,250.50". */
    fun format(amountMinor: Long, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val pattern = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(locale))
        val value = BigDecimal.valueOf(amountMinor, 2)
        val sign = if (amountMinor < 0) "-" else ""
        return sign + symbol(currencyCode) + pattern.format(value.abs())
    }

    /**
     * The currency's own local symbol where possible (e.g. "৳" for BDT, "₹" for INR) instead of the
     * ISO code that many device locales would show.
     */
    fun symbol(currencyCode: String): String = symbolCache.getOrPut(currencyCode) {
        val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
            ?: return@getOrPut currencyCode
        val defaultSymbol = currency.getSymbol(Locale.getDefault())
        if (defaultSymbol != currencyCode) return@getOrPut defaultSymbol
        Locale.getAvailableLocales()
            .firstOrNull { locale ->
                locale.country.isNotEmpty() &&
                    runCatching { Currency.getInstance(locale) }.getOrNull() == currency
            }
            ?.let { currency.getSymbol(it) }
            ?: currencyCode
    }

    /** Parses number-pad text like "12.5" into hundredths (1250). Returns null for invalid input. */
    fun parseToMinor(text: String): Long? {
        if (text.isBlank()) return null
        val value = text.toBigDecimalOrNull() ?: return null
        if (value.scale() > 2 || value.signum() < 0) return null
        return runCatching { value.movePointRight(2).longValueExact() }.getOrNull()
    }

    /** Inverse of [parseToMinor], used when editing an existing transaction (1250 -> "12.5"). */
    fun minorToInput(amountMinor: Long): String =
        BigDecimal.valueOf(amountMinor, 2).stripTrailingZeros().toPlainString()
}

/** Applies a number-pad key press to the current amount text. Keeps max 2 decimals and 10 digits. */
fun applyKey(current: String, key: String): String = when (key) {
    "⌫" -> current.dropLast(1)
    "." -> when {
        current.contains('.') -> current
        current.isEmpty() -> "0."
        else -> "$current."
    }
    else -> {
        val decimals = current.substringAfter('.', missingDelimiterValue = "")
        val digits = current.count { it.isDigit() }
        when {
            current.contains('.') && decimals.length >= 2 -> current
            digits >= 10 -> current
            current == "0" -> key
            else -> current + key
        }
    }
}
