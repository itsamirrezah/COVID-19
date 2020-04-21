package com.itsamirrezah.covid19.data.novelapi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class NovelApiImp {

    companion object {

        fun getApi(): NovelApi {

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClient = OkHttpClient()
                .newBuilder()
                .addInterceptor(loggingInterceptor)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://corona.lmao.ninja")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient.build())
                .build()

            return retrofit.create(NovelApi::class.java)
        }
    }
}