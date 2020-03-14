package com.itsamirrezah.covid19.ui.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.internal.LinkedTreeMap
import com.google.maps.android.clustering.ClusterItem

data class AreaCasesModel(
    val lat: Double,
    val lon: Double,
    val country: String,
    val countryCode: String,
    val province: String,
    val deathsHistory: LinkedTreeMap<String, String>,
    val confirmedHistory: LinkedTreeMap<String, String>,
    val recoverHistory: LinkedTreeMap<String, String>
) : ClusterItem {
    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return "confirmed: " + confirmedHistory.values.sumBy { it.toInt() }
    }

    override fun getPosition(): LatLng {
        return LatLng(lat, lon)
    }
}