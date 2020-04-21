package com.itsamirrezah.covid19.ui.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.itsamirrezah.covid19.util.Utils
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Parcelize
data class AreaModel(
    val id: Int,
    val latLng: LatLng?,
    val country: String,
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
        id: Int,
        confirmed: Long,
        deaths: Long,
        recovered: Long,
        country: String
    ) : this(id, null, country, "", confirmed, deaths, recovered)

    override fun getSnippet(): String {
        return province
    }

    override fun getTitle(): String {
        return country
    }

    override fun getPosition(): LatLng {
        return latLng!!
    }
}