package com.itsamirrezah.covid19.data.api

import com.itsamirrezah.covid19.data.models.AllCasesResponse
import io.reactivex.Observable
import retrofit2.http.GET

interface CovidApi {

    @GET("/all")
    fun getAllCases(): Observable<AllCasesResponse>
}