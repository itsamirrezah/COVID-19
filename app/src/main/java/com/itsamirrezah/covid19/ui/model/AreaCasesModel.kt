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
    val recoverHistory: LinkedTreeMap<String, String>,
    val latestConfirmed: Long,
    val latestDeaths: Long,
    val latestRecovered: Long

) : ClusterItem {
    override fun getSnippet(): String {
        return province
    }

    override fun getTitle(): String {
        return country
    }

    override fun getPosition(): LatLng {
        return LatLng(lat, lon)
    }
}