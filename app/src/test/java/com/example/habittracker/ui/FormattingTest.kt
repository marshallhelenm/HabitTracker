package com.example.habittracker.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class FormattingTest {
    private fun coins(amount: String) = formatCoins(BigDecimal(amount), Locale.US)

    @Test
    fun wholeAmounts_dropTheDecimals() {
        assertEquals("5 coins", coins("5"))
        assertEquals("5 coins", coins("5.00"))
        assertEquals("0 coins", coins("0"))
    }

    @Test
    fun exactlyOne_isSingular() {
        assertEquals("1 coin", coins("1.00"))
    }

    @Test
    fun fractionalAmounts_showTwoPlaces() {
        assertEquals("16.25 coins", coins("16.25"))
        assertEquals("412.50 coins", coins("412.5"))
        assertEquals("0.25 coins", coins("0.25"))
    }

    @Test
    fun largeAmounts_areGrouped() {
        assertEquals("2,000 coins", coins("2000"))
    }
}
