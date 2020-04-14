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
    val confirmed: Long,
    val deaths: Long,
    val recovered: Long

) : ClusterItem, Parcelable {

    val confirmedString = Utils.toNumberSeparator(confirmed)
    val deathString = Utils.toNumberSeparator(deaths)
    val recoveredString = Utils.toNumberSeparator(recovered)
    //timeline & daily data items format: (Date,(confirmed,death,recovered))
    lateinit var timelines: List<Pair<LocalDate, Triple<Int, Int, Int>>>
    lateinit var dailyTimelines: List<Pair<LocalDate, Triple<Int, Int, Int>>>

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