package com.itsamirrezah.covid19.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.ui.model.AreaCasesModel

class ClusterItemWindowInfo(
    context: Context
) : GoogleMap.InfoWindowAdapter {

    private var rootView: View =
        LayoutInflater.from(context).inflate(R.layout.cluster_item_window_info, null)
    private var tvConfirmed: TextView = rootView.findViewById(R.id.tvConfirmed)
    private var tvDeaths: TextView = rootView.findViewById(R.id.tvDeaths)
    private var tvRecovered: TextView = rootView.findViewById(R.id.tvRecovered)
    private var tvCountry: TextView = rootView.findViewById(R.id.tvCountry)
    private var tvProvince: TextView = rootView.findViewById(R.id.tvProvince)

    override fun getInfoWindow(marker: Marker?): View {
        val data = marker!!.tag as AreaCasesModel

        tvCountry.text = data.country
        if (data.province != "") {
            tvProvince.text = data.province
            tvProvince.visibility = View.VISIBLE
        } else
            tvProvince.visibility = View.GONE
        tvConfirmed.text = data.latestConfirmed
        tvDeaths.text = data.latestDeaths
        if (data.latestRecovered.toInt() != 0)
            tvRecovered.text = data.latestRecovered
        return rootView
    }


    override fun getInfoContents(marker: Marker?): View? {
        return null
    }

}