package com.itsamirrezah.covid19.data.api

import com.itsamirrezah.covid19.data.models.AllCasesResponse
import com.itsamirrezah.covid19.data.models.AreaResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface CovidApi {

    @GET("/v2/locations")
    fun getAllCases(): Observable<AllCasesResponse>

    @GET("/v2/locations/{id}")
    fun getAreaById(@Path("id") id: Int): Observable<AreaResponse>
}