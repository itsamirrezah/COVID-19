package com.itsamirrezah.covid19.data.api

import com.itsamirrezah.covid19.data.models.AllCasesResponse
import io.reactivex.Observable
import retrofit2.http.GET

interface CovidApi {

    @GET("/v2/locations")
    fun getAllCases(): Observable<AllCasesResponse>
}