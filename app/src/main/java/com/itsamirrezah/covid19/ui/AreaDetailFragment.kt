package com.itsamirrezah.covid19.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
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
                it
                //do something
            }, {
                print(it.message)
            })
    }

}
