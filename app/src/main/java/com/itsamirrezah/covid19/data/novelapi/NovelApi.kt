package com.itsamirrezah.covid19.data.novelapi
import com.itsamirrezah.covid19.data.novelapi.model.CountriesResponse
import com.itsamirrezah.covid19.data.novelapi.model.CountryTimlineResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface NovelApi {

    @GET("/v2/countries")
    fun getAllCases(): Observable<List<CountriesResponse>>

    @GET("/v2/historical/{id}?lastdays=all")
    fun getTimelinesByCountry(@Path("id") id: Int): Observable<CountryTimlineResponse>
}