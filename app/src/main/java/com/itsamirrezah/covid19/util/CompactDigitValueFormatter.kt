package com.itsamirrezah.covid19.util

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class CompactDigitValueFormatter: ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return Utils.compactShortDigit(value.toInt())
    }
}