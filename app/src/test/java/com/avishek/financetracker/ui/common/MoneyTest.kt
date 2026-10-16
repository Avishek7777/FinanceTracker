package com.avishek.financetracker.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class MoneyTest {
    @Test
    fun parseToMinor_handlesWholeAndDecimalAmounts() {
        assertEquals(1250L, Money.parseToMinor("12.5"))
        assertEquals(1205L, Money.parseToMinor("12.05"))
        assertEquals(1200L, Money.parseToMinor("12"))
        assertEquals(1200L, Money.parseToMinor("12."))
        assertEquals(50L, Money.parseToMinor("0.5"))
    }

    @Test
    fun parseToMinor_rejectsInvalidInput() {
        assertNull(Money.parseToMinor(""))
        assertNull(Money.parseToMinor("abc"))
        assertNull(Money.parseToMinor("1.234"))
        assertNull(Money.parseToMinor("-5"))
    }

    @Test
    fun minorToInput_roundTrips() {
        assertEquals("12.5", Money.minorToInput(1250))
        assertEquals("12", Money.minorToInput(1200))
        assertEquals("0.05", Money.minorToInput(5))
        assertEquals(1250L, Money.parseToMinor(Money.minorToInput(1250)))
    }

    @Test
    fun format_groupsThousandsAndKeepsSign() {
        assertEquals("$1,234.50", Money.format(123450, "USD", Locale.US))
        assertEquals("-$5.00", Money.format(-500, "USD", Locale.US))
    }

    @Test
    fun applyKey_buildsAmounts() {
        assertEquals("1", applyKey("", "1"))
        assertEquals("5", applyKey("0", "5"))
        assertEquals("0.", applyKey("", "."))
        assertEquals("1.", applyKey("1.", "."))
        assertEquals("1.25", applyKey("1.2", "5"))
        assertEquals("1.25", applyKey("1.25", "9"))
        assertEquals("1.2", applyKey("1.25", "⌫"))
        assertEquals("", applyKey("", "⌫"))
    }
}
