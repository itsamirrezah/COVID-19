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
    var timelines: List<TimelineData>? = null
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

@Parcelize
data class Cases(
    val confirmed: Int,
    val deaths: Int,
    val recovered: Int
): Parcelable {
    val formattedConfirmed: String
        get() = Utils.toNumberSeparator(confirmed.toLong())
    val formattedDeaths: String
        get() = Utils.toNumberSeparator(deaths.toLong())
    val formattedRecovered: String
        get() = Utils.toNumberSeparator(recovered.toLong())
}

@Parcelize
data class TimelineData(
    val latestCases: Cases,
    val dailyCases: Cases,
    val localDate: LocalDate
): Parcelable{
    val relativeDate: String
        get() = Utils.shortRelativeDate(localDate)
}
