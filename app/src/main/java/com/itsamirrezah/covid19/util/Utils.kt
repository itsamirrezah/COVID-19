package com.itsamirrezah.covid19.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.DecimalFormat
import java.text.NumberFormat
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

        fun toNumberSeparator(value: Long): String {
            return NumberFormat.getNumberInstance().format(value)
        }

        fun toLocalDateTime(utcDate: String): LocalDate {
            return LocalDate.parse(utcDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        }

        fun toLocalDate(utcDate: String): LocalDate {
            val splitDate = utcDate.split("/")
            return LocalDate.of(splitDate[2].toInt(), splitDate[0].toInt(), splitDate[1].toInt())
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
                getColor(context, colorRes1),
                getColor(context, ColorRes2),
                ratio
            )
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //find currently focused view
            var view = activity.currentFocus
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun getColor(context: Context, color: Int): Int {
            return ContextCompat.getColor(context, color)
        }

    }
}