package com.itsamirrezah.covid19.data.novelapi
import com.itsamirrezah.covid19.data.novelapi.model.CasesResponse
import com.itsamirrezah.covid19.data.novelapi.model.CountryTimlineResponse
import com.itsamirrezah.covid19.data.novelapi.model.Timelines
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface NovelApi {

    @GET("/v2/countries")
    fun getAllCases(): Observable<List<CasesResponse>>

    @GET("/v2/historical/{id}?lastdays=all")
    fun getTimelinesByCountry(@Path("id") id: String): Observable<CountryTimlineResponse>

    @GET("/v2/historical/all?lastdays=all")
    fun getWorldTimeline(): Observable<Timelines>

    @GET("/v2/all")
    fun getAllWorld(): Observable<CasesResponse>

}