package com.itsamirrezah.covid19.data.novelapi.model

import com.google.gson.annotations.SerializedName

class CountriesResponse(
    @SerializedName("countryInfo")
    val countryInfo: CountryInfo,
    @SerializedName("updated")
    val updatedAt: Long,
    @SerializedName("country")
    val countryName: String,
    @SerializedName("cases")
    val confirmedCases: Long,
    @SerializedName("todayCases")
    val todayCases: Long,
    @SerializedName("deaths")
    val deaths: Long,
    @SerializedName("todayDeaths")
    val todayDeaths: Long,
    @SerializedName("recovered")
    val recovered: Long,
    @SerializedName("active")
    val activeCases: Long,
    @SerializedName("critical")
    val criticalCases: Long,
    @SerializedName("casesPerOneMillion")
    val casesPerOneMillion: Long,
    @SerializedName("deathsPerOneMillion")
    val deathsPerOneMillion: Long,
    @SerializedName("testsPerOneMillion")
    val testsPerOneMillion: Long,
    @SerializedName("continent")
    val continent: String
)

data class CountryInfo(
    @SerializedName("_id")
    val id: Int,
    @SerializedName("iso2")
    val shortCode: String,
    @SerializedName("iso3")
    val longCode: String,
    @SerializedName("lat")
    val lat: Float,
    @SerializedName("long")
    val lon: Float,
    @SerializedName("flag")
    val flag: String
)