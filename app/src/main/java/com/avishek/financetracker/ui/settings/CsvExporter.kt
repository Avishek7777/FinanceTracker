package com.avishek.financetracker.ui.settings

import com.avishek.financetracker.data.TransactionWithCategory
import java.math.BigDecimal

object CsvExporter {
    private const val HEADER = "date,type,category,amount,currency,note"

    fun toCsv(rows: List<TransactionWithCategory>, currencyCode: String): String = buildString {
        appendLine(HEADER)
        rows.forEach { row ->
            val tx = row.transaction
            appendLine(
                listOf(
                    tx.date.toString(),
                    tx.type.name.lowercase(),
                    row.category.name,
                    BigDecimal.valueOf(tx.amountMinor, 2).toPlainString(),
                    currencyCode,
                    tx.note,
                ).joinToString(",") { escape(it) },
            )
        }
    }

    /** Quotes a field when it contains a comma, quote or newline (RFC 4180). */
    fun escape(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }
}
