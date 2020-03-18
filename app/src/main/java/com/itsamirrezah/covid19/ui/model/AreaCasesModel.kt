package com.itsamirrezah.covid19.ui.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class AreaCasesModel(
    val id: Int,
    val lat: Double,
    val lon: Double,
    val country: String,
    val countryCode: String,
    val province: String,
    val latestConfirmed: String,
    val latestDeaths: String,
    val latestRecovered: String

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