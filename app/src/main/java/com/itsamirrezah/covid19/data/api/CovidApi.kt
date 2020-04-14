package com.itsamirrezah.covid19.data.api

import com.itsamirrezah.covid19.data.models.AllCasesResponse
import com.itsamirrezah.covid19.data.models.AreaResponse
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