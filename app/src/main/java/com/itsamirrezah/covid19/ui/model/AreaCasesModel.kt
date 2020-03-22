package com.itsamirrezah.covid19.ui.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Parcelize
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

) : ClusterItem, Parcelable {

    lateinit var confirmedHistory: List<Pair<LocalDate, Int>>
    lateinit var deathHistory: List<Pair<LocalDate, Int>>
    lateinit var recoveredHistory: List<Pair<LocalDate, Int>>

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