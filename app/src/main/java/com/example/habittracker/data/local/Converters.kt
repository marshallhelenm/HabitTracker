package com.example.habittracker.data.local

import androidx.room.TypeConverter
import com.example.habittracker.model.SavingsGoalStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun stringToBigDecimal(value: String?): BigDecimal? = value?.let(::BigDecimal)

    @TypeConverter
    fun savingsGoalStatusToString(value: SavingsGoalStatus?): String? = value?.name

    @TypeConverter
    fun stringToSavingsGoalStatus(value: String?): SavingsGoalStatus? = value?.let(SavingsGoalStatus::valueOf)
}
