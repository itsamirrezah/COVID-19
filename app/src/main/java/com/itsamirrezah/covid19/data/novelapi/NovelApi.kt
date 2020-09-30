package com.itsamirrezah.covid19.data.novelapi
import com.itsamirrezah.covid19.data.novelapi.model.CasesResponse
import com.itsamirrezah.covid19.data.novelapi.model.CountryTimlineResponse
import com.itsamirrezah.covid19.data.novelapi.model.Timelines
import retrofit2.http.GET
import retrofit2.http.Path

interface NovelApi {

    @GET("/v2/countries")
    suspend fun getAllCases(): List<CasesResponse>

    @GET("/v2/historical/{id}?lastdays=all")
    suspend fun getTimelinesByCountry(@Path("id") id: String): CountryTimlineResponse

    @GET("/v2/historical/all?lastdays=all")
    suspend fun getWorldTimeline(): Timelines

    @GET("/v2/all")
    suspend fun getWorldCases(): CasesResponse

}