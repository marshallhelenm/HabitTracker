package com.example.habittracker.ui

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// Amounts are piggy coins rather than a currency: "1 coin", "5 coins", "16.25 coins", "2,000 coins".
// Whole amounts drop the decimals; fractional ones always show two places, so 412.5 reads "412.50".
fun formatCoins(amount: BigDecimal, locale: Locale = Locale.getDefault()): String {
    val whole = amount.stripTrailingZeros().scale() <= 0
    val number = DecimalFormat(if (whole) "#,##0" else "#,##0.00", DecimalFormatSymbols.getInstance(locale)).format(amount)
    return if (amount.compareTo(BigDecimal.ONE) == 0) "$number coin" else "$number coins"
}
