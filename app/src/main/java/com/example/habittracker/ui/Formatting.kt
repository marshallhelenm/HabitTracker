package com.example.habittracker.ui

import java.math.BigDecimal
import java.text.NumberFormat

fun formatMoney(amount: BigDecimal): String = NumberFormat.getCurrencyInstance().format(amount)
