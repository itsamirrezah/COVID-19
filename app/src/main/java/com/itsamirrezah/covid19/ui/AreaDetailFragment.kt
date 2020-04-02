package com.itsamirrezah.covid19.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
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
import com.itsamirrezah.covid19.util.CompactDigitValueFormatter
import com.itsamirrezah.covid19.util.DateValueFormatter
import com.itsamirrezah.covid19.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate

class AreaDetailFragment : BottomSheetDialogFragment() {

    private lateinit var areaCaseModel: AreaCasesModel
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart

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

        setupLineChart(view)
        arguments?.let {
            areaCaseModel = it.getParcelable("AREA_CASE_MODEL_EXTRA")!!
            view.findViewById<TextView>(R.id.tvCountry).text = areaCaseModel.country
            view.findViewById<TextView>(R.id.tvConfirmed).text = areaCaseModel.latestConfirmed
            view.findViewById<TextView>(R.id.tvDeaths).text = areaCaseModel.latestDeaths
            if (areaCaseModel.latestRecovered.toInt() > 0)
                view.findViewById<TextView>(R.id.tvRecovered).text = areaCaseModel.latestRecovered

            getAreaCases()
            setupPieChart(view)
        }
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
            areaCaseModel.latestConfirmed.toLong() - (areaCaseModel.latestDeaths.toLong() + areaCaseModel.latestRecovered.toLong())

        val entries = mutableListOf(
            PieEntry(activeCases.toFloat(), "Active"),
            PieEntry(areaCaseModel.latestDeaths.toFloat(), "Deaths"),
            PieEntry(areaCaseModel.latestRecovered.toFloat(), "Recovered")
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
                val confirmedTimeline = mutableListOf<Pair<LocalDate, Int>>()
                val deathTimeline = mutableListOf<Pair<LocalDate, Int>>()
                val recoveredTimeline = mutableListOf<Pair<LocalDate, Int>>()

                for (timeline in it.area.timelines.confirmed.timeline) {
                    //gather information about area since first case confirmed
                    if (timeline.value <= 0)
                        continue
                    confirmedTimeline.add(
                        Pair(Utils.toLocalDate(timeline.key), timeline.value)
                    )
                }

                for (timeline in it.area.timelines.deaths.timeline) {
                    val date = Utils.toLocalDate(timeline.key)

                    if (date.isBefore(confirmedTimeline.first().first))
                        continue
                    deathTimeline.add(
                        Pair(Utils.toLocalDate(timeline.key), timeline.value)
                    )
                }

                for (timeline in it.area.timelines.recovered.timeline) {
                    val date = Utils.toLocalDate(timeline.key)
                    if (date.isBefore(confirmedTimeline.first().first))
                        continue
                    recoveredTimeline.add(
                        Pair(Utils.toLocalDate(timeline.key), timeline.value)
                    )
                }

                areaCaseModel.confirmedHistory = confirmedTimeline
                areaCaseModel.deathHistory = deathTimeline
                areaCaseModel.recoveredHistory = recoveredTimeline

                areaCaseModel
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setupLineData()
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
        //no grid background
        lineChart.setDrawGridBackground(false)

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

        for ((count, value) in areaCaseModel.confirmedHistory.withIndex()) {
            val entry = Entry(count.toFloat(), value.second.toFloat())
            confirmedEntries.add(entry)
        }

        for ((count, value) in areaCaseModel.deathHistory.withIndex()) {
            val entry = Entry(count.toFloat(), value.second.toFloat())
            deathEntries.add(entry)
        }

        for ((count, value) in areaCaseModel.recoveredHistory.withIndex()) {
            val entry = Entry(count.toFloat(), value.second.toFloat())
            recoveredEntries.add(entry)
        }

        val confirmedSet: LineDataSet =
            setupLineData(confirmedEntries, "confirmed", R.color.yellow_700)
        val deathSet: LineDataSet = setupLineData(deathEntries, "deaths", R.color.red_A700)
        val recoveredSet: LineDataSet =
            setupLineData(recoveredEntries, "recovered", R.color.green_400)


        lineChart.data = LineData(
            arrayListOf(
                confirmedSet, deathSet, recoveredSet
            ) as List<ILineDataSet>
        )

        // draw points over time
        lineChart.animateX(500)
        //setup x-axis value formatter: display dates (Mar 13)
        lineChart.xAxis.valueFormatter = DateValueFormatter(
            areaCaseModel.confirmedHistory.last().first,
            areaCaseModel.confirmedHistory.size
        )

        //setup y-axis value formatter: display values in short compact format (12.5 K)
        lineChart.axisLeft.valueFormatter = CompactDigitValueFormatter()
    }

    private fun setupLineData(entries: List<Entry>, text: String, color: Int): LineDataSet {

        val dataset = LineDataSet(entries, text)
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
