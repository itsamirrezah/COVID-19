package com.itsamirrezah.covid19.util.chart

import android.content.Context
import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.ui.model.MarkerData

class MarkerView(
    context: Context,
    layoutResource: Int
) : MarkerView(context, layoutResource) {

    private var tvConfirmed: TextView = findViewById(R.id.tvValue)
    private var ivColor: ImageView = findViewById(R.id.ivColor)
    private var tvDate: TextView = findViewById(R.id.tvDate)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val data = e!!.data as MarkerData

        tvConfirmed.text = data.value.toString()
        tvDate.text = data.dateString
        ivColor.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, data.color))

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height - 10).toFloat())
    }
}