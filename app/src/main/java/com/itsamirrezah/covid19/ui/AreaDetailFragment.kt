package com.itsamirrezah.covid19.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.data.novelapi.NovelApiImp
import com.itsamirrezah.covid19.data.novelapi.model.Timelines
import com.itsamirrezah.covid19.ui.model.AreaModel
import com.itsamirrezah.covid19.ui.model.Cases
import com.itsamirrezah.covid19.ui.model.MarkerData
import com.itsamirrezah.covid19.ui.model.TimelineData
import com.itsamirrezah.covid19.util.Utils
import com.itsamirrezah.covid19.util.chart.CompactDigitValueFormatter
import com.itsamirrezah.covid19.util.chart.DateValueFormatter
import com.itsamirrezah.covid19.util.chart.MarkerView
import kotlinx.coroutines.*

class AreaDetailFragment : BottomSheetDialogFragment() {

    private lateinit var area: AreaModel
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
        arguments?.let {
            area = it.getParcelable("AREA_CASE_MODEL_EXTRA")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_area_detail, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener {
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            //skip the collapsed state
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            requestAndShowData()
        }
        return bottomSheetDialog
    }

    private fun requestAndShowData() {

        lifecycleScope.launch(Dispatchers.Default) {
            if (area.timelines == null) {
                withContext(Dispatchers.IO) {
                    getArea()
                }
            }
            val pieDataDeferred = async { setupPieData() }
            val lineDataDeferred = async { setupLineData() }
            val barDataDeferred = async { setupBarData() }

            setupCharts()
            awaitChartsData(pieDataDeferred, lineDataDeferred, barDataDeferred)
            withContext(Dispatchers.Main) {
                animateCharts()
            }
        }

    }

    private fun setupCharts() {
        setupPieChart()
        setupLineChart()
        setupBarChart()
    }

    private suspend fun awaitChartsData(
        pieData: Deferred<PieData>,
        lineData: Deferred<LineData>,
        barData: Deferred<BarData>
    ) {
        pieChart.data = pieData.await()
        lineChart.data = lineData.await()
        barChart.data = barData.await()
    }

    private suspend fun animateCharts() {
        pieChart.spin(200, 0f, 360f, Easing.EaseInOutQuad)
        delay(150)
        lineChart.animateX(200, Easing.EaseInOutQuad)
        delay(150)
        barChart.animateY(200, Easing.EaseInOutQuad)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvCountry).text = area.country
        view.findViewById<TextView>(R.id.tvConfirmed).text = area.confirmedString
        view.findViewById<TextView>(R.id.tvDeaths).text = area.deathString
        view.findViewById<TextView>(R.id.tvRecovered).text = area.recoveredString
        pieChart = view.findViewById(R.id.pieChart)
        lineChart = view.findViewById(R.id.lineChart)
        barChart = view.findViewById(R.id.barChart)
    }

    private fun setupBarChart() {

        barChart.setNoDataTextColor(Utils.getColor(requireContext(), R.color.grey_300))
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawValueAboveBar(false)
        barChart.isHighlightFullBarEnabled = true
        barChart.isAutoScaleMinMaxEnabled = true
        //disable y-axis scale on zoom
        barChart.isScaleYEnabled = false
        //custom marker
        val markerView =
            MarkerView(requireContext(), R.layout.chart_marker_view)
        markerView.chartView = barChart
        barChart.marker = markerView
        //y-axis
        val yAxis = barChart.axisLeft
        //y-axis value formatter
        yAxis.valueFormatter = CompactDigitValueFormatter()
        yAxis.textColor = Utils.getColor(requireContext(), R.color.grey_300)
        yAxis.axisMinimum = 0f
        yAxis.enableGridDashedLine(10f, 5f, 0f)
        //add extra space over the maximum bar
        yAxis.spaceTop = 30f
        //x-axis
        val xAxis = barChart.xAxis
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -30f
        xAxis.textColor = Utils.getColor(requireContext(), R.color.grey_300)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.enableGridDashedLine(10f, 5f, 0f)
        //disable dual y-axes
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
    }

    private fun setupBarData(): BarData {

        if (barChart.data != null)
            return barChart.data
        //x-axis value formatter
        barChart.xAxis.valueFormatter = DateValueFormatter(area.timelines!!)

        val entries = mutableListOf<BarEntry>()
        //list.indices: returns an [IntRange] of the valid indices for this collection
        for (count in area.timelines!!.indices) {
            val day = area.timelines!![count]

            entries.add(
                BarEntry(
                    count.toFloat(),
                    day.dailyCases.confirmed.toFloat(),
                    MarkerData(
                        day.relativeDate,
                        day.dailyCases.formattedConfirmed,
                        R.color.yellow_A700
                    )
                )
            )
        }

        val barDataSet = BarDataSet(entries, "")
        //don't draw values on bars
        barDataSet.setDrawValues(false)
        barDataSet.setDrawIcons(false)
        //bar colors
        barDataSet.colors = mutableListOf(Utils.getColor(requireContext(), R.color.yellow_A700))
        barDataSet.highLightColor = Utils.getColor(requireContext(), R.color.grey_100)
        return BarData(barDataSet)
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.setNoDataTextColor(Utils.getColor(requireContext(), R.color.grey_300))
        //padding for left/top/right & bottom of the chart
        pieChart.setExtraOffsets(10f, 10f, 10f, 10f)
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
        pieChart.setEntryLabelColor(Utils.getColor(requireContext(), R.color.grey_300))
        pieChart.setEntryLabelTextSize(9f)
        pieChart.setDrawEntryLabels(false)
    }

    private fun setupPieData(): PieData {

        if (pieChart.data != null)
            return pieChart.data
        //active cases = confirmed cases - (deaths + recovered)
        val activeCases = area.confirmed - (area.deaths + area.recovered)
        val entries = listOf(
            PieEntry(
                activeCases.toFloat(),
                "Active",
                Utils.getColor(requireContext(), R.color.yellow_A700)
            ),
            PieEntry(
                area.deaths.toFloat(),
                "Deaths",
                Utils.getColor(requireContext(), R.color.red_A700)
            ),
            PieEntry(
                area.recovered.toFloat(),
                "Recovered",
                Utils.getColor(requireContext(), R.color.green_A700)
            )
            // filter zero entries
            // do not display entries with no values
        ).filter { it.y > 0 }

        //create a pie data set with mutable list of pie entries
        val dataset = PieDataSet(entries, "")
        dataset.setAutomaticallyDisableSliceSpacing(true)
        dataset.sliceSpace = 3f
        dataset.selectionShift = 5f
        //set colors for each slice of pie chart
        dataset.colors = entries.map { it.data as Int }

        //line over the chart
        dataset.valueLinePart1OffsetPercentage = 80f
        dataset.valueLineColor = Utils.getColor(requireContext(), R.color.grey_300)
        dataset.valueLinePart1Length = 0.8f
        dataset.valueLinePart2Length = 0.3f
        dataset.isUsingSliceColorAsValueLineColor = true
        dataset.valueTextColor = Utils.getColor(requireContext(), R.color.grey_300)
        //display values outside of the chart
        dataset.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataset.valueTextSize = 10f

        val pieData = PieData(dataset)
        pieData.setValueFormatter(PercentFormatter(pieChart))
        return pieData
    }

    private suspend fun getArea() {

        val api = NovelApiImp.getApi()
        val timeline =
            if (area.id != -1) api.getTimelinesByCountry(area.id.toString()).timelines
            else api.getWorldTimeline()
        mapToUiModel(timeline)
        setupLineData()
        setupBarData()
    }

    private fun displayApiError() {
        Toast.makeText(activity, "Historical data are not available", Toast.LENGTH_LONG)
            .show()
    }

    private fun mapToUiModel(it: Timelines): AreaModel {
        val timelines: MutableList<TimelineData> = mutableListOf()

        for ((index, timeline) in it.confirmed.toList().withIndex()) {
            //gather information about area since first case confirmed
            if (timeline.second <= 0)
                continue

            val confirmed = timeline.second
            val deaths = it.deaths[timeline.first] ?: 0
            val recovered = it.recovered[timeline.first] ?: 0
            val localDate = Utils.toLocalDate(timeline.first)

            val dailyConfirmed =
                confirmed - it.confirmed.toList().getOrElse(index - 1) { Pair("", 0) }.second
            val dailyDeaths =
                deaths - it.deaths.toList().getOrElse(index - 1) { Pair("", 0) }.second
            val dailyRecovered =
                recovered - it.recovered.toList().getOrElse(index - 1) { Pair("", 0) }.second


            val latestCases = Cases(confirmed, deaths, recovered)
            val dailyCases = Cases(dailyConfirmed, dailyDeaths, dailyRecovered)

            timelines.add(TimelineData(latestCases, dailyCases, localDate))
        }
        area.timelines = timelines
        return area
    }

    private fun setupLineChart() {
        lineChart.setNoDataTextColor(Utils.getColor(requireContext(), R.color.grey_300))
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isAutoScaleMinMaxEnabled = true
        lineChart.isScaleYEnabled = false
        //no grid background
        lineChart.setDrawGridBackground(false)
        //custom marker
        val markerView = MarkerView(requireContext(), R.layout.chart_marker_view)
        markerView.chartView = lineChart
        lineChart.marker = markerView

        setupLineAxis()
        setupLineLegend()
    }

    private fun setupLineAxis() {
        // x-axis style
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -30f
        // vertical grid lines
        xAxis.enableGridDashedLine(10f, 5f, 0f)
        xAxis.textColor = Utils.getColor(requireContext(), R.color.grey_300)
        //y-axis style
        val yAxis = lineChart.axisLeft
        // disable dual y-axis
        lineChart.axisRight.isEnabled = false
        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 5f, 0f)
        yAxis.spaceTop = 35f
        yAxis.textColor = Utils.getColor(requireContext(), R.color.grey_300)
    }

    private fun setupLineLegend() {
        val legend: Legend = lineChart.legend
        // draw legend entries as lines
        legend.form = Legend.LegendForm.CIRCLE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.textSize = 10f
        legend.formSize = 12f
        legend.textColor = Utils.getColor(requireContext(), R.color.grey_300)
    }

    private fun setupLineData(): LineData {

        if (lineChart.data != null)
            return lineChart.data
        //setup x-axis value formatter: display dates (Mar 13)
        lineChart.xAxis.valueFormatter = DateValueFormatter(area.timelines!!)
        //setup y-axis value formatter: display values in short compact format (12.5 K)
        lineChart.axisLeft.valueFormatter = CompactDigitValueFormatter()

        val confirmedEntries = arrayListOf<Entry>()
        val deathEntries = arrayListOf<Entry>()
        val recoveredEntries = arrayListOf<Entry>()

        for ((count, value) in area.timelines!!.withIndex()) {
            val confirmedEntry = Entry(
                count.toFloat(),
                value.latestCases.confirmed.toFloat(),
                MarkerData(
                    value.relativeDate,
                    value.latestCases.formattedConfirmed,
                    R.color.yellow_A700
                )
            )
            val deathsEntry = Entry(
                count.toFloat(),
                value.latestCases.deaths.toFloat(),
                MarkerData(
                    value.relativeDate,
                    value.latestCases.formattedDeaths,
                    R.color.red_A700
                )
            )
            val recoveredEntry = Entry(
                count.toFloat(),
                value.latestCases.recovered.toFloat(),
                MarkerData(
                    value.relativeDate,
                    value.latestCases.formattedRecovered,
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

        return LineData(arrayListOf(confirmedSet, deathSet, recoveredSet) as List<ILineDataSet>)
    }

    private fun setupLineData(entries: List<Entry>, text: String, color: Int): LineDataSet {

        val dataset = LineDataSet(entries, text)
        dataset.mode = LineDataSet.Mode.CUBIC_BEZIER
        //line & circle point colors
        dataset.color = Utils.getColor(requireContext(), color)
        dataset.setCircleColor(Utils.getColor(requireContext(), color))
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
