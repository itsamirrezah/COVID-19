package com.itsamirrezah.covid19.data.models

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import java.sql.Time


data class AllCasesResponse(
    @SerializedName("locations")
    val areas: List<Area>
)

data class Latest(
    @SerializedName("confirmed")
    val confirmed: Long,
    @SerializedName("deaths")
    val deaths: Long,
    @SerializedName("recovered")
    val recovered: Long
)

data class Area(
    @SerializedName("id")
    val id: Int,
    @SerializedName("coordinates")
    val coordinates: Coordinates,
    @SerializedName("country")
    val country: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("timelines")
    val timelines: Timelines,
    @SerializedName("latest")
    val latest: Latest,
    @SerializedName("province")
    val province: String
)

data class Coordinates(
    @SerializedName("latitude")
    val lat: String,
    @SerializedName("longitude")
    val lon: String
)

data class Timelines(
    @SerializedName("confirmed")
    val confirmed: Timeline,
    @SerializedName("deaths")
    val deaths: Timeline,
    @SerializedName("recovered")
    val recovered: Timeline
)

data class Timeline(
    @SerializedName("latest")
    val latest: Long,
    @SerializedName("timeline")
    val timeline: LinkedTreeMap<Time, Int>
)