package com.itsamirrezah.covid19.ui.model

import com.itsamirrezah.covid19.util.Utils
import org.threeten.bp.LocalDate

class MarkerData(
    localDate: LocalDate,
    val value: Int,
    val color: Int
) {
    val dateString: String =
        Utils.shortRelativeDate(localDate)
}