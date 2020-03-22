package com.itsamirrezah.covid19.util

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

class Utils {

    companion object {

        //number of digits
        fun Int.length() = when (this) {
            0 -> 1
            else -> log10(abs(toDouble())).toInt() + 1
        }

        //round down
        fun randDigit(clusterCasesCount: Int): String {
            val clusterText = when {
                clusterCasesCount.length() < 2 -> 1
                (10.0.pow(clusterCasesCount.length().toDouble()) / 2) > clusterCasesCount ->
                    10.0.pow(clusterCasesCount.length() - 1).toInt()
                else -> (10.0.pow(clusterCasesCount.length()) / 2).toInt()
            }
            return compactShortDigit(clusterText)
        }

        //e.g:
        //5,000 -> 5K
        private fun compactShortDigit(number: Int): String {
            if (number < 1000)
                return "" + number
            val exp = (ln(number.toDouble()) / ln(1000.0)).toInt()
            return String.format(
                "%d %c",
                number / 1000.0.pow(exp.toDouble()).toInt(),
                "KM"[exp - 1]
            )
        }

        fun toLocalDate(utcDate: String): LocalDate {
            return LocalDate.parse(utcDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        }
    }
}