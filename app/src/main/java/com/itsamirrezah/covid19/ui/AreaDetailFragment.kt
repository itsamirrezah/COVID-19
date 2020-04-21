package com.itsamirrezah.covid19.ui

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
import com.itsamirrezah.covid19.ui.model.MarkerData
import com.itsamirrezah.covid19.util.Utils
import com.itsamirrezah.covid19.util.chart.CompactDigitValueFormatter
import com.itsamirrezah.covid19.util.chart.DateValueFormatter
import com.itsamirrezah.covid19.util.chart.MarkerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate
import retrofit2.HttpException

class AreaDetailFragment : BottomSheetDialogFragment() {

    private lateinit var area: AreaModel
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    private var handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_area_detail, container, false)
        setupPieChart(view)
        setupLineChart(view)
        setupBarChart(view)
        return view
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
        }

        return bottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            area = it.getParcelable("AREA_CASE_MODEL_EXTRA")!!
            view.findViewById<TextView>(R.id.tvCountry).text = area.country
            view.findViewById<TextView>(R.id.tvConfirmed).text = area.confirmedString
            view.findViewById<TextView>(R.id.tvDeaths).text = area.deathString
            view.findViewById<TextView>(R.id.tvRecovered).text = area.recoveredString

            setupPieData()
            if (area.timelines == null) {
                requestTimeline()
            } else {
                setupLineData()
                setupBarData()
            }

        }
    }

    private fun requestTimeline() {
        if (area.id < 0) getWorld()
        else getArea()
    }

    private fun setupBarChart(view: View) {
        barChart = view.findViewById(R.id.barChart)
        barChart.setNoDataTextColor(Utils.getColor(context!!, R.color.grey_300))
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawValueAboveBar(false)
        barChart.isHighlightFullBarEnabled = true
        barChart.isAutoScaleMinMaxEnabled = true
        //disable y-axis scale on zoom
        barChart.isScaleYEnabled = false
        //custom marker
        val markerView =
            MarkerView(context!!, R.layout.chart_marker_view)
        markerView.chartView = barChart
        barChart.marker = markerView
        //y-axis
        val yAxis = barChart.axisLeft
        //y-axis value formatter
        yAxis.valueFormatter = CompactDigitValueFormatter()
        yAxis.textColor = Utils.getColor(context!!, R.color.grey_300)
        yAxis.axisMinimum = 0f
        yAxis.enableGridDashedLine(10f, 5f, 0f)
        //add extra space over the maximum bar
        yAxis.spaceTop = 30f
        //x-axis
        val xAxis = barChart.xAxis
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -30f
        xAxis.textColor = Utils.getColor(context!!, R.color.grey_300)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.enableGridDashedLine(10f, 5f, 0f)
        //disable dual y-axes
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
    }

    private fun setupBarData() {
        //x-axis value formatter
        barChart.xAxis.valueFormatter = DateValueFormatter(
            area.timelines!!.last().first,
            area.timelines!!.size
        )

        val entries = mutableListOf<BarEntry>()
        //list.indices: returns an [IntRange] of the valid indices for this collection
        for (count in area.dailyTimelines!!.indices) {
            val day = area.dailyTimelines!![count]

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
            Utils.getColor(context!!, R.color.yellow_A700), //confirmed
            Utils.getColor(context!!, R.color.red_A700), //deaths
            Utils.getColor(context!!, R.color.green_A700) //recovered
        )
        barDataSet.highLightColor = Utils.getColor(context!!, R.color.grey_100)

        handler.postDelayed({
            barChart.data = BarData(barDataSet)
            barChart.animateY(800)

        }, 800)
    }

    private fun setupPieChart(view: View) {
        pieChart = view.findViewById(R.id.pieChart)
        pieChart.description.isEnabled = false
        pieChart.setNoDataTextColor(Utils.getColor(context!!, R.color.grey_300))
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
        pieChart.setEntryLabelColor(Utils.getColor(context!!, R.color.grey_300))
        pieChart.setEntryLabelTextSize(9f)
        pieChart.animateY(800, Easing.EaseInOutQuad)
        pieChart.setDrawEntryLabels(false)
    }

    private fun setupPieData() {
        //active cases = confirmed cases - (deaths + recovered)
        val activeCases =
            area.confirmed - (area.deaths + area.recovered)
        val entries = mutableListOf(
            PieEntry(
                activeCases.toFloat(),
                "Active",
                Utils.getColor(context!!, R.color.yellow_A700)
            ),
            PieEntry(
                area.deaths.toFloat(),
                "Deaths",
                Utils.getColor(context!!, R.color.red_A700)
            ),
            PieEntry(
                area.recovered.toFloat(),
                "Recovered",
                Utils.getColor(context!!, R.color.green_A700)
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
        dataset.valueLineColor = Utils.getColor(context!!, R.color.grey_300)
        dataset.valueLinePart1Length = 0.8f
        dataset.valueLinePart2Length = 0.3f
        dataset.isUsingSliceColorAsValueLineColor = true
        dataset.valueTextColor = Utils.getColor(context!!, R.color.grey_300)
        //display values outside of the chart
        dataset.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataset.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataset.valueTextSize = 10f

        val data = PieData(dataset)
        data.setValueFormatter(PercentFormatter(pieChart))
        handler.postDelayed({
            pieChart.data = data
        }, 200)
    }

    private fun getArea() {
        val requestArea = NovelApiImp.getApi()
            .getTimelinesByCountry(area.id.toString())
            .map { it.timelines }
            .map { mapToUiModel(it) }

        subscribe(requestArea)
    }

    private fun subscribe(observable: Observable<AreaModel>): Disposable? {
        return observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setupLineData()
                setupBarData()
            }, {
                //error
                if (it is HttpException)
                    displayApiError()
            })
    }

    private fun displayApiError() {
        Toast.makeText(activity, "Historical data are not available", Toast.LENGTH_LONG)
            .show()
    }

    private fun getWorld() {
        val world = NovelApiImp.getApi()
            .getWorldTimeline()
            .map { mapToUiModel(it) }

        subscribe(world)
    }

    private fun mapToUiModel(it: Timelines): AreaModel {
        val timelines: MutableList<Pair<LocalDate, Triple<Int, Int, Int>>> = mutableListOf()
        val dailyTimeline: MutableList<Pair<LocalDate, Triple<Int, Int, Int>>> =
            mutableListOf()

        for ((index, timeline) in it.confirmed.toList().withIndex()) {
            //gather information about area since first case confirmed
            if (timeline.second <= 0)
                continue

            val confirmed = timeline.second
            val deaths = it.deaths[timeline.first] ?: 0
            val recovered = it.recovered[timeline.first] ?: 0
            val localDate = Utils.toLocalDate(timeline.first)

            timelines.add(
                Pair(
                    localDate, Triple(confirmed!!, deaths, recovered)
                )
            )

            val dailyConfirmed =
                confirmed - it.confirmed.toList()
                    .getOrElse(index - 1) { Pair("", 0) }.second
            val dailyDeaths =
                deaths - it.deaths.toList()
                    .getOrElse(index - 1) { Pair("", 0) }.second
            val dailyRecovered =
                recovered - it.recovered.toList()
                    .getOrElse(index - 1) { Pair("", 0) }.second

            dailyTimeline.add(
                Pair(
                    localDate, Triple(dailyConfirmed, dailyDeaths, dailyRecovered)
                )
            )
        }

        area.timelines = timelines
        area.dailyTimelines = dailyTimeline
        return area
    }

    private fun setupLineChart(view: View) {
        lineChart = view.findViewById(R.id.lineChart)
        lineChart.setNoDataTextColor(Utils.getColor(context!!, R.color.grey_300))
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isAutoScaleMinMaxEnabled = true
        lineChart.isScaleYEnabled = false
        //no grid background
        lineChart.setDrawGridBackground(false)
        //custom marker
        val markerView = MarkerView(context!!, R.layout.chart_marker_view)
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
        xAxis.textColor = Utils.getColor(context!!, R.color.grey_300)
        //y-axis style
        val yAxis = lineChart.axisLeft
        // disable dual y-axis
        lineChart.axisRight.isEnabled = false
        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 5f, 0f)
        yAxis.spaceTop = 35f
        yAxis.textColor = Utils.getColor(context!!, R.color.grey_300)
    }

    private fun setupLineLegend() {
        val legend: Legend = lineChart.legend
        // draw legend entries as lines
        legend.form = Legend.LegendForm.CIRCLE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.textSize = 10f
        legend.formSize = 12f
        legend.textColor = Utils.getColor(context!!, R.color.grey_300)
    }

    private fun setupLineData() {

        //setup x-axis value formatter: display dates (Mar 13)
        lineChart.xAxis.valueFormatter = DateValueFormatter(
            area.timelines!!.last().first,
            area.timelines!!.size
        )
        //setup y-axis value formatter: display values in short compact format (12.5 K)
        lineChart.axisLeft.valueFormatter =
            CompactDigitValueFormatter()

        val confirmedEntries = arrayListOf<Entry>()
        val deathEntries = arrayListOf<Entry>()
        val recoveredEntries = arrayListOf<Entry>()

        for ((count, value) in area.timelines!!.withIndex()) {
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

        handler.postDelayed({
            lineChart.data = LineData(
                arrayListOf(
                    confirmedSet, deathSet, recoveredSet
                ) as List<ILineDataSet>
            )
            // draw points over time
            lineChart.animateX(800)
        }, 800)
    }

    private fun setupLineData(entries: List<Entry>, text: String, color: Int): LineDataSet {

        val dataset = LineDataSet(entries, text)
        dataset.mode = LineDataSet.Mode.CUBIC_BEZIER
        //line & circle point colors
        dataset.color = Utils.getColor(context!!, color)
        dataset.setCircleColor(Utils.getColor(context!!, color))
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
