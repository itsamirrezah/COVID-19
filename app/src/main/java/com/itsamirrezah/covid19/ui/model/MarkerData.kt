package com.itsamirrezah.covid19.ui.model

import com.itsamirrezah.covid19.util.Utils
import org.threeten.bp.LocalDate

class MarkerData(
    localDate: LocalDate,
    value: Int,
    val color: Int
) {
    val dateString: String =
        Utils.shortRelativeDate(localDate)
    val valueString = Utils.toNumberSeparator(value.toLong())
}