package com.itsamirrezah.covid19.util.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import com.itsamirrezah.covid19.util.Utils

class CompactDigitValueFormatter : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return Utils.compactShortDigit(value.toInt())
    }
}