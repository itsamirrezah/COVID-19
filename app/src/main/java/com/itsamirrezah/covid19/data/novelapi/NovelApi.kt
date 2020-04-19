package com.itsamirrezah.covid19.data.novelapi
import com.itsamirrezah.covid19.data.novelapi.model.CountriesResponse
import io.reactivex.Observable
import retrofit2.http.GET

interface NovelApi {

    @GET("/v2/countries")
    fun getAllCases(): Observable<List<CountriesResponse>>

//    @GET("/v2/locations/{id}")
//    fun getAreaById(@Path("id") id: Int): Observable<AreaResponse>
}