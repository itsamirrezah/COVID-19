package com.itsamirrezah.covid19.util

import org.threeten.bp.LocalDate

class MarkerData(
    localDate: LocalDate,
    val value: Int,
    val color: Int
) {
    val dateString: String = Utils.shortRelativeDate(localDate)
}