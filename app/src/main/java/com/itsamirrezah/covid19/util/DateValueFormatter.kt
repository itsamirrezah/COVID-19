package com.itsamirrezah.covid19.util

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import org.threeten.bp.LocalDate

class DateValueFormatter(
    private val lastDate: LocalDate,
    private val recordedSize: Int
) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val date = Utils.minusDateByInt(lastDate, (recordedSize - 1) - value.toLong())
        return Utils.shortRelativeDate(date!!)
    }
}