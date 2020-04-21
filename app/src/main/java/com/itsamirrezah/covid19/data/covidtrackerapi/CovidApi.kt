package com.itsamirrezah.covid19.data.covidtrackerapi

import com.itsamirrezah.covid19.data.covidtrackerapi.models.AllCasesResponse
import com.itsamirrezah.covid19.data.covidtrackerapi.models.AreaResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CovidApi {

    @GET("/v2/locations")
    fun getAllCases(
        @Query("timelines") timelines: Boolean?
    ): Observable<AllCasesResponse>

    @GET("/v2/locations/{id}")
    fun getAreaById(@Path("id") id: Int): Observable<AreaResponse>
}