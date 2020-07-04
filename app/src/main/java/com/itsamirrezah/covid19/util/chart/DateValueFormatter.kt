package com.itsamirrezah.covid19.util.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import com.itsamirrezah.covid19.ui.model.TimelineData

class DateValueFormatter(
    private val timelines: List<TimelineData>
) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return timelines[value.toInt()].relativeDate
    }
}