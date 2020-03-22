package com.itsamirrezah.covid19.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.data.api.CovidApiImp
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import com.itsamirrezah.covid19.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate


/**
 * A simple [Fragment] subclass.
 */
class AreaDetailFragment : BottomSheetDialogFragment() {

    private lateinit var areaCaseModel: AreaCasesModel
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_area_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLineChart(view)
        arguments?.let {
            areaCaseModel = it.getParcelable("AREA_CASE_MODEL_EXTRA")!!
            view.findViewById<TextView>(R.id.tvCountry).text = areaCaseModel.country
            view.findViewById<TextView>(R.id.tvConfirmed).text = areaCaseModel.latestConfirmed
            view.findViewById<TextView>(R.id.tvRecovered).text = areaCaseModel.latestRecovered
            view.findViewById<TextView>(R.id.tvDeaths).text = areaCaseModel.latestDeaths
            getAreaCases()
        }
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
                setupDataset()
            }, {
                print(it.message)
            })
    }


    private fun setupLineChart(view: View) {
        lineChart = view.findViewById(R.id.lineChart)
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(false)
        lineChart.setDrawGridBackground(false)

        setupAxis()
        setupLegend()

    }

    private fun setupAxis() {
        // x-axis style
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        // vertical grid lines
        xAxis.enableGridDashedLine(10f, 5f, 0f)

        //y-axis style
        val yAxis = lineChart.axisLeft
        // disable dual y-axis
        lineChart.axisRight.isEnabled = false

        // horizontal grid lines
        yAxis.enableGridDashedLine(10f, 5f, 0f)
    }

    private fun setupLegend() {
        val legend: Legend = lineChart.legend
        // draw legend entries as lines
        legend.form = Legend.LegendForm.CIRCLE
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
    }

    private fun setupDataset() {

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
            setupDataset(confirmedEntries, "confirmed", R.color.yellow_900)
        val deathSet: LineDataSet = setupDataset(deathEntries, "deaths", R.color.red_900)
        val recoveredSet: LineDataSet =
            setupDataset(recoveredEntries, "recovered", R.color.green_400)


        lineChart.data = LineData(
            arrayListOf(
                confirmedSet, deathSet, recoveredSet
            ) as List<ILineDataSet>
        )

        // draw points over time
        lineChart.animateX(500)
    }

    private fun setupDataset(entries: List<Entry>, text: String, color: Int): LineDataSet {
        val dataset = LineDataSet(entries, text)

        //line & circle point colors
        dataset.color = ContextCompat.getColor(context!!, color)
        dataset.setCircleColor(ContextCompat.getColor(context!!, color))

        //line thickness and point size
        dataset.lineWidth = 3f
        dataset.circleRadius = 3.5f

        // draw line
        dataset.enableDashedLine(10f, 0f, 0f)

        dataset.setDrawIcons(false)
        dataset.setDrawCircleHole(true)
        dataset.setDrawValues(false)

        return dataset
    }
}
