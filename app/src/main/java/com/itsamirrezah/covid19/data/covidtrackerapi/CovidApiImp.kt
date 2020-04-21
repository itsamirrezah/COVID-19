package com.itsamirrezah.covid19.data.covidtrackerapi

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class CovidApiImp {

    companion object {

        fun getApi(): CovidApi {

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClient = OkHttpClient()
                .newBuilder()
                .addInterceptor(loggingInterceptor)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://coronavirus-tracker-api.herokuapp.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient.build())
                .build()

            return retrofit.create(CovidApi::class.java)
        }
    }
}