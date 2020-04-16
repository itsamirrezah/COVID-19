package com.itsamirrezah.covid19.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.data.api.CovidApiImp
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import com.itsamirrezah.covid19.ui.model.MarkerData
import com.itsamirrezah.covid19.util.Utils
import com.itsamirrezah.covid19.util.chart.CompactDigitValueFormatter
import com.itsamirrezah.covid19.util.chart.DateValueFormatter
import com.itsamirrezah.covid19.util.chart.MarkerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate

class AreaDetailFragment : BottomSheetDialogFragment() {

    private lateinit var areaCaseModel: AreaCasesModel
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_area_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            areaCaseModel = it.getParcelable("AREA_CASE_MODEL_EXTRA")!!
            view.findViewById<TextView>(R.id.tvCountry).text = areaCaseModel.country
            view.findViewById<TextView>(R.id.tvConfirmed).text = areaCaseModel.confirmedString
            view.findViewById<TextView>(R.id.tvDeaths).text = areaCaseModel.deathString
            if (areaCaseModel.recovered > 0)
                view.findViewById<TextView>(R.id.tvRecovered).text =
                    areaCaseModel.recoveredString
            setupPieChart(view)
            setupLineChart(view)
            setupBarChart(view)

            if (areaCaseModel.timelines == null) {
                getAreaCases()
            } else {
                setupLineData()
                setupBarData()
            }

        }
    }

    private fun setupBarChart(view: View) {
        barChart = view.findViewById(R.id.barChart)
        barChart.setNoDataTextColor(ContextCompat.getColor(context!!, R.color.grey_300))
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawValueAboveBar(false)
        barChart.isHighlightFullBarEnabled = true
        barChart.isAutoScaleMinMaxEnabled = true
        //custom marker
        val markerView =
            MarkerView(context!!, R.layout.chart_marker_view)
        markerView.chartView = barChart
        barChart.marker = markerView
        //y-axis
        val yAxis = barChart.axisLeft
        //y-axis value formatter
        yAxis.valueFormatter = CompactDigitValueFormatter()
        yAxis.textColor = ContextCompat.getColor(context!!, R.color.grey_300)
        yAxis.axisMinimum = 0f
        yAxis.enableGridDashedLine(10f, 5f, 0f)
        //add extra space over the maximum bar
        yAxis.spaceTop = 30f
        //x-axis
        val xAxis = barChart.xAxis
        xAxis.axisMinimum = 0f
        xAxis.textColor = ContextCompat.getColor(context!!, R.color.grey_300)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.enableGridDashedLine(10f, 5f, 0f)
        //disable dual y-axes
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
    }

    private fun setupBarData() {
        val entries = mutableListOf<BarEntry>()
        //list.indices: returns an [IntRange] of the valid indices for this collection
        for (count in areaCaseModel.dailyTimelines!!.indices) {
            val day = areaCaseModel.dailyTimelines!![count]

            entries.add(
                BarEntry(
                    count.toFloat(),
                    floatArrayOf(
                        day.second.first.toFloat(),
                        day.second.second.toFloat(),
                        day.second.third.toFloat()
                    ),
                    MarkerData(day.first, day.second.first, R.color.yellow_A700)
                )
            )
        }

        val barDataSet = BarDataSet(entries, "")
        //don't draw values on bars
        barDataSet.setDrawValues(false)
        barDataSet.setDrawIcons(false)
        //bar colors
        barDataSet.colors = mutableListOf(
            ContextCompat.getColor(context!!, R.color.yellow_A700), //confirmed
            ContextCompat.getColor(context!!, R.color.red_A700), //deaths
            ContextCompat.getColor(context!!, R.color.green_A700) //recovered
        )
        barDataSet.highLightColor = ContextCompat.getColor(context!!, R.color.grey_100)
        barChart.data = BarData(barDataSet)
        //show 15 days of data
        barChart.setVisibleXRangeMaximum(30f)
        //move the viewport to right side of the chart
        barChart.moveViewToX(barChart.xChartMax)

        barChart.invalidate()
        barChart.animateY(1000)
        //x-axis value formatter
        barChart.xAxis.valueFormatter = DateValueFormatter(
            areaCaseModel.timelines!!.last().first,
            areaCaseModel.timelines!!.size
        )
    }

    private fun setupPieChart(view: View) {
        pieChart = view.findViewById(R.id.pieChart)
        pieChart.description.isEnabled = false
        pieChart.setNoDataTextColor(ContextCompat.getColor(context!!, R.color.grey_300))
        //padding for left/top/right & bottom of the chart
        pieChart.setExtraOffsets(0f, 5f, 0f, 5f)
        //rotation speed
        pieChart.dragDecelerationFrictionCoef = 0.95f
        //no draw hole
        pieChart.isDrawHoleEnabled = false
        pieChart.setDrawCenterText(false)
        //initial angle
        pieChart.rotationAngle = -30f
        // enable rotation of the chart by touch
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = false
        //display values as percentage
        pieChart.setUsePercentValues(true)
        //no legend
        pieChart.legend.isEnabled = false
        //set entry label
        pieChart.setEntryLabelColor(ContextCompat.getColor(context!!, R.color.grey_300))
        pieChart.setEntryLabelTextSize(10f)
        //animation
        pieChart.animateY(1000, Easing.EaseInOutQuad)

        setupPieData()
    }

    private fun setupPieData() {
        //active cases = confirmed cases - (deaths + recovered)
        val activeCases =
            areaCaseModel.confirmed.toLong() - (areaCaseModel.deaths.toLong() + areaCaseModel.recovered.toLong())

        val entries = mutableListOf(
            PieEntry(activeCases.toFloat(), "Active"),
            PieEntry(areaCaseModel.deaths.toFloat(), "Deaths"),
            PieEntry(areaCaseModel.recovered.toFloat(), "Recovered")
            //do not display entries with no values
        ).filter { it.value != 0f }

        //create a pie data set with mutable list of pie entries
        val dataset = PieDataSet(entries, "")

        dataset.sliceSpace = 2f
        dataset.selectionShift = 5f
        //set colors for each slice of pie chart
        dataset.colors = listOf(
            ContextCompat.getColor(context!!, R.color.yellow_A700),
            ContextCompat.getColor(context!!, R.color.red_A700),
            ContextCompat.getColor(context!!, R.color.green_A700)
        )

        //line over the chart
        dataset.valueLinePart1OffsetPercentage = 90f
        dataset.valueLineColor = ContextCompat.getColor(context!!, R.color.grey_300)
        dataset.valueLinePart1Length = 0.8f
        dataset.valueLinePart2Length = 1f

        dataset.valueTextColor = ContextCompat.getColor(context!!, R.color.grey_300)
        //display values outside of the chart
        dataset.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataset.valueTextSize = 10f

        val data = PieData(dataset)
        data.setValueFormatter(PercentFormatter(pieChart))
        pieChart.data = data
        pieChart.invalidate()

    }

    private fun getAreaCases() {
        val areaCasesRequest = CovidApiImp.getApi()
            .getAreaById(areaCaseModel.id)
            //map data model to ui model
            .map {
                val timelines: MutableList<Pair<LocalDate, Triple<Int, Int, Int>>> = mutableListOf()
                val dailyTimeline: MutableList<Pair<LocalDate, Triple<Int, Int, Int>>> =
                    mutableListOf()

                for ((index, timeline) in it.area.timelines.confirmed.timeline.toList().withIndex()) {
                    //gather information about area since first case confirmed
                    if (timeline.second <= 0)
                        continue

                    val confirmed = timeline.second
                    val deaths = it.area.timelines.deaths.timeline[timeline.first] ?: 0
                    val recovered = it.area.timelines.recovered.timeline[timeline.first] ?: 0
                    val localDate = Utils.toLocalDate(timeline.first)

                    timelines.add(
                        Pair(
                            localDate, Triple(confirmed!!, deaths, recovered)
                        )
                    )

                    val dailyConfirmed =
                        confirmed - it.area.timelines.confirmed.timeline.toList()
                            .getOrElse(index - 1) { Pair("", 0) }.second
                    val dailyDeaths =
                        deaths - it.area.timelines.deaths.timeline.toList()
                            .getOrElse(index - 1) { Pair("", 0) }.second
                    val dailyRecovered =
                        recovered - it.area.timelines.recovered.timeline.toList()
                            .getOrElse(index - 1) { Pair("", 0) }.second

                    dailyTimeline.add(
                        Pair(
                            localDate, Triple(dailyConfirmed, dailyDeaths, dailyRecovered)
                        )
                    )
                }

                areaCaseModel.timelines = timelines
                areaCaseModel.dailyTimelines = dailyTimeline
                areaCaseModel
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setupLineData()
                setupBarData()
            }, {
                print(it.message)
            })
    }

    private fun setupLineChart(view: View) {
        lineChart = view.findViewById(R.id.lineChart)
        lineChart.setNoDataTextColor(ContextCompat.getColor(context!!, R.color.grey_300))
        //padding for left/top/right & bottom of the chart
        lineChart.setExtraOffsets(0f, 5f, 0f, 5f)
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isAutoScaleMinMaxEnabled = true
        //no grid background
        lineChart.setDrawGridBackground(false)
        //custom marker
        val markerView =
            MarkerView(context!!, R.layout.chart_marker_view)
        markerView.chartView = lineChart
        lineChart.marker = markerView

        setupLineAxis()
        setupLineLegend()

    }

    private fun setupLineAxis() {
        // x-axis style
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        // vertical grid lines
        xAxis.enableGridDashedLine(10f, 5f, 0f)
        xAxis.textColor = ContextCompat.getColor(context!!, R.color.grey_300)
        //y-axis style
        val yAxis = lineChart.axisLeft
        // disable dual y-axis
        lineChart.axisRight.isEnabled = false
        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 5f, 0f)
        yAxis.spaceTop = 35f
        yAxis.textColor = ContextCompat.getColor(context!!, R.color.grey_300)
    }

    private fun setupLineLegend() {
        val legend: Legend = lineChart.legend
        // draw legend entries as lines
        legend.form = Legend.LegendForm.CIRCLE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.textSize = 10f
        legend.formSize = 12f
        legend.textColor = ContextCompat.getColor(context!!, R.color.grey_300)
    }

    private fun setupLineData() {

        val confirmedEntries = arrayListOf<Entry>()
        val deathEntries = arrayListOf<Entry>()
        val recoveredEntries = arrayListOf<Entry>()

        for ((count, value) in areaCaseModel.timelines!!.withIndex()) {
            val confirmedEntry = Entry(
                count.toFloat(),
                value.second.first.toFloat(),
                MarkerData(
                    value.first,
                    value.second.first,
                    R.color.yellow_A700
                )
            )
            val deathsEntry = Entry(
                count.toFloat(),
                value.second.second.toFloat(),
                MarkerData(
                    value.first,
                    value.second.second,
                    R.color.red_A700
                )
            )
            val recoveredEntry = Entry(
                count.toFloat(),
                value.second.third.toFloat(),
                MarkerData(
                    value.first,
                    value.second.third,
                    R.color.green_A700
                )
            )
            confirmedEntries.add(confirmedEntry)
            deathEntries.add(deathsEntry)
            recoveredEntries.add(recoveredEntry)
        }

        val confirmedSet: LineDataSet =
            setupLineData(confirmedEntries, "confirmed", R.color.yellow_700)
        val deathSet: LineDataSet = setupLineData(deathEntries, "deaths", R.color.red_A700)
        val recoveredSet: LineDataSet =
            setupLineData(recoveredEntries, "recovered", R.color.green_A700)


        lineChart.data = LineData(
            arrayListOf(
                confirmedSet, deathSet, recoveredSet
            ) as List<ILineDataSet>
        )
        lineChart.setVisibleXRangeMaximum(30f)
        lineChart.moveViewToX(lineChart.xChartMax)
        // draw points over time
        lineChart.animateX(500)
        //setup x-axis value formatter: display dates (Mar 13)
        lineChart.xAxis.valueFormatter = DateValueFormatter(
            areaCaseModel.timelines!!.last().first,
            areaCaseModel.timelines!!.size
        )
        //setup y-axis value formatter: display values in short compact format (12.5 K)
        lineChart.axisLeft.valueFormatter =
            CompactDigitValueFormatter()
    }

    private fun setupLineData(entries: List<Entry>, text: String, color: Int): LineDataSet {

        val dataset = LineDataSet(entries, text)
        dataset.mode = LineDataSet.Mode.CUBIC_BEZIER
        //line & circle point colors
        dataset.color = ContextCompat.getColor(context!!, color)
        dataset.setCircleColor(ContextCompat.getColor(context!!, color))
        //line thickness and point size
        dataset.lineWidth = 2f
        dataset.circleRadius = 2.5f
        // draw line
        dataset.enableDashedLine(10f, 0f, 0f)
        //do not use icons for circle points
        dataset.setDrawIcons(false)

        dataset.setDrawCircleHole(false)
        dataset.setDrawValues(false)

        return dataset
    }
}
