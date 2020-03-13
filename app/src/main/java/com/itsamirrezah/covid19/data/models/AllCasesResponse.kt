package com.itsamirrezah.covid19.data.models

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap


data class AllCasesResponse(
    @SerializedName("latest")
    val latest: Latest,
    @SerializedName("confirmed")
    val confirmed: Cases,
    @SerializedName("deaths")
    val deaths: Cases,
    @SerializedName("recovered")
    val recovered: Cases
)

data class Latest(
    @SerializedName("confirmed")
    val confirmed: Long,
    @SerializedName("deaths")
    val deaths: Long,
    @SerializedName("recovered")
    val recovered: Long

)

data class Cases(
    @SerializedName("last_updated")
    val lastUpdated: String,
    @SerializedName("latest")
    val latest: Long,
    @SerializedName("locations")
    val locations: List<Area>
)

data class Area(
    @SerializedName("coordinates")
    val coordinates: Coordinates,
    @SerializedName("country")
    val country: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("history")
    val history: LinkedTreeMap<String, String>,
    @SerializedName("Latest")
    val latest: Int,
    @SerializedName("province")
    val province: String
)

data class Coordinates(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("long")
    val long: Double
)