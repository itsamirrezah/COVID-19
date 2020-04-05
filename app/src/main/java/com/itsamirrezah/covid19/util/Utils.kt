package com.itsamirrezah.covid19.util

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.DecimalFormat
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
            val firstDigit = clusterCasesCount.toString()[0].toString().toInt()

            val clusterText = when {
                clusterCasesCount.length() < 2 -> clusterCasesCount
                else ->
                    firstDigit * (10.0.pow(clusterCasesCount.length() - 1).toInt())
            }
            return compactShortDigit(clusterText)
        }

        //e.g:
        //5,000 -> 5K
        fun compactShortDigit(number: Int): String {
            if (number < 1000)
                return "" + number
            val exp = (ln(number.toDouble()) / ln(1000.0)).toInt()
            return String.format(
                "%s%c",
                DecimalFormat("0.#").format(number / 1000.0.pow(exp.toDouble())),
                "KM"[exp - 1]
            )
        }

        fun toLocalDate(utcDate: String): LocalDate {
            return LocalDate.parse(utcDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        }

        //e.g: (2020-3-22)-3 = (2020-3-19)
        fun minusDateByInt(date: LocalDate, value: Long): LocalDate? {
            return date.minusDays(value)
        }

        //e.g: Dec 18
        fun shortRelativeDate(localDate: LocalDate): String {
            return localDate.format(DateTimeFormatter.ofPattern("MMM dd"))
        }

        fun blendColors(context: Context, colorRes1: Int, ColorRes2: Int, ratio: Float): Int {
            return ColorUtils.blendARGB(
                ContextCompat.getColor(context, colorRes1),
                ContextCompat.getColor(context, ColorRes2),
                ratio
            )
        }
    }
}