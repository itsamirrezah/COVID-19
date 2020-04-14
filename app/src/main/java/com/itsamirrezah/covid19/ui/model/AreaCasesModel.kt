package com.itsamirrezah.covid19.ui.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.itsamirrezah.covid19.util.Utils
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
    var confirmed: Long,
    var deaths: Long,
    var recovered: Long,
    //timeline & daily data items format: (Date,(confirmed,death,recovered))
    var timelines: List<Pair<LocalDate, Triple<Int, Int, Int>>>? = null,
    var dailyTimelines: List<Pair<LocalDate, Triple<Int, Int, Int>>>? = null
) : ClusterItem, Parcelable {

    val confirmedString = Utils.toNumberSeparator(confirmed)
    val deathString = Utils.toNumberSeparator(deaths)
    val recoveredString = Utils.toNumberSeparator(recovered)

    constructor(
        confirmed: Long,
        deaths: Long,
        recovered: Long,
        timelines: List<Pair<LocalDate, Triple<Int, Int, Int>>>,
        dailyTimelines: List<Pair<LocalDate, Triple<Int, Int, Int>>>,
        country: String = "Wordwide"
    ) : this(0, .0, .0, country, "", "", confirmed, deaths, recovered, timelines, dailyTimelines)

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